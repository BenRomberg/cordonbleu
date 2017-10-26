package com.benromberg.cordonbleu.main.resource.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddRepositoryRequest {
    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String sourceUrl;

    @JsonProperty
    private final String type;

    @JsonCreator
    public AddRepositoryRequest(String teamId, String name, String sourceUrl, String type) {
        this.teamId = teamId;
        this.name = name;
        this.sourceUrl = sourceUrl;
        this.type=type;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getType() {
        return type;
    }

}
