package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RepositoryFilterResponse {
    private final CodeRepositoryMetadata repository;

    public RepositoryFilterResponse(CodeRepositoryMetadata repository) {
        this.repository = repository;
    }

    @JsonProperty
    public String getId() {
        return repository.getId();
    }

    @JsonProperty
    public String getName() {
        return repository.getName();
    }

}
