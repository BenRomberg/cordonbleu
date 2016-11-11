package com.benromberg.cordonbleu.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitId {
    public static final String TEAM_PROPERTY = "team";
    public static final String HASH_PROPERTY = "hash";

    @JsonProperty(HASH_PROPERTY)
    private final String hash;

    @JsonProperty(TEAM_PROPERTY)
    private final Team team;

    @JsonCreator
    public CommitId(@JsonProperty(HASH_PROPERTY) String hash, @JsonProperty(TEAM_PROPERTY) Team team) {
        this.hash = hash;
        this.team = team;
    }

    public String getHash() {
        return hash;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public int hashCode() {
        return 31 * (31 + hash.hashCode()) + team.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!getClass().equals(object.getClass())) {
            return false;
        }
        CommitId other = (CommitId) object;
        return hash.equals(other.hash) && team.equals(other.team);
    }

}
