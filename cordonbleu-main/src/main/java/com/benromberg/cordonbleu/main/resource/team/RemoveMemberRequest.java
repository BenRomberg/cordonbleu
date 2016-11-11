package com.benromberg.cordonbleu.main.resource.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoveMemberRequest {
    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final String userId;

    @JsonCreator
    public RemoveMemberRequest(String teamId, String userId) {
        this.teamId = teamId;
        this.userId = userId;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getUserId() {
        return userId;
    }
}
