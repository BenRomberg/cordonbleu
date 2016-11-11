package com.benromberg.cordonbleu.data.migration.change0012;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NamedEntityBefore {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public NamedEntityBefore(String id, String name, String otherField) {
        this.id = id;
        this.name = name;
        this.otherField = otherField;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public String getName() {
        return name;
    }
}
