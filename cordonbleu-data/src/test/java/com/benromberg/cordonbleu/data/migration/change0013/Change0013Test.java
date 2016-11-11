package com.benromberg.cordonbleu.data.migration.change0013;

import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0013.Change0013;
import com.mongodb.BasicDBObject;

public class Change0013Test {
    private static final String INDEX_KEY = "name";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0013.class);

    @Test
    public void existingNameIndex_IsDropped() throws Exception {
        TestCollection<Repository> collection = changeRule.getCollection("codeRepositoryMetadata", Repository.class);
        collection.createIndex(new BasicDBObject(INDEX_KEY, 1));

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
