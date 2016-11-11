package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.User;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse {
    private final User user;

    public UserResponse(User user) {
        this.user = user;
    }

    @JsonProperty
    public String getId() {
        return user.getId();
    }

    @JsonProperty
    public String getEmail() {
        return user.getEmail();
    }

    @JsonProperty
    public String getName() {
        return user.getName();
    }

    @JsonProperty
    public List<String> getEmailAliases() {
        return user.getEmailAliases();
    }

    @JsonProperty
    public boolean isAdmin() {
        return user.isAdmin();
    }

    @JsonProperty
    public boolean isInactive() {
        return user.isInactive();
    }
}
