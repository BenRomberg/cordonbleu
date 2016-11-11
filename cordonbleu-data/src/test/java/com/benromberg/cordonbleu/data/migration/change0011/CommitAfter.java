package com.benromberg.cordonbleu.data.migration.change0011;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final CommitAuthorAfter author;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public CommitAfter(String id, CommitAuthorAfter author, String otherField) {
        this.id = id;
        this.author = author;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public CommitAuthorAfter getAuthor() {
        return author;
    }
}
