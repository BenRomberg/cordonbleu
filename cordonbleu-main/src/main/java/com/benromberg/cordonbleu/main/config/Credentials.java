package com.benromberg.cordonbleu.main.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Credentials {
    @JsonProperty
    private final String username;

    @JsonProperty
    private final String password;

    @JsonCreator
    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
