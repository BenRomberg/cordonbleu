package com.benromberg.cordonbleu.data.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitApproval {
    @JsonProperty
    private User approver;

    @JsonProperty
    private LocalDateTime time;

    @JsonCreator
    public CommitApproval(User approver, LocalDateTime time) {
        this.approver = approver;
        this.time = time;
    }

    public User getApprover() {
        return approver;
    }

    public LocalDateTime getTime() {
        return time;
    }

}
