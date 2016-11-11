package com.benromberg.cordonbleu.main.resource.team;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserTeam;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import com.benromberg.cordonbleu.service.team.TeamService;
import com.benromberg.cordonbleu.service.user.UserService;
import io.dropwizard.auth.Auth;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.benromberg.cordonbleu.main.permission.TeamPermissionGuard;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.codahale.metrics.annotation.Timed;

@Path("/team")
@Produces(MediaType.APPLICATION_JSON)
public class TeamResource {
    private final TeamService teamService;
    private final TeamPermissionGuard teamPermissionGuard;
    private final CodeRepositoryService codeRepositoryService;
    private final UserService userService;

    @Inject
    public TeamResource(TeamService teamService, TeamPermissionGuard teamPermissionGuard,
            CodeRepositoryService codeRepositoryService, UserService userService) {
        this.teamService = teamService;
        this.teamPermissionGuard = teamPermissionGuard;
        this.codeRepositoryService = codeRepositoryService;
        this.userService = userService;
    }

    @GET
    @Timed
    public ActiveTeamResponse getTeam(@Auth(required = false) UserWithPermissions user, @QueryParam("name") String name) {
        Team team = teamPermissionGuard.guardGetTeam(user, name);
        return new ActiveTeamResponse(team, user, getFilterResponse(team));
    }

    @POST
    @Timed
    public OwnUserResponse createTeam(@Auth UserWithPermissions user, CreateTeamRequest request) {
        User updatedUser = teamService.createTeam(request.getName(), request.getFlags(), user.getUser());
        return new OwnUserResponse(new UserWithPermissions(updatedUser));
    }

    @POST
    @Timed
    @Path("/update")
    public UserTeamResponse updateTeam(@Auth UserWithPermissions user, UpdateTeamRequest request) {
        UserTeam userTeam = teamPermissionGuard.guardUpdateTeam(user, request.getId());
        Team team = teamService.updateTeam(user.getUser(), userTeam.getTeam(), request.getName(), request.getFlags())
                .get();
        return new UserTeamResponse(team, user);
    }

    @GET
    @Timed
    @Path("/public")
    public List<TeamResponse> getPublicTeams() {
        return teamService.findPublicTeams().stream().map(TeamResponse::new).collect(toList());
    }

    @GET
    @Timed
    @Path("/members")
    public List<TeamMemberResponse> getMembers(@Auth UserWithPermissions user, @QueryParam("teamId") String teamId) {
        Team team = teamPermissionGuard.guardManageMembers(user, teamId);
        return findMembers(team);
    }

    @POST
    @Timed
    @Path("/members/add")
    public List<TeamMemberResponse> addMember(@Auth UserWithPermissions user, AddMemberRequest request) {
        Team team = teamPermissionGuard.guardManageMembers(user, request.getTeamId());
        teamService.addMember(team, request.getUserName());
        return findMembers(team);
    }

    @POST
    @Timed
    @Path("/members/remove")
    public List<TeamMemberResponse> removeMember(@Auth UserWithPermissions user, RemoveMemberRequest request) {
        Team team = teamPermissionGuard.guardManageMembers(user, request.getTeamId());
        teamService.removeMember(team, request.getUserId());
        return findMembers(team);
    }

    @POST
    @Timed
    @Path("/members/updateFlag")
    public List<TeamMemberResponse> updateFlagForMember(@Auth UserWithPermissions user,
            UpdateFlagForMemberRequest request) {
        Team team = teamPermissionGuard.guardManageMembers(user, request.getTeamId());
        teamService.updateMemberFlag(team, request.getUserId(), request.getFlag(), request.isFlagValue());
        return findMembers(team);
    }

    private FilterResponse getFilterResponse(Team team) {
        List<CodeRepositoryMetadata> repositories = codeRepositoryService.findByTeam(team);
        List<RepositoryFilterResponse> repositoryValues = repositories.stream()
                .map(repository -> new RepositoryFilterResponse(repository)).collect(toList());
        List<CommitAuthor> authors = codeRepositoryService.findTeamAuthors(team);
        List<CommitAuthorResponse> authorValues = authors.stream().map(author -> new CommitAuthorResponse(author))
                .collect(toList());
        List<UserFilterResponse> userValues = userService.findActiveTeamUsers(team).stream()
                .map(userValue -> new UserFilterResponse(userValue)).collect(toList());
        return new FilterResponse(repositoryValues, authorValues, userValues);
    }

    private List<TeamMemberResponse> findMembers(Team team) {
        return teamService.findMembers(team).stream().map(member -> new TeamMemberResponse(member, team))
                .collect(toList());
    }

}
