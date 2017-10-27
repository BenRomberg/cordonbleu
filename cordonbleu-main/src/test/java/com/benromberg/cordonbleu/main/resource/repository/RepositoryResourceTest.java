package com.benromberg.cordonbleu.main.resource.repository;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.RepositoryFixture;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;

import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.main.resource.repository.AddRepositoryRequest;
import com.benromberg.cordonbleu.main.resource.repository.DeleteRepositoryRequest;

public class RepositoryResourceTest implements RepositoryFixture {
    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule().withTeam();

    private final CodeRepositoryService repositoryService = RULE.getInstance(CodeRepositoryService.class);
    private final TeamDao teamDao = RULE.getInstance(TeamDao.class);

    @Test
    public void listRepositories_WithWrongTeam_YieldsNotFound() throws Exception {
        Response response = RULE.withTeamOwnerUser().param("teamId", "doesntExist").get("/api/repository/list");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void listRepositories_WithoutPermission_YieldsForbidden() throws Exception {
        Response response = RULE.withTeamUser().param("teamId", TEAM_ID).get("/api/repository/list");
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void listRepositories_WithoutRepositories_ReturnsEmptyList() throws Exception {
        Response response = RULE.withTeamOwnerUser().param("teamId", TEAM_ID).get("/api/repository/list");
        assertEmptyRepositoryList(response);
    }

    @Test
    public void listRepositories_WithRepository_ReturnsList() throws Exception {
        repositoryService.addRepository(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE);
        Response response = RULE.withTeamOwnerUser().param("teamId", TEAM_ID).get("/api/repository/list");
        assertRepositoryListWithOneElement(response);
    }

    @Test
    public void listRepositories_WithRepositoryInOtherTeam_ReturnsEmptyList() throws Exception {
        Team otherTeam = team().name("other-team").build();
        teamDao.insert(otherTeam);
        repositoryService.addRepository(otherTeam.getId(), REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE);
        Response response = RULE.withTeamOwnerUser().param("teamId", TEAM_ID).get("/api/repository/list");
        assertEmptyRepositoryList(response);
    }

    @Test
    public void addRepository_WithWrongTeam_YieldsNotFound() throws Exception {
        Response response = RULE.withTeamOwnerUser().post("/api/repository/add",
                new AddRepositoryRequest("doesntExist", REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addRepository_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.post("/api/repository/add", new AddRepositoryRequest(TEAM_ID, REPOSITORY_NAME,
                REPOSITORY_URL, REPOSITORY_TYPE));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void addRepository_WithoutPermission_YieldsForbiddenWithNoRepositoryAdded() throws Exception {
        Response response = RULE.withTeamUser().post("/api/repository/add",
                new AddRepositoryRequest(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
        assertThat(repositoryService.findByTeam(TEAM)).isEmpty();
    }

    @Test
    public void addRepository_WithTeamOwner_ReturnsList() throws Exception {
        Response response = RULE.withTeamOwnerUser().post("/api/repository/add",
                new AddRepositoryRequest(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE));
        assertRepositoryListWithOneElement(response);
    }

    @Test
    public void addRepository_WithDuplicateName_YieldsConflict() throws Exception {
        repositoryService.addRepository(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE);
        Response response = RULE.withTeamOwnerUser().post("/api/repository/add",
                new AddRepositoryRequest(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE));
        assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());
    }

    @Test
    public void deleteRepository_WithWrongTeam_YieldsNotFound() throws Exception {
        Response response = RULE.withTeamOwnerUser().post("/api/repository/delete",
                new DeleteRepositoryRequest(REPOSITORY_ID, "doesntExist"));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteRepository_WithoutPermission_YieldsForbiddenWithNoRepositoryDeleted() throws Exception {
        CodeRepositoryMetadata repository = repositoryService.addRepository(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE);
        Response response = RULE.withTeamUser().post("/api/repository/delete",
                new DeleteRepositoryRequest(repository.getId(), TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
        assertThat(repositoryService.findByTeam(TEAM)).hasSize(1);
    }

    @Test
    public void deleteRepository_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.post("/api/repository/delete", new DeleteRepositoryRequest(REPOSITORY_ID, TEAM_ID));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void deleteRepository_WithNonExistingRepository_DoesNothing() throws Exception {
        repositoryService.addRepository(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE);
        Response response = RULE.withTeamOwnerUser().post("/api/repository/delete",
                new DeleteRepositoryRequest("non-existing-id", TEAM_ID));
        assertRepositoryListWithOneElement(response);
    }

    @Test
    public void deleteRepository_WithExistingRepository_DeletesRepository() throws Exception {
        CodeRepositoryMetadata repository = repositoryService.addRepository(TEAM_ID, REPOSITORY_NAME, REPOSITORY_URL, REPOSITORY_TYPE);
        Response response = RULE.withTeamOwnerUser().post("/api/repository/delete",
                new DeleteRepositoryRequest(repository.getId(), TEAM_ID));
        assertEmptyRepositoryList(response);
    }

    @Test
    public void deleteRepository_WithRepositoryOutsideOfTeam_YieldsForbidden() throws Exception {
        Team otherTeam = team().name("other-team").build();
        teamDao.insert(otherTeam);
        CodeRepositoryMetadata repository = repositoryService.addRepository(otherTeam.getId(), REPOSITORY_NAME,
                REPOSITORY_URL, REPOSITORY_TYPE);
        Response response = RULE.withTeamOwnerUser().post("/api/repository/delete",
                new DeleteRepositoryRequest(repository.getId(), otherTeam.getId()));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
        assertThat(repositoryService.findByTeam(otherTeam)).hasSize(1);
    }

    private void assertRepositoryListWithOneElement(Response response) {
        List<ReadRepositoryResponse> repositories = getRepositoryList(response);
        assertThat(repositories).hasSize(1);
        assertThat(repositories.get(0).getId()).isNotEmpty();
        assertThat(repositories.get(0).getName()).isEqualTo(REPOSITORY_NAME);
        assertThat(repositories.get(0).getSourceUrl()).isEqualTo(REPOSITORY_URL);
        assertThat(repositories.get(0).getType()).isEqualTo(REPOSITORY_TYPE);
    }

    private void assertEmptyRepositoryList(Response response) {
        List<ReadRepositoryResponse> repositories = getRepositoryList(response);
        assertThat(repositories).isEmpty();
    }

    private List<ReadRepositoryResponse> getRepositoryList(Response response) {
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadRepositoryResponse> repositories = response
                .readEntity(new GenericType<List<ReadRepositoryResponse>>() {
                });
        return repositories;
    }
}
