package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;
import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;
import com.benromberg.cordonbleu.util.ClockService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class GitRepository implements CodeRepository, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepository.class);
    private static final int PULL_LIMIT = 100;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String REMOTE_BRANCH_PREFIX = Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/";
    private static final String INDEX_LOCK_FILE = "index.lock";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(5);

    static {
        JSch.setConfig("StrictHostKeyChecking", "no");
    }

    private final Repository repository;
    private final Git git;
    private final CodeRepositoryMetadata repositoryMetadata;
    private final SshPrivateKeyPasswordProvider passwordProvider;

    public GitRepository(CodeRepositoryMetadata repositoryMetadata, File folder, SshPrivateKeyPasswordProvider passwordProvider) {
        this.repositoryMetadata = repositoryMetadata;
        this.passwordProvider = passwordProvider;
        if (folder.exists()) {
            repository = convertException(() -> new FileRepositoryBuilder().setGitDir(new File(folder, Constants.DOT_GIT)).build());
            git = new Git(repository);
            configureRepository();
            return;
        }
        git = callWithAuthentication(
                Git.cloneRepository().setURI(repositoryMetadata.getSourceUrl()).setDirectory(folder).setCloneAllBranches(true));
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
        if (convertException(this::repositoryIsLocked)) {
            LOGGER.warn("Repository {} is locked, waiting {} before unlocking it. Skipping Pull for now.", repositoryMetadata.getName(),
                    LOCK_TIMEOUT);
            return new PullResult(emptyList(), emptyList());
        }
        convertException(() -> git.reset().setMode(ResetType.HARD).call());
        convertException(() -> git.clean().setCleanDirectories(true).setIgnore(false).call());
        callWithAuthentication(git.pull().setStrategy(MergeStrategy.THEIRS));
        return collectCommits(new HashSet<>(existingCommits));
    }

    private boolean repositoryIsLocked() throws IOException {
        File indexLock = new File(repository.getDirectory(), INDEX_LOCK_FILE);
        if (indexLock.exists()) {
            if (Instant.ofEpochMilli(indexLock.lastModified()).plus(LOCK_TIMEOUT).isBefore(ClockService.now().toInstant(ZoneOffset.UTC))) {
                Files.delete(indexLock.toPath());
                return false;
            }
            return true;
        }
        return false;
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
        return new PullResult(newCommits,
                removedHashes.stream().map(hash -> new CommitId(hash, repositoryMetadata.getTeam())).collect(toList()));
    }

    public List<String> getBranches() {
        return getRemoteBranchReferences().stream().map(branchReference -> convertBranchReferenceToName(branchReference)).collect(toList());
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
        return new CommitWithRepository(new Commit(commitId, asList(commitRepository),
                new CommitAuthor(commit.getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress()), Optional.empty(),
                LocalDateTime.ofEpochSecond(commit.getCommitTime(), 0, ZoneOffset.UTC), commit.getFullMessage(), ClockService.now()),
                commitRepository);
    }

    private List<Ref> getRemoteBranchReferences() {
        return convertException(() -> git.branchList().setListMode(ListMode.REMOTE).call());
    }

    private List<String> getBranchesForCommit(RevCommit commit) {
        return convertException(() -> git.branchList()
                .setListMode(ListMode.REMOTE)
                .setContains(commit.getName())
                .call()
                .stream()
                .map(branchReference -> convertBranchReferenceToName(branchReference))
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
            defaultJSch.addIdentity(null, repositoryMetadata.getTeam().getKeyPair().getPrivateKey().getBytes(CHARSET), null,
                    passwordProvider.getSshPrivateKeyPassword().getBytes(CHARSET));
            return defaultJSch;
        }
    }
}
