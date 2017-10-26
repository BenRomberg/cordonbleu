package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;
import com.benromberg.cordonbleu.service.coderepository.svncredential.SvnCredentialProvider;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SvnRepository implements CodeRepository, AutoCloseable {
    private static final int PULL_LIMIT = 100;

    private static SVNClientManager svn;
    private final CodeRepositoryMetadata repositoryMetadata;
    private final SvnCredentialProvider svnCredentialProvider;
    private final File folder;
    private SVNURL uri;
    private SVNRepository repository = null;
    private ISVNAuthenticationManager authManager;


    public SvnRepository(CodeRepositoryMetadata repositoryMetadata, File folder,
                         SvnCredentialProvider svnCredentialProvider) {
        this.repositoryMetadata = repositoryMetadata;
        this.svnCredentialProvider = svnCredentialProvider;
        this.folder=folder;
        try {
            this.uri = SVNURL.parseURIEncoded(repositoryMetadata.getSourceUrl());

            manageAuthentification(repositoryMetadata.getSourceUrl());

            // no need to checkout svn project on local repository because we need to work only with distant repo
            // indeed svn don't allow to work with a working repository to find logs

            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repositoryMetadata.getSourceUrl()));
            repository.setAuthenticationManager(authManager);

        } catch (Exception e) {
            new RuntimeException(e);
        }

    }

    private void manageAuthentification(String url)  throws SVNException{
        ISVNOptions options = SVNWCUtil.createDefaultOptions( true );

        SVNAuthentication svnAuth = new SVNPasswordAuthentication(this.svnCredentialProvider.getSvnUser(),this.svnCredentialProvider.getSvnPassword(), true, uri, false);
        SVNAuthentication[] svnAuths = new SVNAuthentication[1];
        svnAuths[0] = svnAuth;
        authManager = new BasicAuthenticationManager(svnAuths);

        svn = SVNClientManager.newInstance( options , authManager );
    }


    private void checkout() throws SVNException {
        SVNUpdateClient updateClient = svn.getUpdateClient();
        updateClient.setIgnoreExternals( false );

        updateClient.doCheckout( uri , folder , SVNRevision.HEAD , SVNRevision.HEAD , SVNDepth.INFINITY , true );
    }

    private void update() throws SVNException {
        SVNUpdateClient updateClient = svn.getUpdateClient( );
        updateClient.setIgnoreExternals( false );

        updateClient.doUpdate( folder , SVNRevision.HEAD , SVNDepth.INFINITY , true  , true);
    }

    @Override
    public PullResult pull(Collection<Commit> existingCommits) {
        // no checkout so no update, just collect commits !
        return collectCommits(new HashSet<>(existingCommits));
    }


    private PullResult collectCommits(Set<Commit> existingCommits) {
        Set<String> existingHashes = existingCommits.stream().map(commit -> commit.getId().getHash()).collect(toSet());
        String[] paths = {""};
        try {
            Collection<SVNLogEntry > logEntries = repository.log(paths,null,0,-1,true,true);

            return createPullResult(existingHashes, logEntries);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String createHash(long revision) {
        return revision + "-" + repositoryMetadata.getTeam().getName() + "-" + repositoryMetadata.getName();
    }

    private PullResult createPullResult(Set<String> existingHashes, Collection<SVNLogEntry > logEntries) {

        List<CommitWithRepository> newCommits = new ArrayList<>();
        Set<String> repositoryHashes = new HashSet<>();

        for (final SVNLogEntry logEntry : logEntries) {
            String hash = createHash(logEntry.getRevision());
            repositoryHashes.add(hash);

            if (!existingHashes.contains(hash) && newCommits.size() < PULL_LIMIT) {
                newCommits.add(toCommit(logEntry));
            }
        }

        Set<String> removedHashes = new HashSet<>(existingHashes);
        removedHashes.removeAll(repositoryHashes);
        return new PullResult(newCommits, removedHashes.stream()
            .map(hash -> new CommitId(hash, repositoryMetadata.getTeam())).collect(toList()));
    }

    private CommitWithRepository toCommit(SVNLogEntry logEntry) {
        CommitRepository commitRepository = new CommitRepository(repositoryMetadata, new ArrayList<>());

        String hash = createHash(logEntry.getRevision());

        CommitId commitId = new CommitId(hash, repositoryMetadata.getTeam());
        return new CommitWithRepository(new Commit(commitId, asList(commitRepository), new CommitAuthor(logEntry.getAuthor(),
            "TODO"), LocalDateTime.ofEpochSecond(
            logEntry.getDate().getTime()/1000l, 0, ZoneOffset.UTC), logEntry.getMessage()), commitRepository);
    }

    @Override
    public void close() throws Exception {
        svn.dispose();
        repository.closeSession();
    }

    @Override
    public CommitDetail getCommitDetail(final Commit commit) {
        return new SvnCommitDetail(repositoryMetadata, repository).getCommitDetail(commit);
    }

    @Override
    public void remove() {
        // no directory to delete
    }
}
