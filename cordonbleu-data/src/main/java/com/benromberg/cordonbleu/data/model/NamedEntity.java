package com.benromberg.cordonbleu.data.model;

import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueDeserializer;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class NamedEntity extends Entity<String> {
    public static final String NAME_PROPERTY = "name";

    @JsonProperty(NAME_PROPERTY)
    @JsonSerialize(using = CaseInsensitiveUniqueSerializer.class)
    @JsonDeserialize(using = CaseInsensitiveUniqueDeserializer.class)
    private final String name;

    public NamedEntity(String id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
