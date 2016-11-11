package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.commit.CommitNotification;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitNotificationResponse {
    private final CommitNotification notification;

    public CommitNotificationResponse(CommitNotification notification) {
        this.notification = notification;
    }

    @JsonProperty
    public boolean isPrompt() {
        return notification.isPrompt();
    }

    @JsonProperty
    public CommitNotificationCommitResponse getCommit() {
        return new CommitNotificationCommitResponse(notification.getCommit());
    }

    @JsonProperty
    public CommitNotificationActionResponse getLastAction() {
        return new CommitNotificationActionResponse(notification.getLastAction());
    }
}
