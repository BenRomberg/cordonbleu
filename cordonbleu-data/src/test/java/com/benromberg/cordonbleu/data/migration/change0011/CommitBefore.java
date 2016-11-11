package com.benromberg.cordonbleu.data.migration.change0011;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitBefore {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final CommitAuthorBefore author;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public CommitBefore(String id, CommitAuthorBefore author, String otherField) {
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

    public CommitAuthorBefore getAuthor() {
        return author;
    }
}
