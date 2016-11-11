package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.User;

import java.time.LocalDateTime;

public class CommitNotificationAction {
    private final User user;
    private final CommitNotificationActionType type;
    private final LocalDateTime time;

    public CommitNotificationAction(User user, CommitNotificationActionType type, LocalDateTime time) {
        this.user = user;
        this.type = type;
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public CommitNotificationActionType getType() {
        return type;
    }

    public LocalDateTime getTime() {
        return time;
    }

}
