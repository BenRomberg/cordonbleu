package com.benromberg.cordonbleu.main.resource.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRequest {
    @JsonProperty
    private final String name;

    @JsonProperty
    private final String email;

    @JsonProperty
    private final List<String> emailAliases;

    @JsonCreator
    public UserRequest(String name, String email, List<String> emailAliases) {
        this.name = name;
        this.email = email;
        this.emailAliases = emailAliases;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getEmailAliases() {
        return emailAliases;
    }

}
