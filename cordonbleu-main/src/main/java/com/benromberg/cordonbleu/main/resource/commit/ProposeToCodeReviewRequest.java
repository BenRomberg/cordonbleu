package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.commit.RawCommitId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProposeToCodeReviewRequest {
    @JsonProperty
    private final String hash;

    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final boolean value;
    
    @JsonCreator
    public ProposeToCodeReviewRequest(String hash, String teamId, boolean value) {
        this.hash = hash;
        this.teamId = teamId;
        this.value=value;
    }

    public RawCommitId getCommitId() {
        return new RawCommitId(hash, teamId);
    }

	public boolean getValue() {
		return value;
	}
    
    
}
