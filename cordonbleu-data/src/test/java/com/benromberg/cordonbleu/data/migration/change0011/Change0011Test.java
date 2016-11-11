package com.benromberg.cordonbleu.data.migration.change0011;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0011.Change0011;

public class Change0011Test {
    private static final String AUTHOR_EMAIL = "author@email.com";
    private static final String AUTHOR_NAME = "author name";
    private static final String COMMIT_COLLECTION = "commit";
    private static final String OTHER_FIELD = "other field";
    private static final String COMMIT_ID = "commit-id";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0011.class);

    @Test
    public void convertUniqueFields_CanReadValues() throws Exception {
        TestCollection<CommitBefore> collectionBefore = changeRule.getCollection(COMMIT_COLLECTION, CommitBefore.class);
        CommitBefore userBefore = new CommitBefore(COMMIT_ID, new CommitAuthorBefore(AUTHOR_NAME, AUTHOR_EMAIL),
                OTHER_FIELD);
        collectionBefore.insert(userBefore);

        changeRule.runChanges();

        TestCollection<CommitAfter> collectionAfter = changeRule.getCollection(COMMIT_COLLECTION, CommitAfter.class);
        CommitAfter userAfter = collectionAfter.findOneById(COMMIT_ID);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.getAuthor().getName()).isEqualTo(AUTHOR_NAME);
        assertThat(userAfter.getAuthor().getEmail()).isEqualTo(AUTHOR_EMAIL);
    }

}
