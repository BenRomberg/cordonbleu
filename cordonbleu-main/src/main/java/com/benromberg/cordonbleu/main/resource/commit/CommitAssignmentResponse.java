package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.data.model.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitAssignmentResponse {
    private User assignee;

    @JsonCreator
    public CommitAssignmentResponse(User assignee) {
        this.assignee = assignee;
    }

    @JsonProperty
    public User getAssignee() {
        return assignee;
    }
}
