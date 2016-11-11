package com.benromberg.cordonbleu.data.model;

import com.benromberg.cordonbleu.util.ClockService;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSession extends Entity<String> {
    @JsonProperty
    private final User user;

    @JsonProperty
    private final LocalDateTime created;

    @JsonCreator
    private UserSession(String id, User user, LocalDateTime created) {
        super(id);
        this.user = user;
        this.created = created;
    }

    public UserSession(String id, User user) {
        this(id, user, ClockService.now());
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCreated() {
        return created;
    }
}
