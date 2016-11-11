package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserTeam;

import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamMemberResponse extends UserResponse {
    private final UserTeam team;

    public TeamMemberResponse(User user, Team team) {
        super(user);
        this.team = user.getTeams().stream().filter(teamFilter(team)).findAny().get();
    }

    // using a lambda leads to issues with Jackson
    private Predicate<UserTeam> teamFilter(Team team) {
        return new Predicate<UserTeam>() {
            @Override
            public boolean test(UserTeam userTeam) {
                return userTeam.getTeam().equals(team);
            }
        };
    }

    @JsonProperty
    public boolean isOwner() {
        return team.isOwner();
    }
}
