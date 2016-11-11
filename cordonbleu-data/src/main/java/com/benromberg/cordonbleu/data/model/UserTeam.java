package com.benromberg.cordonbleu.data.model;

import static java.util.Collections.emptyList;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserTeam {
    public static final String FLAGS_PROPERTY = "flags";
    public static final String TEAM_PROPERTY = "team";

    @JsonProperty(TEAM_PROPERTY)
    private final Team team;

    @JsonProperty(FLAGS_PROPERTY)
    private final List<UserTeamFlag> flags;

    @JsonCreator
    private UserTeam(@JsonProperty(TEAM_PROPERTY) Team team, @JsonProperty(FLAGS_PROPERTY) List<UserTeamFlag> flags) {
        this.team = team;
        this.flags = flags;
    }

    public UserTeam(Team team) {
        this(team, emptyList());
    }

    public Team getTeam() {
        return team;
    }

    public List<UserTeamFlag> getFlags() {
        return flags;
    }

    public boolean isOwner() {
        return flags.contains(UserTeamFlag.OWNER);
    }
}
