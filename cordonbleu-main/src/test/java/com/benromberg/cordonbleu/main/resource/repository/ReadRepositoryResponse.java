package com.benromberg.cordonbleu.main.resource.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadRepositoryResponse {
    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String sourceUrl;

    @JsonCreator
    public ReadRepositoryResponse(String id, String name, String sourceUrl) {
        this.id = id;
        this.name = name;
        this.sourceUrl = sourceUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

}
