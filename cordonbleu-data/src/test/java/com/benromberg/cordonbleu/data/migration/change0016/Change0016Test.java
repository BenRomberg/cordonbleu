package com.benromberg.cordonbleu.data.migration.change0016;

import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0016.Change0016;
import com.mongodb.BasicDBObject;

public class Change0016Test {
    private static final String INDEX_KEY = "name.unique";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0016.class);

    @Test
    public void existingNameIndex_IsDropped() throws Exception {
        TestCollection<Repository> collection = changeRule.getCollection("codeRepositoryMetadata", Repository.class);
        collection.createIndex(new BasicDBObject(INDEX_KEY, 1), new BasicDBObject("unique", true));

        changeRule.runChanges();

        changeRule.assertIndexIsNotPresent(collection, INDEX_KEY);
    }

    @Test
    public void nonExistingNameIndex_ChangesNothing() throws Exception {
        TestCollection<Repository> collection = changeRule.getCollection("codeRepositoryMetadata", Repository.class);

        changeRule.runChanges();

        changeRule.assertIndexIsNotPresent(collection, INDEX_KEY);
    }
}
