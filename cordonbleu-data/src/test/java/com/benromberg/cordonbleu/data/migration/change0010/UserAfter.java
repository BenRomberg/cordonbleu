package com.benromberg.cordonbleu.data.migration.change0010;

import java.util.List;

import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAfter {
    @JsonProperty(MongoCommand.ID_PROPERTY)
    private final String id;

    @JsonProperty
    private final String otherField;

    @JsonProperty
    private final List<UserTeamAfter> teams;

    @JsonCreator
    public UserAfter(String id, String otherField, List<UserTeamAfter> teams) {
        this.id = id;
        this.otherField = otherField;
        this.teams = teams;
    }

    public String getId() {
        return id;
    }

    public String getOtherField() {
        return otherField;
    }

    public List<UserTeamAfter> getTeams() {
        return teams;
    }

    public static class UserTeamAfter {
        @JsonProperty
        private final String team;

        @JsonProperty
        private final List<String> flags;

        @JsonCreator
        public UserTeamAfter(String team, List<String> flags) {
            this.team = team;
            this.flags = flags;
        }

        public String getTeam() {
            return team;
        }

        public List<String> getFlags() {
            return flags;
        }
    }
}
