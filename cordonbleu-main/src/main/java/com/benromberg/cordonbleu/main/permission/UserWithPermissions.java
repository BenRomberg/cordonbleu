package com.benromberg.cordonbleu.main.permission;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserTeam;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class UserWithPermissions {
    private final Optional<User> user;
    private final List<UserTeam> teams;
    private final boolean admin;

    public UserWithPermissions(User user) {
        this.user = Optional.of(user);
        this.teams = user.getTeams();
        this.admin = user.isAdmin();
    }

    private UserWithPermissions() {
        this.user = Optional.empty();
        this.teams = emptyList();
        this.admin = false;
    }

    public static UserWithPermissions forAnonymousUser() {
        return new UserWithPermissions();
    }

    public User getUser() {
        return user.get();
    }

    public boolean isKnown() {
        return user.isPresent();
    }

    public boolean hasGlobalPermission(GlobalPermission permission) {
        return permission.grantTo(this);
    }

    public boolean hasTeamPermission(TeamPermission permission, Team team) {
        return permission.grantTo(this, team);
    }

    public Optional<UserTeam> hasTeamPermission(TeamPermission permission, String teamId) {
        return teams.stream().filter(userTeam -> userTeam.getTeam().getId().equals(teamId)).findAny()
                .filter(userTeam -> permission.grantTo(this, userTeam.getTeam()));
    }

    public Set<GlobalPermission> getGlobalPermissions() {
        return Stream.of(GlobalPermission.values()).filter(permission -> permission.grantTo(this)).collect(toSet());
    }

    public Set<TeamPermission> getTeamPermissions(Team team) {
        return Stream.of(TeamPermission.values()).filter(permission -> permission.grantTo(this, team)).collect(toSet());
    }

    public boolean belongsTo(Team team) {
        return teams.stream().map(UserTeam::getTeam).anyMatch(userTeam -> userTeam.equals(team));
    }

    public Optional<UserTeam> getTeam(Team team) {
        return teams.stream().filter(userTeam -> userTeam.getTeam().equals(team)).findFirst();
    }

    public List<UserTeam> getTeams() {
        return teams;
    }

    public boolean isAdmin() {
        return admin;
    }
}
