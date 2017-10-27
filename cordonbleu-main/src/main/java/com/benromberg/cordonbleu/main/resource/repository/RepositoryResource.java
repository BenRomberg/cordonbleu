package com.benromberg.cordonbleu.main.resource.repository;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import io.dropwizard.auth.Auth;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.benromberg.cordonbleu.main.permission.RepositoryPermissionGuard;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.codahale.metrics.annotation.Timed;

@Path("/repository")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryResource {
    private final CodeRepositoryService repositoryService;
    private final RepositoryPermissionGuard repositoryPermissionGuard;

    @Inject
    public RepositoryResource(CodeRepositoryService repositoryService,
            RepositoryPermissionGuard repositoryPermissionGuard) {
        this.repositoryService = repositoryService;
        this.repositoryPermissionGuard = repositoryPermissionGuard;
    }

    @GET
    @Path("/list")
    @Timed
    public List<RepositoryResponse> getRepositories(@Auth UserWithPermissions user, @QueryParam("teamId") String teamId) {
        Team team = repositoryPermissionGuard.guardManageRepositories(user, teamId);
        return findRepositories(team);
    }

    @POST
    @Path("/add")
    @Timed
    public List<RepositoryResponse> addRepository(@Auth UserWithPermissions user, AddRepositoryRequest request) {
        Team team = repositoryPermissionGuard.guardManageRepositories(user, request.getTeamId());
        repositoryService.addRepository(request.getTeamId(), request.getName(), request.getSourceUrl(), request.getType());
        return findRepositories(team);
    }

    @POST
    @Path("/delete")
    @Timed
    public List<RepositoryResponse> deleteRepository(@Auth UserWithPermissions user, DeleteRepositoryRequest request) {
        Team team = repositoryPermissionGuard.guardManageRepositories(user, request.getTeamId());
        repositoryService.removeRepository(request.getId());
        return findRepositories(team);
    }

    private List<RepositoryResponse> findRepositories(Team team) {
        return repositoryService.findByTeam(team).stream().map(repository -> new RepositoryResponse(repository))
                .collect(toList());
    }
}
