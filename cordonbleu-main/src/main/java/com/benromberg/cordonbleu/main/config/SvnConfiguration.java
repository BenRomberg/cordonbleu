package com.benromberg.cordonbleu.main.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SvnConfiguration {

    @JsonProperty
    private final String user;

    @JsonProperty
    private final String password;

    @JsonCreator
    public SvnConfiguration(final String user, final String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
