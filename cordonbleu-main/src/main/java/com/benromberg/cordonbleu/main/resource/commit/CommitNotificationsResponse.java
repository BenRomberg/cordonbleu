package com.benromberg.cordonbleu.main.resource.commit;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.service.commit.CommitNotifications;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitNotificationsResponse {
    private final CommitNotifications notifications;

    public CommitNotificationsResponse(CommitNotifications notifications) {
        this.notifications = notifications;
    }

    @JsonProperty
    public int getTotalPrompts() {
        return notifications.getTotalPrompts();
    }

    @JsonProperty
    public List<CommitNotificationResponse> getNotifications() {
        return notifications.getNotifications().stream().map(CommitNotificationResponse::new).collect(toList());
    }
}
