package com.benromberg.cordonbleu.main.resource;

import com.benromberg.cordonbleu.service.commit.RawCommitId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RevertAssignmentRequest {
    @JsonProperty
    private final String hash;

    @JsonProperty
    private final String teamId;

    @JsonCreator
    public RevertAssignmentRequest(String hash, String teamId) {
        this.hash = hash;
        this.teamId = teamId;
    }

    public RawCommitId getCommitId() {
        return new RawCommitId(hash, teamId);
    }

}
