package com.benromberg.cordonbleu.main.resource.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitAuthorResponse {
    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    @JsonCreator
    public ReadCommitAuthorResponse(String name, String email) {
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
