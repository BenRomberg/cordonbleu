package com.benromberg.cordonbleu.data.migration.change0006;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;
import com.benromberg.cordonbleu.data.migration.change0006.CommitAfter.CommitRepositoryAfter;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0006.Change0006;

public class Change0006Test {
    private static final List<String> COMMIT_BRANCHES = asList("commit-branch");
    private static final String REPOSITORY_ID = "repository-id";
    private static final String OTHER_FIELD = "other field";
    private static final String COMMIT_ID1 = "commit-id-1";
    private static final String COMMIT_ID2 = "commit-id-2";
    private static final String COMMIT_COLLECTION = "commit";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0006.class);

    @Test
    public void convertUniqueFields_CanReadValues() throws Exception {
        insertCommitBefore(COMMIT_ID1);
        insertCommitBefore(COMMIT_ID2);

        changeRule.runChanges();

        assertCommitAfter(COMMIT_ID1);
        assertCommitAfter(COMMIT_ID2);
    }

    private void assertCommitAfter(String commitId) {
        TestCollection<CommitAfter> collectionAfter = changeRule.getCollection(COMMIT_COLLECTION, CommitAfter.class);
        CommitAfter commitAfter = collectionAfter.findOneById(commitId);
        assertThat(commitAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(commitAfter.getRepositories()).extracting(CommitRepositoryAfter::getRepository,
                CommitRepositoryAfter::getBranches).containsExactly(tuple(REPOSITORY_ID, COMMIT_BRANCHES));
    }

    private void insertCommitBefore(String commitId) {
        TestCollection<CommitBefore> collectionBefore = changeRule.getCollection(COMMIT_COLLECTION, CommitBefore.class);
        CommitBefore userBefore = new CommitBefore(commitId, REPOSITORY_ID, OTHER_FIELD, COMMIT_BRANCHES);
        collectionBefore.insert(userBefore);
    }
}
