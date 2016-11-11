package com.benromberg.cordonbleu.data.migration.change0009;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final CommitIdAfter id;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public CommitAfter(CommitIdAfter id, String otherField) {
        this.id = id;
        this.otherField = otherField;
    }

    public CommitIdAfter getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }
}
