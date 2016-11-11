package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.main.resource.team.ReadUserResponse;
import com.benromberg.cordonbleu.service.commit.CommitNotificationActionType;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitNotificationActionResponse {
    @JsonProperty
    private final ReadUserResponse user;

    @JsonProperty
    private final CommitNotificationActionType type;

    @JsonProperty
    private final LocalDateTime time;

    @JsonCreator
    public ReadCommitNotificationActionResponse(ReadUserResponse user, CommitNotificationActionType type,
            LocalDateTime time) {
        this.user = user;
        this.type = type;
        this.time = time;
    }

    public ReadUserResponse getUser() {
        return user;
    }

    public CommitNotificationActionType getType() {
        return type;
    }

    public LocalDateTime getTime() {
        return time;
    }

}
