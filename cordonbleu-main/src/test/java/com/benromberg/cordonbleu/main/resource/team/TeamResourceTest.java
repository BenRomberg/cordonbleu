package com.benromberg.cordonbleu.main.resource.team;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.RepositoryFixture;
import com.benromberg.cordonbleu.data.model.TeamFlag;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFixture;
import com.benromberg.cordonbleu.data.model.UserTeamFlag;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;

import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.main.permission.TeamPermission;
import com.benromberg.cordonbleu.main.resource.team.AddMemberRequest;
import com.benromberg.cordonbleu.main.resource.team.CreateTeamRequest;
import com.benromberg.cordonbleu.main.resource.team.RemoveMemberRequest;
import com.benromberg.cordonbleu.main.resource.team.UpdateFlagForMemberRequest;
import com.benromberg.cordonbleu.main.resource.team.UpdateTeamRequest;

public class TeamResourceTest implements RepositoryFixture, UserFixture {
    private static final String NEW_TEAM_NAME = "new-team-name";

    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule().withTeam();

    private final UserDao userDao = RULE.getInstance(UserDao.class);
    private final TeamDao teamDao = RULE.getInstance(TeamDao.class);

    @Test
    public void getTeam_WithWrongName_YieldsNotFound() throws Exception {
        Response response = RULE.request().param("name", "wrong team name").get("/api/team");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void getTeam_WithPublicTeamAndNotLoggedIn_ReturnsTeam() throws Exception {
        Response response = RULE.request().param("name", TEAM_NAME).get("/api/team");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadActiveTeamResponse team = response.readEntity(ReadActiveTeamResponse.class);
        assertThat(team.getId()).isEqualTo(TEAM_ID);
        assertThat(team.getName()).isEqualTo(TEAM_NAME);
        assertThat(team.getPermissions()).containsOnly(TeamPermission.VIEW);
        assertThat(team.getFlags()).isEmpty();
        assertThat(team.getPublicKey()).isEqualTo(TEAM_PUBLIC_KEY);
    }

    @Test
    public void getTeam_WithPublicTeamAndLoggedIn_ReturnsCommentableAndApprovableTeam() throws Exception {
        Response response = RULE.withAuthenticatedUser().param("name", TEAM_NAME).get("/api/team");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadActiveTeamResponse team = response.readEntity(ReadActiveTeamResponse.class);
        assertThat(team.getPermissions()).containsOnly(TeamPermission.VIEW, TeamPermission.APPROVE,
                TeamPermission.COMMENT);
    }

    @Test
    public void getTeam_WithPrivateTeamAndNotLoggedIn_YieldsNotFound() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.request().param("name", TEAM_NAME).get("/api/team");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void getTeam_WithPrivateTeamAndAuthenticatedUser_YieldsNotFound() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.withAuthenticatedUser().param("name", TEAM_NAME).get("/api/team");
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void getTeam_WithPrivateTeamAndTeamMember_ReturnsTeam() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.withTeamUser().param("name", TEAM_NAME).get("/api/team");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadActiveTeamResponse team = response.readEntity(ReadActiveTeamResponse.class);
        assertThat(team.getId()).isEqualTo(TEAM_ID);
        assertThat(team.getName()).isEqualTo(TEAM_NAME);
        assertThat(team.getPermissions()).containsOnly(TeamPermission.VIEW, TeamPermission.COMMENT,
                TeamPermission.APPROVE);
        assertThat(team.getFlags()).containsOnly(TeamFlag.PRIVATE);
    }

