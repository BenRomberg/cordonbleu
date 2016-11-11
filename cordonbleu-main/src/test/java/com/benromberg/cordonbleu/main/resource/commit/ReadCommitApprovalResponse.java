package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.main.resource.team.ReadUserResponse;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitApprovalResponse {
    @JsonProperty
    private ReadUserResponse approver;

    @JsonProperty
    private LocalDateTime time;

    @JsonCreator
    public ReadCommitApprovalResponse(ReadUserResponse approver, LocalDateTime time) {
        this.approver = approver;
        this.time = time;
    }

    public ReadUserResponse getApprover() {
        return approver;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
