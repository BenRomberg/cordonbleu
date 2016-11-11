package com.benromberg.cordonbleu.data.migration.change0006;

import java.util.List;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final List<CommitRepositoryAfter> repositories;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public CommitAfter(String id, List<CommitRepositoryAfter> repositories, String otherField) {
        this.id = id;
        this.repositories = repositories;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public List<CommitRepositoryAfter> getRepositories() {
        return repositories;
    }

    public static class CommitRepositoryAfter {
        @JsonProperty
        private final String repository;

        @JsonProperty
        private final List<String> branches;

        @JsonCreator
        public CommitRepositoryAfter(String repository, List<String> branches) {
            this.repository = repository;
            this.branches = branches;
        }

        public String getRepository() {
            return repository;
        }

        public List<String> getBranches() {
            return branches;
        }

    }
}
