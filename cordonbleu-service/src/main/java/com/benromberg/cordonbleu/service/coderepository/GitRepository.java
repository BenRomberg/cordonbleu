package com.benromberg.cordonbleu.service.coderepository;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class GitRepository implements CodeRepository, AutoCloseable {
    private static final int PULL_LIMIT = 100;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String REMOTE_BRANCH_PREFIX = Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/";

    static {
        JSch.setConfig("StrictHostKeyChecking", "no");
    }

    private final Repository repository;
    private final Git git;
    private final CodeRepositoryMetadata repositoryMetadata;
    private final SshPrivateKeyPasswordProvider passwordProvider;

    public GitRepository(CodeRepositoryMetadata repositoryMetadata, File folder,
            SshPrivateKeyPasswordProvider passwordProvider) {
        this.repositoryMetadata = repositoryMetadata;
        this.passwordProvider = passwordProvider;
        if (folder.exists()) {
            repository = convertException(() -> new FileRepositoryBuilder().setGitDir(
                    new File(folder, Constants.DOT_GIT)).build());
            git = new Git(repository);
            configureRepository();
            return;
        }
        git = callWithAuthentication(Git.cloneRepository().setURI(repositoryMetadata.getSourceUrl())
                .setDirectory(folder).setCloneAllBranches(true));
        repository = git.getRepository();
        configureRepository();
    }

    private void configureRepository() {
        repository.getConfig().setBoolean("remote", "origin", "prune", true);
    }

    private <C extends GitCommand<T>, T> T callWithAuthentication(TransportCommand<C, T> command) {
        return convertException(() -> command.setTransportConfigCallback(new ConfigCallback()).call());
    }

    @Override
    public PullResult pull(Collection<Commit> existingCommits) {
        convertException(() -> git.reset().setMode(ResetType.HARD).call());
        convertException(() -> git.clean().setCleanDirectories(true).setIgnore(false).call());
        callWithAuthentication(git.pull().setStrategy(MergeStrategy.THEIRS));
        return collectCommits(new HashSet<>(existingCommits));
    }

    private PullResult collectCommits(Set<Commit> existingCommits) {
        Set<String> existingHashes = existingCommits.stream().map(commit -> commit.getId().getHash()).collect(toSet());
        try (RevWalk walk = new RevWalk(repository)) {
            getRemoteBranchReferences().stream().forEach(headReference -> {
                convertException(() -> walk.markStart(walk.parseCommit(headReference.getObjectId())));
            });
            return createPullResult(existingHashes, StreamSupport.stream(walk.spliterator(), false));
        }
    }

    private PullResult createPullResult(Set<String> existingHashes, Stream<RevCommit> walkStream) {
        Set<String> repositoryHashes = new HashSet<>();
        List<CommitWithRepository> newCommits = new ArrayList<>();
        walkStream.forEach(commit -> {
            repositoryHashes.add(commit.getName());
            if (!existingHashes.contains(commit.getName()) && newCommits.size() < PULL_LIMIT) {
                newCommits.add(toCommit(commit));
            }
        });
        Set<String> removedHashes = new HashSet<>(existingHashes);
        removedHashes.removeAll(repositoryHashes);
        return new PullResult(newCommits, removedHashes.stream()
                .map(hash -> new CommitId(hash, repositoryMetadata.getTeam())).collect(toList()));
    }

    public List<String> getBranches() {
        return getRemoteBranchReferences().stream()
                .map(branchReference -> convertBranchReferenceToName(branchReference)).collect(toList());
    }

    private String convertBranchReferenceToName(Ref branchReference) {
        return branchReference.getName().replaceFirst("^" + REMOTE_BRANCH_PREFIX, "");
    }

    @Override
    public void close() throws Exception {
        repository.close();
    }

    @Override
    public CommitDetail getCommitDetail(Commit commit) {
        return new GitCommitDetail(repository).getCommitDetail(commit);
    }

    private CommitWithRepository toCommit(RevCommit commit) {
        CommitRepository commitRepository = new CommitRepository(repositoryMetadata, getBranchesForCommit(commit));
        CommitId commitId = new CommitId(commit.getName(), repositoryMetadata.getTeam());
        return new CommitWithRepository(new Commit(commitId, asList(commitRepository), new CommitAuthor(commit
                .getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress()), LocalDateTime.ofEpochSecond(
                commit.getCommitTime(), 0, ZoneOffset.UTC), commit.getFullMessage()), commitRepository);
    }

    private List<Ref> getRemoteBranchReferences() {
        return convertException(() -> git.branchList().setListMode(ListMode.REMOTE).call());
    }

    private List<String> getBranchesForCommit(RevCommit commit) {
        return convertException(() -> git.branchList().setListMode(ListMode.REMOTE).setContains(commit.getName())
                .call().stream().map(branchReference -> convertBranchReferenceToName(branchReference))
                .collect(toList()));
    }

    @Override
    public void remove() {
        convertException(() -> FileUtils.deleteDirectory(repository.getWorkTree()));
    }

    private class ConfigCallback implements TransportConfigCallback {
        @Override
        public void configure(Transport transport) {
            if (transport instanceof SshTransport) {
                ((SshTransport) transport).setSshSessionFactory(new SshSessionFactory());
            }
        }
    }

    private class SshSessionFactory extends JschConfigSessionFactory {
        @Override
        protected void configure(Host hc, Session session) {
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);
            defaultJSch.addIdentity(null, repositoryMetadata.getTeam().getKeyPair().getPrivateKey().getBytes(CHARSET),
                    null, passwordProvider.getSshPrivateKeyPassword().getBytes(CHARSET));
            return defaultJSch;
        }
    }
}
