package com.benromberg.cordonbleu.data.migration.change0015;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final boolean removed;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public CommitAfter(String id, boolean removed, String otherField) {
        this.id = id;
        this.removed = removed;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public boolean isRemoved() {
        return removed;
    }
}
