package com.benromberg.cordonbleu.data.migration.change0009;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitIdAfter {
    @JsonProperty
    private final String hash;

    @JsonProperty
    private final String team;

    @JsonCreator
    public CommitIdAfter(String hash, String team) {
        this.hash = hash;
        this.team = team;
    }

    public String getHash() {
        return hash;
    }

    public String getTeam() {
        return team;
    }
}