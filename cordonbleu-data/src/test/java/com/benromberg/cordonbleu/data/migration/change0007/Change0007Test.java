package com.benromberg.cordonbleu.data.migration.change0007;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0007.Change0007;

public class Change0007Test {
    private static final String REPO_ID_2 = "repo-id-2";
    private static final String REPO_ID_1 = "repo-id-1";
    private static final String OTHER_FIELD_1 = "other field 1";
    private static final String OTHER_FIELD_2 = "other field 2";
    private static final String REPOSITORY_COLLECTION = "codeRepositoryMetadata";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0007.class);

    @Test
    public void addEmptyFlagsToAllRepositories() throws Exception {
        TestCollection<RepositoryBefore> collectionBefore = changeRule.getCollection(REPOSITORY_COLLECTION,
                RepositoryBefore.class);
        collectionBefore.insert(new RepositoryBefore(REPO_ID_1, OTHER_FIELD_1));
        collectionBefore.insert(new RepositoryBefore(REPO_ID_2, OTHER_FIELD_2));

        changeRule.runChanges();

        TestCollection<RepositoryAfter> collectionAfter = changeRule.getCollection(REPOSITORY_COLLECTION,
                RepositoryAfter.class);
        List<RepositoryAfter> repositoriesAfter = collectionAfter.find().toArray();
        assertThat(repositoriesAfter).extracting(RepositoryAfter::getOtherField, RepositoryAfter::getFlags)
                .containsOnly(tuple(OTHER_FIELD_1, asList()), tuple(OTHER_FIELD_2, asList()));
    }
}
