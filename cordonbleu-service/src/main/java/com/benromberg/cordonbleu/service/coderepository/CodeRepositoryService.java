package com.benromberg.cordonbleu.service.coderepository;

import static java.util.Arrays.asList;
import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.RepositoryFlag;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.benromberg.cordonbleu.service.coderepository.svncredential.SvnCredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benromberg.cordonbleu.service.coderepository.keypair.SshPrivateKeyPasswordProvider;

@Singleton
public class CodeRepositoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeRepositoryService.class);

    private final CodeRepositoryFactory codeRepositoryFactory;
    private final File folder;
    private final CodeRepositoryMetadataDao repositoryDao;
    private final CommitDao commitDao;
    private final UserDao userDao;
    private final TeamDao teamDao;
    private final SshPrivateKeyPasswordProvider sshPrivateKeyPasswordProvider;
    private final SvnCredentialProvider svnCredentialProvider;

    @Inject
    public CodeRepositoryService(CodeRepositoryFactory codeRepositoryFactory,
                                 CodeRepositoryFolderProvider folderProvider, CodeRepositoryMetadataDao repositoryDao, CommitDao commitDao,
                                 UserDao userDao, TeamDao teamDao, SshPrivateKeyPasswordProvider sshPrivateKeyPasswordProvider, SvnCredentialProvider svnCredentialProvider) {
        this.codeRepositoryFactory = codeRepositoryFactory;
        this.repositoryDao = repositoryDao;
        this.commitDao = commitDao;
        this.userDao = userDao;
        this.teamDao = teamDao;
        this.sshPrivateKeyPasswordProvider = sshPrivateKeyPasswordProvider;
        this.svnCredentialProvider = svnCredentialProvider;
        this.folder = folderProvider.getCodeRepositoryFolder();
    }

    public interface CodeRepositoryFolderProvider {
        File getCodeRepositoryFolder();
    }

    public void updateRepositories(Consumer<Commit> callback) {
        List<CodeRepositoryMetadata> activeRepositories = repositoryDao.findActive();
        removeObsoleteRepositories(activeRepositories);
        activeRepositories.forEach(repository -> {
            try {
                updateRepository(repository, callback);
            } catch (Exception e) {
                LOGGER.warn("Couldn't update repository {}.", repository.getName(), e);
            }
        });
    }

    private void removeObsoleteRepositories(List<CodeRepositoryMetadata> remainingRepositories) {
        List<CodeRepositoryMetadata> removeRepositories = repositoryDao.findByFlag(
                RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        commitDao.removeOrphaned(remainingRepositories);
        removeRepositories.forEach(repository -> {
            repositoryDao.remove(repository.getId());
            getRepository(repository).remove();
        });
    }

    public List<Commit> getCommitsForFilter(RawCommitFilter commitFilter, List<CodeRepositoryMetadata> repositories) {
        List<User> users = userDao.findByIds(commitFilter.getUserIds());
        if (repositories.isEmpty()) {
            return asList();
        }
        Team team = repositories.get(0).getTeam();
        return commitDao.findByFilter(commitFilter.toCommitFilter(team, repositories, users));
    }

    public List<CodeRepositoryMetadata> findRepositories(List<String> repositories) {
        return repositoryDao.findByIds(repositories);
    }

    public Optional<CommitDetail> getCommitDetail(CommitId commitId) {
        return commitDao.findById(commitId).map(commit -> getCommitDetail(commit));
    }

    public CommitDetail getCommitDetail(Commit commit) {
        return getRepository(commit.getRepositories().get(0).getRepository()).getCommitDetail(commit);
    }

    private List<Commit> getCommits(List<CodeRepositoryMetadata> repositoryMetadata) {
        return commitDao.findByRepositories(repositoryMetadata);
    }

    private void updateRepository(CodeRepositoryMetadata metadata, Consumer<Commit> callback) {
        CodeRepository codeRepository = getRepository(metadata);
        List<Commit> existingCommits = getCommits(asList(metadata));
        PullResult pullResult = codeRepository.pull(existingCommits);
        pullResult.getNewCommits().forEach(commit -> importCommit(commit, callback));
        pullResult.getRemovedCommitIds().forEach(commitId -> commitDao.updateAsRemoved(commitId));
    }

    private void importCommit(CommitWithRepository commit, Consumer<Commit> callback) {
        Commit insertedCommit = commitDao.insertOrUpdateRepository(commit.getCommit(), commit.getRepository());
        callback.accept(insertedCommit);
    }

    private CodeRepository getRepository(CodeRepositoryMetadata metadata) {
        return codeRepositoryFactory.createCodeRepository(metadata, new File(folder, metadata.getId()),
                sshPrivateKeyPasswordProvider, svnCredentialProvider);
    }

    public List<CodeRepositoryMetadata> findByTeam(Team team) {
        return repositoryDao.findByTeam(team);
    }

    public CodeRepositoryMetadata addRepository(String teamId, String name, String sourceUrl, String type) {
        CodeRepositoryMetadata repository = new CodeRepositoryMetadata(sourceUrl, name, teamDao.findById(teamId).get(), type);
        repositoryDao.insert(repository);
        return repository;
    }

    public boolean removeRepository(String repositoryId) {
        Optional<CodeRepositoryMetadata> repository = repositoryDao.findById(repositoryId);
        if (!repository.isPresent()) {
            return false;
        }
        repositoryDao.updateFlag(repositoryId, RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        return true;
    }

    public List<CommitAuthor> findTeamAuthors(Team team) {
        return commitDao.findAuthors(team);
    }
}
