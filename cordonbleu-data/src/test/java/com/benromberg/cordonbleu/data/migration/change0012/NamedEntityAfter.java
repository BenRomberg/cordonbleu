package com.benromberg.cordonbleu.data.migration.change0012;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueDeserializer;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class NamedEntityAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    @JsonSerialize(using = CaseInsensitiveUniqueSerializer.class)
    @JsonDeserialize(using = CaseInsensitiveUniqueDeserializer.class)
    private final String name;

    @JsonProperty
    private final String otherField;

    @JsonCreator
    public NamedEntityAfter(String id, String name, String otherField) {
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
