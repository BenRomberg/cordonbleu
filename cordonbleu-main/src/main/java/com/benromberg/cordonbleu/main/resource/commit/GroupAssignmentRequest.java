package com.benromberg.cordonbleu.main.resource.commit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroupAssignmentRequest {

    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final List<String> userIds;

    @JsonCreator
    public GroupAssignmentRequest(String teamId, List<String> userIds) {
        this.teamId = teamId;
        this.userIds = userIds;
    }

    public String getTeamId() {
        return teamId;
    }

    public List<String> getUserIds() {
        return userIds;
    }
}
