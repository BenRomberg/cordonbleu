package com.benromberg.cordonbleu.main.resource.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddMemberRequest {
    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final String userName;

    @JsonCreator
    public AddMemberRequest(String teamId, String userName) {
        this.teamId = teamId;
        this.userName = userName;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getUserName() {
        return userName;
    }
}
