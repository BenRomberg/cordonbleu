package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.TeamFlag;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateTeamRequest extends CreateTeamRequest {
    @JsonProperty
    private final String id;

    @JsonCreator
    public UpdateTeamRequest(String id, String name, Set<TeamFlag> flags) {
        super(name, flags);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
