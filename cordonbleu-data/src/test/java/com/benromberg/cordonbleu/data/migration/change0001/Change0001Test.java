package com.benromberg.cordonbleu.data.migration.change0001;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0001.Change0001;

public class Change0001Test {
    private static final String COMMIT_COLLECTION = "commit";
    private static final String OTHER_FIELD = "other field";
    private static final String AUTHOR_EMAIL = "author@email.com";
    private static final String AUTHOR = "author";
    private static final String COMMIT_ID = "commit id";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0001.class);

    @Test
    public void adjustCommitAuthor() throws Exception {
        TestCollection<CommitBefore> collectionBefore = changeRule.getCollection(COMMIT_COLLECTION, CommitBefore.class);
        CommitBefore commitBefore = new CommitBefore(COMMIT_ID, AUTHOR, AUTHOR_EMAIL, OTHER_FIELD);
        collectionBefore.insert(commitBefore);

        changeRule.runChanges();

        TestCollection<CommitAfter> collectionAfter = changeRule.getCollection(COMMIT_COLLECTION, CommitAfter.class);
        CommitAfter commitAfter = collectionAfter.findOneById(COMMIT_ID);
        assertThat(commitAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(commitAfter.getAuthor().getName()).isEqualTo(AUTHOR);
        assertThat(commitAfter.getAuthor().getEmail()).isEqualTo(AUTHOR_EMAIL);
    }
}
