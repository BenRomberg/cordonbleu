package com.benromberg.cordonbleu.main.resource.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadUserFilterResponse {
    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    @JsonCreator
    public ReadUserFilterResponse(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
