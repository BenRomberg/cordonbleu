package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.CommitRepository;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitRepositoryResponse {
    private final CommitRepository repository;

    public CommitRepositoryResponse(CommitRepository repository) {
        this.repository = repository;
    }

    @JsonProperty
    public String getName() {
        return repository.getRepository().getName();
    }

    @JsonProperty
    public List<String> getBranches() {
        return repository.getBranches();
    }
}
