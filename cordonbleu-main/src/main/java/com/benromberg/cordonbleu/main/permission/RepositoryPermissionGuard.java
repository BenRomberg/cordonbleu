package com.benromberg.cordonbleu.main.permission;

import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.service.team.TeamService;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

public class RepositoryPermissionGuard {
    private final TeamService teamService;

    @Inject
    public RepositoryPermissionGuard(TeamService teamService) {
        this.teamService = teamService;
    }

    public Team guardManageRepositories(UserWithPermissions user, String teamId) {
        Team team = teamService.findById(teamId).get();
        if (!user.hasTeamPermission(TeamPermission.MANAGE, team)) {
            throw new ForbiddenException();
        }
        return team;
    }
}
