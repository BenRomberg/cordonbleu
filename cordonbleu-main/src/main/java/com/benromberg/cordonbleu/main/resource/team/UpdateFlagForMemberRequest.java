package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.UserTeamFlag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateFlagForMemberRequest extends RemoveMemberRequest {
    @JsonProperty
    private final UserTeamFlag flag;

    @JsonProperty
    private final boolean flagValue;

    @JsonCreator
    public UpdateFlagForMemberRequest(String teamId, String userId, UserTeamFlag flag, boolean flagValue) {
        super(teamId, userId);
        this.flag = flag;
        this.flagValue = flagValue;
    }

    public UserTeamFlag getFlag() {
        return flag;
    }

    public boolean isFlagValue() {
        return flagValue;
    }
}
