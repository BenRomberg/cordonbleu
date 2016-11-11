package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.Team;

import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ActiveTeamResponse extends UserTeamResponse {
    private final FilterResponse filters;

    public ActiveTeamResponse(Team team, UserWithPermissions user, FilterResponse filters) {
        super(team, user);
        this.filters = filters;
    }

    @JsonProperty
    public FilterResponse getFilters() {
        return filters;
    }
}
