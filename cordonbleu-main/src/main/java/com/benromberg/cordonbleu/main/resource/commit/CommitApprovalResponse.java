package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.CommitApproval;

import java.time.LocalDateTime;

import com.benromberg.cordonbleu.main.resource.team.UserResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitApprovalResponse {
    private CommitApproval approval;

    public CommitApprovalResponse(CommitApproval approval) {
        this.approval = approval;
    }

    @JsonProperty
    public UserResponse getApprover() {
        return new UserResponse(approval.getApprover());
    }

    @JsonProperty
    public LocalDateTime getTime() {
        return approval.getTime();
    }

}
