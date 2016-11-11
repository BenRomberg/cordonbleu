package com.benromberg.cordonbleu.data.migration.change0003;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0003.Change0003;
import com.mongodb.BasicDBObject;

public class Change0003Test {
    private static final String INDEX_KEY = "email";
    private static final String EMAIL = "UPPERCASE@email.com";
    private static final String EXISTING_USER_NAME = "existingUserName";
    private static final String OTHER_FIELD = "other field";
    private static final String USER_ID = "user-id";
    private static final String USER_COLLECTION = "user";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0003.class);

    @Test
    public void adjustEmptyUserName() throws Exception {
        TestCollection<UserBefore> collectionBefore = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        UserBefore userBefore = new UserBefore(USER_ID, Optional.empty(), EMAIL, OTHER_FIELD);
        collectionBefore.insert(userBefore);

        changeRule.runChanges();

        TestCollection<UserAfter> collectionAfter = changeRule.getCollection(USER_COLLECTION, UserAfter.class);
        UserAfter userAfter = collectionAfter.findOneById(USER_ID);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.getName().getValue()).isEqualTo(USER_ID);
    }

    @Test
    public void doNotAdjustExistingUserName() throws Exception {
        TestCollection<UserBefore> collectionBefore = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        UserBefore userBefore = new UserBefore(USER_ID, Optional.of(EXISTING_USER_NAME), EMAIL, OTHER_FIELD);
        collectionBefore.insert(userBefore);

        changeRule.runChanges();

        TestCollection<UserAfter> collectionAfter = changeRule.getCollection(USER_COLLECTION, UserAfter.class);
        UserAfter userAfter = collectionAfter.findOneById(USER_ID);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.getName().getValue()).isEqualTo(EXISTING_USER_NAME);
    }

    @Test
    public void convertUniqueFields() throws Exception {
        TestCollection<UserBefore> collectionBefore = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        UserBefore userBefore = new UserBefore(USER_ID, Optional.of(EXISTING_USER_NAME), EMAIL, OTHER_FIELD);
        collectionBefore.insert(userBefore);

        changeRule.runChanges();

        TestCollection<UserAfter> collectionAfter = changeRule.getCollection(USER_COLLECTION, UserAfter.class);
        UserAfter userAfter = collectionAfter.findOneById(USER_ID);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.getName().getUnique()).isEqualTo(EXISTING_USER_NAME.toLowerCase());
        assertThat(userAfter.getName().getValue()).isEqualTo(EXISTING_USER_NAME);
        assertThat(userAfter.getEmail().getUnique()).isEqualTo(EMAIL.toLowerCase());
        assertThat(userAfter.getEmail().getValue()).isEqualTo(EMAIL);
    }

    @Test
    public void existingEmailIndex_IsDropped() throws Exception {
        TestCollection<UserBefore> collection = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        collection.createIndex(new BasicDBObject(INDEX_KEY, 1));

        changeRule.runChanges();

        changeRule.assertIndexIsNotPresent(collection, INDEX_KEY);
    }

    @Test
    public void nonExistingEmailIndex_ChangesNothing() throws Exception {
        TestCollection<UserBefore> collection = changeRule.getCollection(USER_COLLECTION, UserBefore.class);

        changeRule.runChanges();

        changeRule.assertIndexIsNotPresent(collection, INDEX_KEY);
    }
}
