package com.benromberg.cordonbleu.main.resource.commit;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitNotificationsResponse {
    @JsonProperty
    private final int totalPrompts;

    @JsonProperty
    private final List<ReadCommitNotificationResponse> notifications;

    @JsonCreator
    public ReadCommitNotificationsResponse(int totalPrompts, List<ReadCommitNotificationResponse> notifications) {
        this.totalPrompts = totalPrompts;
        this.notifications = notifications;
    }

    public int getTotalPrompts() {
        return totalPrompts;
    }

    public List<ReadCommitNotificationResponse> getNotifications() {
        return notifications;
    }

}
