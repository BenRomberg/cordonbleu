package com.benromberg.cordonbleu.service.coderepository;

import static com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock.COMMIT_CONTENT_AFTER;
import static com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock.COMMIT_CONTENT_BEFORE;
import static com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock.COMMIT_PATH_AFTER;
import static com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock.COMMIT_PATH_BEFORE;
import static com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock.createCommit;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitFixture;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import com.benromberg.cordonbleu.service.coderepository.CommitDetail;
import com.benromberg.cordonbleu.service.coderepository.RawCommitFilter;

public class CodeRepositoryServiceTest implements CommitFixture {
    private static final String OTHER_REPOSITORY_NAME = "other-repository";
    private static final CodeRepositoryMetadata OTHER_REPOSITORY = new RepositoryBuilder().name(OTHER_REPOSITORY_NAME)
            .build();
    private static final RawCommitFilter COMMIT_FILTER = new RawCommitFilter(asList(COMMIT_AUTHOR), asList(), true,
            Optional.empty(), 100, false);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public DaoRule databaseRule = new DaoRule().withTeam();

    private CodeRepositoryFactoryMock codeRepositoryFactory;
    private CodeRepositoryService service;
    private final CodeRepositoryMetadataDao repositoryDao = databaseRule.createRepositoryDao();
    private final UserDao userDao = databaseRule.createUserDao();
    private final CommitDao commitDao = databaseRule.createCommitDao();
    private CodeRepositoryMock codeRepository;

    @Before
    public void setUp() {
        codeRepositoryFactory = new CodeRepositoryFactoryMock(REPOSITORY);
        codeRepository = codeRepositoryFactory.getDefaultRepository();
        service = new CodeRepositoryService(codeRepositoryFactory, () -> temporaryFolder.getRoot(), repositoryDao,
                commitDao, userDao, databaseRule.createTeamDao(), () -> "");
    }

    @Test
    public void newRepository_IsDroppedIntoCorrectFolder() throws Exception {
        repositoryDao.insert(REPOSITORY);
        updateRepositories();
        assertThat(codeRepositoryFactory.getLastFolder()).isEqualTo(
                new File(temporaryFolder.getRoot(), REPOSITORY.getId()));
    }

    @Test
    public void repositoriesWithSameCommits_AddSecondRepositoryToSameCommit() throws Exception {
        repositoryDao.insert(REPOSITORY);
        repositoryDao.insert(OTHER_REPOSITORY);
        updateRepositories();
        assertThat(commitDao.findById(COMMIT_ID).get().getRepositories()).extracting(
                repository -> repository.getRepository().getName())
                .containsOnly(REPOSITORY_NAME, OTHER_REPOSITORY_NAME);
    }

    @Test
    public void repositoriesWithSameCommits_CallsConsumerWithBothRepositories() throws Exception {
        repositoryDao.insert(REPOSITORY);
        repositoryDao.insert(OTHER_REPOSITORY);
        updateRepositories();
    }

    @Test
    public void updateRepositories_PullsAndStoresCommits_FromAllRepositoriesInDatabase() throws Exception {
        repositoryDao.insert(REPOSITORY);
        updateRepositories();
        List<Commit> commits = service.getCommitsForFilter(COMMIT_FILTER, asList(REPOSITORY));
        assertThat(commits).extracting(Commit::getId).containsExactly(COMMIT_ID);
    }

    @Test
    public void updateRepositories_WithRemovedCommits_SetsRemovedFlagOnCommits() throws Exception {
        repositoryDao.insert(REPOSITORY);
        commitDao.insert(COMMIT);
        codeRepository.setNewCommits(emptyList());
        codeRepository.setRemovedCommitIds(asList(COMMIT_ID));
        updateRepositories();
        List<Commit> commits = service.getCommitsForFilter(COMMIT_FILTER, asList(REPOSITORY));
        assertThat(commits).extracting(Commit::getId, Commit::isRemoved).containsExactly(tuple(COMMIT_ID, true));
    }

