package com.benromberg.cordonbleu.data.migration.change0001;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitBefore {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private String id;

    @JsonProperty
    private String author;

    @JsonProperty
    private String authorEmail;

    @JsonProperty
    private String otherField;

    @JsonCreator
    public CommitBefore(String id, String author, String authorEmail, String otherField) {
        this.id = id;
        this.author = author;
        this.authorEmail = authorEmail;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getOtherField() {
        return otherField;
    }

}
