package com.benromberg.cordonbleu.data.migration.change0006;

import java.util.List;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitBefore {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final String repository;

    @JsonProperty
    private final String otherField;

    @JsonProperty
    private final List<String> branches;

    @JsonCreator
    public CommitBefore(String id, String repository, String otherField, List<String> branches) {
        this.id = id;
        this.repository = repository;
        this.otherField = otherField;
        this.branches = branches;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public String getRepository() {
        return repository;
    }

    public List<String> getBranches() {
        return branches;
    }
}