    @Test
    public void updateTeam_WithTeamUser_YieldsForbidden() throws Exception {
        Response response = RULE.withTeamUser().post("/api/team/update",
                new UpdateTeamRequest(TEAM_ID, NEW_TEAM_NAME, emptySet()));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void updateTeam_WithTeamOwner_ReturnsNewTeam() throws Exception {
        Response response = RULE.withTeamOwnerUser().post("/api/team/update",
                new UpdateTeamRequest(TEAM_ID, NEW_TEAM_NAME, emptySet()));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadUserTeamResponse team = response.readEntity(ReadUserTeamResponse.class);
        assertThat(team.getName()).isEqualTo(NEW_TEAM_NAME);
        assertThat(team.getPermissions()).containsOnly(TeamPermission.values());
    }

    @Test
    public void updateTeam_WithTeamOwner_ForcesUserCacheInvalidation() throws Exception {
        RULE.withTeamOwnerUser().post("/api/team/update", new UpdateTeamRequest(TEAM_ID, NEW_TEAM_NAME, emptySet()));
        User user = userDao.findById(RULE.getAuthenticatedUser().getId()).get();
        assertThat(user.getTeams().get(0).getTeam().getName()).isEqualTo(NEW_TEAM_NAME);
    }

    @Test
    public void updateTeam_WithFlag_ReturnsNewTeamHavingFlag() throws Exception {
        Response response = RULE.withTeamOwnerUser().post("/api/team/update",
                new UpdateTeamRequest(TEAM_ID, NEW_TEAM_NAME, singleton(TeamFlag.PRIVATE)));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadUserTeamResponse team = response.readEntity(ReadUserTeamResponse.class);
        assertThat(team.getFlags()).containsExactly(TeamFlag.PRIVATE);
    }

    @Test
    public void updateTeam_WithSameNameAsExisting_YieldsConflict() throws Exception {
        teamDao.insert(team().name(NEW_TEAM_NAME).build());
        Response response = RULE.withTeamOwnerUser().post("/api/team/update",
                new UpdateTeamRequest(TEAM_ID, NEW_TEAM_NAME, emptySet()));
        assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());
    }

