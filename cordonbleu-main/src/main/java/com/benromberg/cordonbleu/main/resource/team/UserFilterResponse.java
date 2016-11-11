package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.User;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserFilterResponse {
    private User user;

    public UserFilterResponse(User user) {
        this.user = user;
    }

    @JsonProperty
    public String getId() {
        return user.getId();
    }

    @JsonProperty
    public String getName() {
        return user.getName();
    }

    @JsonProperty
    public String getEmail() {
        return user.getEmail();
    }
}
