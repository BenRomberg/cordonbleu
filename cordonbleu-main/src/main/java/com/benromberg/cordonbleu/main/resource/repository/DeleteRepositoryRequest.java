package com.benromberg.cordonbleu.main.resource.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteRepositoryRequest {
    @JsonProperty
    private final String id;

    @JsonProperty
    private final String teamId;

    @JsonCreator
    public DeleteRepositoryRequest(String id, String teamId) {
        this.id = id;
        this.teamId = teamId;
    }

    public String getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
    }
}
