package com.benromberg.cordonbleu.main.resource.usermanagement;

import com.benromberg.cordonbleu.data.model.UserFlag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateFlagRequest {
    @JsonProperty
    private final String userId;

    @JsonProperty
    private final UserFlag flag;

    @JsonProperty
    private final boolean flagValue;

    @JsonCreator
    public UpdateFlagRequest(String userId, UserFlag flag, boolean flagValue) {
        this.userId = userId;
        this.flag = flag;
        this.flagValue = flagValue;
    }

    public String getUserId() {
        return userId;
    }

    public UserFlag getFlag() {
        return flag;
    }

    public boolean isFlagValue() {
        return flagValue;
    }

}
