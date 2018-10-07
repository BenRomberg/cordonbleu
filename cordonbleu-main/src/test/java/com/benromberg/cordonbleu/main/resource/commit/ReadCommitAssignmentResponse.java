package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.main.resource.team.ReadUserResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitAssignmentResponse {
    @JsonProperty
    private ReadUserResponse assignee;

    @JsonCreator
    public ReadCommitAssignmentResponse(ReadUserResponse assignee) {
        this.assignee = assignee;
    }

    public ReadUserResponse getAssignee() {
        return assignee;
    }

}
