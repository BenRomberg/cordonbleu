package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.Team;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamResponse {
    private final Team team;

    public TeamResponse(Team team) {
        this.team = team;
    }

    @JsonProperty
    public String getId() {
        return team.getId();
    }

    @JsonProperty
    public String getName() {
        return team.getName();
    }
}
