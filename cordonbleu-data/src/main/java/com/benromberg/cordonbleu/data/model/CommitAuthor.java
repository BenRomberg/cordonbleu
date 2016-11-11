package com.benromberg.cordonbleu.data.model;

import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueDeserializer;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CommitAuthor {
    public static final String EMAIL_PROPERTY = "email";
    public static final String NAME_PROPERTY = "name";

    @JsonProperty(NAME_PROPERTY)
    private final String name;

    @JsonProperty(EMAIL_PROPERTY)
    @JsonSerialize(using = CaseInsensitiveUniqueSerializer.class)
    @JsonDeserialize(using = CaseInsensitiveUniqueDeserializer.class)
    private final String email;

    @JsonCreator
    public CommitAuthor(String name, String email) {
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
