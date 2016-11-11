package com.benromberg.cordonbleu.data.migration.change0017;

import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0017.Change0017;
import com.mongodb.BasicDBObject;

public class Change0017Test {
    private static final String COMMIT_COLLECTION = "commit";
    private static final String OLD_INDEX_KEY = "repositories";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0017.class);

    @Test
    public void existingRepositoriesIndex_IsDropped() throws Exception {
        TestCollection<Commit> collection = changeRule.getCollection(COMMIT_COLLECTION, Commit.class);
        collection.createIndex(new BasicDBObject(OLD_INDEX_KEY, 1));

        changeRule.runChanges();

        changeRule.assertIndexIsNotPresent(collection, OLD_INDEX_KEY);
    }

    @Test
    public void nonExistingRepositoriesIndex_ChangesNothing() throws Exception {
        TestCollection<Commit> collection = changeRule.getCollection(COMMIT_COLLECTION, Commit.class);

        changeRule.runChanges();

        changeRule.assertIndexIsNotPresent(collection, OLD_INDEX_KEY);
    }
}
