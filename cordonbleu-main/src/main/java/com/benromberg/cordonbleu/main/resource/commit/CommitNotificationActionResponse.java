package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.commit.CommitNotificationAction;
import com.benromberg.cordonbleu.service.commit.CommitNotificationActionType;

import java.time.LocalDateTime;

import com.benromberg.cordonbleu.main.resource.team.UserResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitNotificationActionResponse {
    private final CommitNotificationAction action;

    public CommitNotificationActionResponse(CommitNotificationAction action) {
        this.action = action;
    }

    @JsonProperty
    public UserResponse getUser() {
        return new UserResponse(action.getUser());
    }

    @JsonProperty
    public CommitNotificationActionType getType() {
        return action.getType();
    }

    @JsonProperty
    public LocalDateTime getTime() {
        return action.getTime();
    }

}
