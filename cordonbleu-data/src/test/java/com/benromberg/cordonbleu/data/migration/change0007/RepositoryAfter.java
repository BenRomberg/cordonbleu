package com.benromberg.cordonbleu.data.migration.change0007;

import java.util.List;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RepositoryAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final String otherField;

    @JsonProperty
    private final List<String> flags;

    @JsonCreator
    public RepositoryAfter(String id, String otherField, List<String> flags) {
        this.id = id;
        this.otherField = otherField;
        this.flags = flags;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public List<String> getFlags() {
        return flags;
    }

}