    @Test
    public void createTeam_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.request().post("/api/team", new CreateTeamRequest(NEW_TEAM_NAME, emptySet()));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void createTeam_ReturnsNewTeam() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/team",
                new CreateTeamRequest(NEW_TEAM_NAME, emptySet()));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadOwnUserResponse user = response.readEntity(ReadOwnUserResponse.class);
        assertThat(user.getTeams()).extracting(ReadUserTeamResponse::getName).containsExactly(NEW_TEAM_NAME);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createTeam_WithFlag_ReturnsNewTeamHavingFlag() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/team",
                new CreateTeamRequest(NEW_TEAM_NAME, singleton(TeamFlag.PRIVATE)));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        ReadOwnUserResponse user = response.readEntity(ReadOwnUserResponse.class);
        assertThat(user.getTeams()).extracting(ReadUserTeamResponse::getFlags).containsExactly(
                singleton(TeamFlag.PRIVATE));
    }

    @Test
    public void createTeam_WithSameNameAsExisting_YieldsConflict() throws Exception {
        Response response = RULE.withAuthenticatedUser()
                .post("/api/team", new CreateTeamRequest(TEAM_NAME, emptySet()));
        assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());
    }

    @Test
    public void createdTeam_CanBeFound() throws Exception {
        RULE.withAuthenticatedUser().post("/api/team", new CreateTeamRequest(NEW_TEAM_NAME, emptySet()));
        Response response = RULE.request().param("name", NEW_TEAM_NAME).get("/api/team");
        ReadActiveTeamResponse team = response.readEntity(ReadActiveTeamResponse.class);
        assertThat(team.getName()).isEqualTo(NEW_TEAM_NAME);
    }

    @Test
    public void createdTeam_HasCurrentUserAsOwner() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/team",
                new CreateTeamRequest(NEW_TEAM_NAME, emptySet()));
        ReadOwnUserResponse user = response.readEntity(ReadOwnUserResponse.class);
        assertThat(user.getTeams()).extracting(ReadUserTeamResponse::getName, ReadUserTeamResponse::getPermissions)
                .containsExactly(tuple(NEW_TEAM_NAME, new HashSet<>(asList(TeamPermission.values()))));
    }

    @Test
    public void getPublicTeams_WithPublicTeam_ReturnsTeam() throws Exception {
        Response response = RULE.request().get("/api/team/public");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadTeamResponse> teams = response.readEntity(new GenericType<List<ReadTeamResponse>>() {
        });
        assertThat(teams).extracting(ReadTeamResponse::getId).containsExactly(TEAM_ID);
    }

    @Test
    public void getPublicTeams_WithPrivateTeam_ReturnsEmptyList() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        Response response = RULE.request().get("/api/team/public");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadTeamResponse> teams = response.readEntity(new GenericType<List<ReadTeamResponse>>() {
        });
        assertThat(teams).isEmpty();
    }

    @Test
    public void getMembers_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.request().param("teamId", TEAM_ID).get("/api/team/members");
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void getMembers_WithoutTeamOwnerUser_YieldsForbidden() throws Exception {
        Response response = RULE.withTeamUser().param("teamId", TEAM_ID).get("/api/team/members");
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void getMembers_ReturnsTeamMembers() throws Exception {
        Response response = RULE.withTeamOwnerUser().param("teamId", TEAM_ID).get("/api/team/members");
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadTeamMemberResponse> members = response.readEntity(new GenericType<List<ReadTeamMemberResponse>>() {
        });
        assertThat(members).extracting(ReadTeamMemberResponse::getName).containsExactly(CordonBleuTestRule.USER_NAME);
    }

    @Test
    public void addMember_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.request().post("/api/team/members/add", new AddMemberRequest(TEAM_ID, USER_NAME));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void addMember_WithoutTeamOwnerUser_YieldsForbidden() throws Exception {
        Response response = RULE.withTeamUser().post("/api/team/members/add", new AddMemberRequest(TEAM_ID, USER_NAME));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void addMembers_WithNonExistentUser_YieldsNotFound() throws Exception {
        userDao.insert(USER);
        Response response = RULE.withTeamOwnerUser().post("/api/team/members/add",
                new AddMemberRequest(TEAM_ID, "nonexisting-user"));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addMembers_ReturnsUpdatedTeamMembers() throws Exception {
        userDao.insert(USER);
        Response response = RULE.withTeamOwnerUser().post("/api/team/members/add",
                new AddMemberRequest(TEAM_ID, USER_NAME));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadTeamMemberResponse> members = response.readEntity(new GenericType<List<ReadTeamMemberResponse>>() {
        });
        assertThat(members).extracting(ReadTeamMemberResponse::getName).containsExactly(CordonBleuTestRule.USER_NAME,
                USER_NAME);
    }

    @Test
    public void removeMember_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.request().post("/api/team/members/remove", new RemoveMemberRequest(TEAM_ID, USER_ID));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void removeMember_WithoutTeamOwnerUser_YieldsForbidden() throws Exception {
        Response response = RULE.withTeamUser().post("/api/team/members/remove",
                new RemoveMemberRequest(TEAM_ID, USER_ID));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void removeMembers_ReturnsUpdatedTeamMembers() throws Exception {
        userDao.insert(USER);
        userDao.addTeam(USER_ID, TEAM);
        Response response = RULE.withTeamOwnerUser().post("/api/team/members/remove",
                new RemoveMemberRequest(TEAM_ID, USER_ID));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadTeamMemberResponse> members = response.readEntity(new GenericType<List<ReadTeamMemberResponse>>() {
        });
        assertThat(members).extracting(ReadTeamMemberResponse::getName).containsExactly(CordonBleuTestRule.USER_NAME);
    }

    @Test
    public void updateFlagForMember_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.request().post("/api/team/members/updateFlag",
                new UpdateFlagForMemberRequest(TEAM_ID, USER_ID, UserTeamFlag.OWNER, true));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void updateFlagForMember_WithoutTeamOwnerUser_YieldsForbidden() throws Exception {
        Response response = RULE.withTeamUser().post("/api/team/members/updateFlag",
                new UpdateFlagForMemberRequest(TEAM_ID, USER_ID, UserTeamFlag.OWNER, true));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void updateFlagForMembers_ReturnsUpdatedTeamMembers() throws Exception {
        userDao.insert(USER);
        userDao.addTeam(USER_ID, TEAM);
        Response response = RULE.withTeamOwnerUser().post("/api/team/members/updateFlag",
                new UpdateFlagForMemberRequest(TEAM_ID, USER_ID, UserTeamFlag.OWNER, true));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadTeamMemberResponse> members = response.readEntity(new GenericType<List<ReadTeamMemberResponse>>() {
        });
        assertThat(members).extracting(ReadTeamMemberResponse::isOwner).containsExactly(true, true);
    }
}