    @Test
    public void getCommitsForFilter_WithoutRepositories_ReturnsEmptyList() throws Exception {
        List<Commit> commits = service.getCommitsForFilter(new RawCommitFilter(asList(COMMIT_AUTHOR), asList(), true,
                Optional.empty(), 100, false), asList());
        assertThat(commits).isEmpty();
    }

    @Test
    public void updateRepositories_CleansUpCommitsFromDeletedRepositories() throws Exception {
        repositoryDao.insert(REPOSITORY);
        commitDao.insert(createCommit(REPOSITORY).getCommit());
        repositoryDao.remove(REPOSITORY.getId());
        updateRepositories();
        Optional<Commit> foundCommit = commitDao.findById(COMMIT_ID);
        assertThat(foundCommit).isEmpty();
    }

    @Test
    public void addingRepository_CanBeFoundWithGetRepositories() throws Exception {
        service.addRepository(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL);
        List<CodeRepositoryMetadata> repositories = service.findByTeam(TEAM);
        assertThat(repositories).extracting(CodeRepositoryMetadata::getName, CodeRepositoryMetadata::getSourceUrl)
                .containsExactly(tuple(REPOSITORY_NAME, REPOSITORY_URL));
    }

    @Test
    public void commitDetails_AreRetrievedFromCodeRepository() throws Exception {
        repositoryDao.insert(REPOSITORY);
        commitDao.insert(createCommit(REPOSITORY).getCommit());
        CommitDetail commitDetail = service.getCommitDetail(COMMIT_ID).get();
        assertThat(commitDetail.getCommit().getId()).isEqualTo(COMMIT_ID);
        assertThat(commitDetail.getFiles()).hasSize(1);
        assertThat(commitDetail.getFiles().get(0).getStateBefore().get().getPath()).isEqualTo(COMMIT_PATH_BEFORE);
        assertThat(commitDetail.getFiles().get(0).getStateBefore().get().getContent()).isEqualTo(
                COMMIT_CONTENT_BEFORE.getContent());
        assertThat(commitDetail.getFiles().get(0).getStateAfter().get().getPath()).isEqualTo(COMMIT_PATH_AFTER);
        assertThat(commitDetail.getFiles().get(0).getStateAfter().get().getContent()).isEqualTo(
                COMMIT_CONTENT_AFTER.getContent());
    }

    @Test
    public void removingNonExistingRepository_ReturnsFalse() throws Exception {
        assertThat(service.removeRepository(REPOSITORY.getId())).isFalse();
    }

    @Test
    public void removedRepository_RemovesDatabaseEntries() throws Exception {
        repositoryDao.insert(REPOSITORY);
        boolean removalResult = service.removeRepository(REPOSITORY.getId());
        updateRepositories();
        assertThat(removalResult).isTrue();
        assertThat(repositoryDao.findActive()).isEmpty();
    }

    @Test
    public void removedRepository_RemovesRepositoryFolder() throws Exception {
        repositoryDao.insert(REPOSITORY);
        updateRepositories();
        service.removeRepository(REPOSITORY.getId());
        updateRepositories();
        assertThat(codeRepositoryFactory.getDefaultRepository().wasRemoveCalled()).isTrue();
    }

    @Test
    public void removeRepository_WithCommitsContainingMultipleRepositories_CleansUpCommitsOnSubsequentUpdate()
            throws Exception {
        repositoryDao.insert(REPOSITORY);
        repositoryDao.insert(OTHER_REPOSITORY);
        updateRepositories();
        assertThat(service.removeRepository(REPOSITORY.getId())).isTrue();
        updateRepositories();
        assertThat(commitDao.findByRepositories(asList(OTHER_REPOSITORY))).extracting(Commit::getRepositories).hasSize(
                1);
    }

    private void updateRepositories() {
        service.updateRepositories(commit -> {
        });
    }
}
