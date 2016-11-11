package com.benromberg.cordonbleu.data.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitRepository {
    public static final String REPOSITORY_PROPERTY = "repository";

    @JsonProperty(REPOSITORY_PROPERTY)
    private final CodeRepositoryMetadata repository;

    @JsonProperty
    private final List<String> branches;

    @JsonCreator
    public CommitRepository(CodeRepositoryMetadata repository, List<String> branches) {
        this.repository = repository;
        this.branches = branches;
    }

    public CodeRepositoryMetadata getRepository() {
        return repository;
    }

    public List<String> getBranches() {
        return branches;
    }

}
