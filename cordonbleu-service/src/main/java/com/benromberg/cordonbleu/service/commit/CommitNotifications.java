package com.benromberg.cordonbleu.service.commit;

import java.util.List;

public class CommitNotifications {
    private final int totalPrompts;
    private final List<CommitNotification> notifications;

    public CommitNotifications(int totalPrompts, List<CommitNotification> notifications) {
        this.totalPrompts = totalPrompts;
        this.notifications = notifications;
    }

    public int getTotalPrompts() {
        return totalPrompts;
    }

    public List<CommitNotification> getNotifications() {
        return notifications;
    }

}
