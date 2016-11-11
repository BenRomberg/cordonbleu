package com.benromberg.cordonbleu.data.dao;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.model.RepositoryFixture;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityExistsException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.RepositoryFlag;
import com.benromberg.cordonbleu.data.model.Team;

public class CodeRepositoryMetadataDaoTest implements RepositoryFixture {
    private static final Team OTHER_TEAM = new TeamBuilder().name("other-team").build();
    private static final CodeRepositoryMetadata OTHER_REPOSITORY = new RepositoryBuilder().name("other-name").build();
    private static final CodeRepositoryMetadata UPPERCASE_REPOSITORY = new RepositoryBuilder().name("Uppercase-Repo")
            .build();

    @Rule
    public DaoRule databaseRule = new DaoRule().withTeam();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final CodeRepositoryMetadataDao dao = databaseRule.createRepositoryDao();
    private final TeamDao teamDao = databaseRule.createTeamDao();

    @Test
    public void insertedElement_CanBeFoundById() throws Exception {
        CodeRepositoryMetadata dummyElement = REPOSITORY;
        dao.insert(dummyElement);
        CodeRepositoryMetadata foundElement = dao.findById(dummyElement.getId()).get();
        assertThat(foundElement.getName()).isEqualTo(REPOSITORY_NAME);
        assertThat(foundElement.getSourceUrl()).isEqualTo(REPOSITORY_URL);
        assertThat(foundElement.getTeam().getName()).isEqualTo(TEAM_NAME);
    }

    @Test
    public void insertedElement_CanNotHaveDuplicateName_InSameTeam() throws Exception {
        dao.insert(REPOSITORY);
        expectedException.expect(EntityExistsException.class);
        dao.insert(REPOSITORY);
    }

    @Test
    public void insertedElement_CanHaveDuplicateName_AcrossTeams() throws Exception {
        dao.insert(REPOSITORY);
        teamDao.insert(OTHER_TEAM);
        CodeRepositoryMetadata repositoryInOtherTeam = repository().team(OTHER_TEAM).build();
        dao.insert(repositoryInOtherTeam);
        CodeRepositoryMetadata foundRepository = dao.findById(repositoryInOtherTeam.getId()).get();
        assertThat(foundRepository.getTeam()).isEqualTo(OTHER_TEAM);
    }

    @Test
    public void insertedElement_CanBeFoundByIds() throws Exception {
        dao.insert(REPOSITORY);
        List<CodeRepositoryMetadata> foundElements = dao.findByIds(asList(REPOSITORY_ID));
        assertThat(foundElements).extracting(CodeRepositoryMetadata::getName, CodeRepositoryMetadata::getSourceUrl)
                .containsExactly(tuple(REPOSITORY_NAME, REPOSITORY_URL));
    }

    @Test
    public void findAllRepositories_ReturnsRepositoriesSortedByName() throws Exception {
        dao.insert(REPOSITORY);
        dao.insert(OTHER_REPOSITORY);
        List<CodeRepositoryMetadata> foundRepos = dao.findActive();
        assertThat(foundRepos).containsExactly(OTHER_REPOSITORY, REPOSITORY);
    }

    @Test
    public void findAllRepositories_ReturnsRepositoriesSortedByName_CaseInsensitive() throws Exception {
        dao.insert(REPOSITORY);
        dao.insert(UPPERCASE_REPOSITORY);
        List<CodeRepositoryMetadata> foundRepos = dao.findActive();
        assertThat(foundRepos).containsExactly(REPOSITORY, UPPERCASE_REPOSITORY);
    }

