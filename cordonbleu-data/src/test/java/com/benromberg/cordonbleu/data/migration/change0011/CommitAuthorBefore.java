package com.benromberg.cordonbleu.data.migration.change0011;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAuthorBefore {
    @JsonProperty
    private final String name;

    @JsonProperty
    private final String email;

    @JsonCreator
    public CommitAuthorBefore(String name, String email) {
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
