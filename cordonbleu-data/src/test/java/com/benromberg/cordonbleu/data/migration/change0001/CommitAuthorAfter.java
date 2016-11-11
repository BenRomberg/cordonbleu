package com.benromberg.cordonbleu.data.migration.change0001;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAuthorAfter {
    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

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
