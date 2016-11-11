package com.benromberg.cordonbleu.data.migration.change0008;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HighlightCacheBefore {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public HighlightCacheBefore(String id, String otherField) {
        this.id = id;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

}
