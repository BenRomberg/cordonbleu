package com.benromberg.cordonbleu.data.migration.change0014;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;
import com.benromberg.cordonbleu.data.migration.TestMigration;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0014.Change0014;

public class Change0014Test {
    private static final String TEAM_ID_2 = "team-id-2";
    private static final String TEAM_ID_1 = "team-id-1";
    private static final String OTHER_FIELD_1 = "other field 1";
    private static final String OTHER_FIELD_2 = "other field 2";
    private static final String TEAM_COLLECTION = "team";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0014.class);

    @Test
    public void teams_HaveKeyPairs() throws Exception {
        TestCollection<TeamBefore> collectionBefore = changeRule.getCollection(TEAM_COLLECTION, TeamBefore.class);
        collectionBefore.insert(new TeamBefore(TEAM_ID_1, OTHER_FIELD_1));
        collectionBefore.insert(new TeamBefore(TEAM_ID_2, OTHER_FIELD_2));

        changeRule.runChanges();

        TestCollection<TeamAfter> collectionAfter = changeRule.getCollection(TEAM_COLLECTION, TeamAfter.class);
        List<TeamAfter> teamsAfter = collectionAfter.find().toArray();
        assertThat(teamsAfter).extracting(TeamAfter::getOtherField, team -> team.getKeyPair().getPrivateKey(),
                team -> team.getKeyPair().getPublicKey()).containsOnly(
                tuple(OTHER_FIELD_1, TestMigration.PRIVATE_KEY, TestMigration.PUBLIC_KEY),
                tuple(OTHER_FIELD_2, TestMigration.PRIVATE_KEY, TestMigration.PUBLIC_KEY));
    }

}
