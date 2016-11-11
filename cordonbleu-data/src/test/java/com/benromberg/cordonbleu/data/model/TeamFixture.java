package com.benromberg.cordonbleu.data.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.Set;

import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.TeamFlag;
import com.benromberg.cordonbleu.data.model.TeamKeyPair;

public interface TeamFixture {
    String TEAM_NAME = "team-name";
    String TEAM_PUBLIC_KEY = "team-public-key";
    String TEAM_PRIVATE_KEY = "team-private-key";
    Team TEAM = new TeamBuilder().build();
    String TEAM_ID = TEAM.getId();

    default TeamBuilder team() {
        return new TeamBuilder();
    }

    class TeamBuilder {
        private String name = TEAM_NAME;
        private Set<TeamFlag> flags = emptySet();
        private TeamKeyPair keyPair = new TeamKeyPair(TEAM_PRIVATE_KEY, TEAM_PUBLIC_KEY);

        public TeamBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamBuilder flags(TeamFlag... flags) {
            this.flags = new HashSet<>(asList(flags));
            return this;
        }

        public TeamBuilder keyPair(TeamKeyPair keyPair) {
            this.keyPair = keyPair;
            return this;
        }

        public Team build() {
            return new Team(name, flags, keyPair);
        }
    }
}
