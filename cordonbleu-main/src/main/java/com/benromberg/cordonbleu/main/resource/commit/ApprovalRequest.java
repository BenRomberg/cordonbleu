package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.commit.RawCommitId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApprovalRequest {
    @JsonProperty
    private final String hash;

    @JsonProperty
    private final String teamId;

    @JsonCreator
    public ApprovalRequest(String hash, String teamId) {
        this.hash = hash;
        this.teamId = teamId;
    }

    public RawCommitId getCommitId() {
        return new RawCommitId(hash, teamId);
    }
}
