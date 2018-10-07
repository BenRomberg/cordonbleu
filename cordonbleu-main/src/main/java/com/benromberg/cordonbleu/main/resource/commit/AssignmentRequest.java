package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.commit.RawCommitId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AssignmentRequest {
    @JsonProperty
    private final String hash;

    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final String userId;

    @JsonCreator
    public AssignmentRequest(String hash, String teamId, String userId) {
        this.hash = hash;
        this.teamId = teamId;
        this.userId = userId;
    }

    public RawCommitId getCommitId() {
        return new RawCommitId(hash, teamId);
    }

    public String getUserId() {
        return userId;
    }
}
