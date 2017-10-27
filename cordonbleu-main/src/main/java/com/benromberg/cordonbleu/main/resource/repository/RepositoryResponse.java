package com.benromberg.cordonbleu.main.resource.repository;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RepositoryResponse {
    private CodeRepositoryMetadata repository;

    public RepositoryResponse(CodeRepositoryMetadata repository) {
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

    @JsonProperty
    public String getSourceUrl() {
        return repository.getSourceUrl().toString();
    }

    @JsonProperty
    public String getType() { return (repository.getType() == null)  ? "git" : repository.getType().toString(); }

}
