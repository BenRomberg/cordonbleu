package com.benromberg.cordonbleu.data.migration.change0011;

import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueDeserializer;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CommitAuthorAfter {
    @JsonProperty
    private final String name;

    @JsonProperty
    @JsonSerialize(using = CaseInsensitiveUniqueSerializer.class)
    @JsonDeserialize(using = CaseInsensitiveUniqueDeserializer.class)
    private final String email;

    @JsonCreator
    public CommitAuthorAfter(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
