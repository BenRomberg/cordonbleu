package com.benromberg.cordonbleu.data.migration.change0012;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0012.Change0012;

public class Change0012Test {
    private static final String REPOSITORY_COLLECTION = "codeRepositoryMetadata";
    private static final String TEAM_COLLECTION = "team";
    private static final String ENTITY_NAME = "entity-name";
    private static final String ENTITY_ID = "entity-id";
    private static final String OTHER_FIELD = "other field";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0012.class);

    @Test
    public void convertUniqueFields_OnTeams_CanReadValues() throws Exception {
        insertNamedEntity(TEAM_COLLECTION);

        changeRule.runChanges();

        assertNamedEntity(TEAM_COLLECTION);
    }

    @Test
    public void convertUniqueFields_OnRepositories_CanReadValues() throws Exception {
        insertNamedEntity(REPOSITORY_COLLECTION);

        changeRule.runChanges();

        assertNamedEntity(REPOSITORY_COLLECTION);
    }

    private void assertNamedEntity(String collection) {
        TestCollection<NamedEntityAfter> collectionAfter = changeRule.getCollection(collection, NamedEntityAfter.class);
        NamedEntityAfter userAfter = collectionAfter.findOneById(ENTITY_ID);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.getName()).isEqualTo(ENTITY_NAME);
    }

    private void insertNamedEntity(String collection) {
        TestCollection<NamedEntityBefore> collectionBefore = changeRule.getCollection(collection,
                NamedEntityBefore.class);
        NamedEntityBefore userBefore = new NamedEntityBefore(ENTITY_ID, ENTITY_NAME, OTHER_FIELD);
        collectionBefore.insert(userBefore);
    }
}
