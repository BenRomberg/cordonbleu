package com.benromberg.cordonbleu.main.resource.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadRepositoryFilterResponse {
    @JsonProperty
    private final String id;

    @JsonProperty
    private final String name;

    @JsonCreator
    public ReadRepositoryFilterResponse(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
