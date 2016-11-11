package com.benromberg.cordonbleu.main.permission;

import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.UserTeam;
import com.benromberg.cordonbleu.service.team.TeamService;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

public class TeamPermissionGuard {
    private final TeamService teamService;

    @Inject
    public TeamPermissionGuard(TeamService teamService) {
        this.teamService = teamService;
    }

    public Team guardManageMembers(UserWithPermissions user, String teamId) {
        return guardUpdateTeam(user, teamId).getTeam();
    }

    public UserTeam guardUpdateTeam(UserWithPermissions user, String teamId) {
        return user.hasTeamPermission(TeamPermission.MANAGE, teamId).orElseThrow(() -> new ForbiddenException());
    }

    public Team guardGetTeam(UserWithPermissions user, String name) {
        Optional<UserTeam> teamFromUser = user.getTeams().stream()
                .filter(userTeam -> userTeam.getTeam().getName().equals(name)).findAny();
        Team team = teamFromUser.map(UserTeam::getTeam).orElseGet(() -> teamService.findPublicTeamByName(name).get());
        if (!user.hasTeamPermission(TeamPermission.VIEW, team)) {
            throw new NotFoundException();
        }
        return team;
    }
}
