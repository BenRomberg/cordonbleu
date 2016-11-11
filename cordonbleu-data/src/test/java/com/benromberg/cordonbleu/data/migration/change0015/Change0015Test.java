package com.benromberg.cordonbleu.data.migration.change0015;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0015.Change0015;

public class Change0015Test {
    private static final String COMMIT_COLLECTION = "commit";
    private static final String OTHER_FIELD = "other field";
    private static final String COMMIT_ID = "commit-id";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0015.class);

    @Test
    public void convertUniqueFields_CanReadValues() throws Exception {
        TestCollection<CommitBefore> collectionBefore = changeRule.getCollection(COMMIT_COLLECTION, CommitBefore.class);
        CommitBefore commitBefore = new CommitBefore(COMMIT_ID, OTHER_FIELD);
        collectionBefore.insert(commitBefore);

        changeRule.runChanges();

        TestCollection<CommitAfter> collectionAfter = changeRule.getCollection(COMMIT_COLLECTION, CommitAfter.class);
        CommitAfter userAfter = collectionAfter.findOneById(COMMIT_ID);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.isRemoved()).isFalse();
    }

}
