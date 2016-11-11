package com.benromberg.cordonbleu.main.resource.team;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;

import com.benromberg.cordonbleu.main.permission.GlobalPermission;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OwnUserResponse extends UserResponse {
    private final UserWithPermissions userWithPermissions;

    public OwnUserResponse(UserWithPermissions userWithPermissions) {
        super(userWithPermissions.getUser());
        this.userWithPermissions = userWithPermissions;
    }

    @JsonProperty
    public Set<GlobalPermission> getGlobalPermissions() {
        return userWithPermissions.getGlobalPermissions();
    }

    @JsonProperty
    public List<UserTeamResponse> getTeams() {
        return userWithPermissions.getTeams().stream()
                .map(team -> new UserTeamResponse(team.getTeam(), userWithPermissions)).collect(toList());
    }
}
