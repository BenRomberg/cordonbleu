package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.TeamFlag;

import java.util.Set;

import com.benromberg.cordonbleu.main.permission.TeamPermission;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserTeamResponse extends TeamResponse {
    private final Set<TeamPermission> permissions;
    private final Team team;

    public UserTeamResponse(Team team, UserWithPermissions user) {
        super(team);
        this.team = team;
        this.permissions = user.getTeamPermissions(team);
    }

    @JsonProperty
    public Set<TeamPermission> getPermissions() {
        return permissions;
    }

    @JsonProperty
    public Set<TeamFlag> getFlags() {
        return team.getFlags();
    }

    @JsonProperty
    public String getPublicKey() {
        return team.getKeyPair().getPublicKey();
    }
}