    @Test
    public void findAllRepositories_HavingRemovalFlag_ReturnsOnlyActiveRepositories() throws Exception {
        dao.insert(REPOSITORY);
        dao.updateFlag(REPOSITORY.getId(), RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        dao.insert(OTHER_REPOSITORY);
        List<CodeRepositoryMetadata> activeRepositories = dao.findActive();
        assertThat(activeRepositories).extracting(CodeRepositoryMetadata::getName).containsExactly(
                OTHER_REPOSITORY.getName());
    }

    @Test
    public void findByTeam_ReturnsRepositoriesSortedByName() throws Exception {
        dao.insert(REPOSITORY);
        dao.insert(OTHER_REPOSITORY);
        List<CodeRepositoryMetadata> foundRepos = dao.findByTeam(TEAM);
        assertThat(foundRepos).containsExactly(OTHER_REPOSITORY, REPOSITORY);
    }

    @Test
    public void findByTeam_IgnoresRepositoryInOtherTeam() throws Exception {
        Team otherTeam = team().name("other-team").build();
        databaseRule.createTeamDao().insert(otherTeam);
        CodeRepositoryMetadata repositoryInOtherTeam = repository().team(otherTeam).build();
        dao.insert(repositoryInOtherTeam);
        List<CodeRepositoryMetadata> foundRepos = dao.findByTeam(TEAM);
        assertThat(foundRepos).isEmpty();
    }

    @Test
    public void findByTeam_ReturnsRepositoriesSortedByName_CaseInsensitive() throws Exception {
        dao.insert(REPOSITORY);
        dao.insert(UPPERCASE_REPOSITORY);
        List<CodeRepositoryMetadata> foundRepos = dao.findByTeam(TEAM);
        assertThat(foundRepos).containsExactly(REPOSITORY, UPPERCASE_REPOSITORY);
    }

    @Test
    public void findByTeam_HavingRemovalFlag_ReturnsOnlyActiveRepositories() throws Exception {
        dao.insert(REPOSITORY);
        dao.updateFlag(REPOSITORY.getId(), RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        dao.insert(OTHER_REPOSITORY);
        List<CodeRepositoryMetadata> activeRepositories = dao.findByTeam(TEAM);
        assertThat(activeRepositories).extracting(CodeRepositoryMetadata::getName).containsExactly(
                OTHER_REPOSITORY.getName());
    }

    @Test
    public void updateFlag_WithMissingRepository_ReturnsEmptyRepository() throws Exception {
        Optional<CodeRepositoryMetadata> user = dao.updateFlag("non-existing-id", RepositoryFlag.REMOVE_ON_NEXT_UPDATE,
                true);
        assertThat(user).isEmpty();
    }

    @Test
    public void updateFlag_WithExistingUserWithoutFlag_ReturnsUserWithFlag() throws Exception {
        dao.insert(REPOSITORY);
        CodeRepositoryMetadata repository = dao.updateFlag(REPOSITORY.getId(), RepositoryFlag.REMOVE_ON_NEXT_UPDATE,
                true).get();
        assertThat(repository.isRemoveOnNextUpdate()).isTrue();
    }

    @Test
    public void updateFlag_WithExistingUserWithFlag_ReturnsUserWithoutFlag() throws Exception {
        dao.insert(REPOSITORY);
        dao.updateFlag(REPOSITORY.getId(), RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        CodeRepositoryMetadata repository = dao.updateFlag(REPOSITORY.getId(), RepositoryFlag.REMOVE_ON_NEXT_UPDATE,
                false).get();
        assertThat(repository.isRemoveOnNextUpdate()).isFalse();
    }

    @Test
    public void findByFlag_HavingRemovalFlag_ReturnsRemovalRepositories() throws Exception {
        dao.insert(REPOSITORY);
        dao.updateFlag(REPOSITORY.getId(), RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        dao.insert(OTHER_REPOSITORY);
        List<CodeRepositoryMetadata> removalRepositories = dao.findByFlag(RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        assertThat(removalRepositories).extracting(CodeRepositoryMetadata::getName).containsExactly(REPOSITORY_NAME);
    }

    @Test
    public void findByFlag_HavingNoRemovalFlag_ReturnsRegularRepositories() throws Exception {
        dao.insert(REPOSITORY);
        dao.updateFlag(REPOSITORY.getId(), RepositoryFlag.REMOVE_ON_NEXT_UPDATE, true);
        dao.insert(OTHER_REPOSITORY);
        List<CodeRepositoryMetadata> regularRepositories = dao.findByFlag(RepositoryFlag.REMOVE_ON_NEXT_UPDATE, false);
        assertThat(regularRepositories).extracting(CodeRepositoryMetadata::getName).containsExactly(
                OTHER_REPOSITORY.getName());
    }

    @Test
    public void findByFlag_ReturnsRepositoriesSortedByName() throws Exception {
        dao.insert(REPOSITORY);
        dao.insert(OTHER_REPOSITORY);
        List<CodeRepositoryMetadata> allRepositories = dao.findByFlag(RepositoryFlag.REMOVE_ON_NEXT_UPDATE, false);
        assertThat(allRepositories).extracting(CodeRepositoryMetadata::getName).containsExactly(
                OTHER_REPOSITORY.getName(), REPOSITORY_NAME);
    }
}
