package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.TeamFlag;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateTeamRequest {
    @JsonProperty
    private final String name;

    @JsonProperty
    private final Set<TeamFlag> flags;

    @JsonCreator
    public CreateTeamRequest(String name, Set<TeamFlag> flags) {
        this.name = name;
        this.flags = flags;
    }

    public String getName() {
        return name;
    }

    public Set<TeamFlag> getFlags() {
        return flags;
    }
}
