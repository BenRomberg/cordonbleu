package com.benromberg.cordonbleu.data.migration.change0010;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;
import com.benromberg.cordonbleu.data.migration.change0010.UserAfter.UserTeamAfter;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0010.Change0010;

public class Change0010Test {
    private static final String USER_ID_2 = "user-id-2";
    private static final String USER_ID_1 = "user-id-1";
    private static final String USER_COLLECTION = "user";
    private static final String TEAM_ID_2 = "team-id-2";
    private static final String TEAM_ID_1 = "team-id-1";
    private static final String OTHER_FIELD_1 = "other field 1";
    private static final String OTHER_FIELD_2 = "other field 2";
    private static final String TEAM_COLLECTION = "team";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0010.class);

    @Test
    public void teams_HaveEmptyFlags() throws Exception {
        createTeams();

        changeRule.runChanges();

        TestCollection<TeamAfter> collectionAfter = changeRule.getCollection(TEAM_COLLECTION, TeamAfter.class);
        List<TeamAfter> teamsAfter = collectionAfter.find().toArray();
        assertThat(teamsAfter).extracting(TeamAfter::getOtherField, TeamAfter::getFlags).containsOnly(
                tuple(OTHER_FIELD_1, asList()), tuple(OTHER_FIELD_2, asList()));
    }

    @Test
    public void users_AreOwnersOfAllTeams() throws Exception {
        createTeams();
        TestCollection<UserBefore> collectionBefore = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        collectionBefore.insert(new UserBefore(USER_ID_1, OTHER_FIELD_1));
        collectionBefore.insert(new UserBefore(USER_ID_2, OTHER_FIELD_2));

        changeRule.runChanges();

        TestCollection<UserAfter> collectionAfter = changeRule.getCollection(USER_COLLECTION, UserAfter.class);
        List<UserAfter> usersAfter = collectionAfter.find().toArray();
        assertThat(usersAfter).extracting(UserAfter::getOtherField).containsOnly(OTHER_FIELD_1, OTHER_FIELD_2);
        assertUserHasTeams(usersAfter.get(0));
        assertUserHasTeams(usersAfter.get(1));
    }

    private void assertUserHasTeams(UserAfter user) {
        assertThat(user.getTeams()).extracting(UserTeamAfter::getTeam, UserTeamAfter::getFlags).containsOnly(
                tuple(TEAM_ID_1, asList("OWNER")), tuple(TEAM_ID_2, asList("OWNER")));
    }

    private void createTeams() {
        TestCollection<TeamBefore> collectionBefore = changeRule.getCollection(TEAM_COLLECTION, TeamBefore.class);
        collectionBefore.insert(new TeamBefore(TEAM_ID_1, OTHER_FIELD_1));
        collectionBefore.insert(new TeamBefore(TEAM_ID_2, OTHER_FIELD_2));
    }

}
