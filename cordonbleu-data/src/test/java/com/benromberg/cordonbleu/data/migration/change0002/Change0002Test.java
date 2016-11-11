package com.benromberg.cordonbleu.data.migration.change0002;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0002.Change0002;

public class Change0002Test {
    private static final String OTHER_USER_ID = "other-user-id";
    private static final String OTHER_FIELD = "other field";
    private static final String USER_ID = "user-id";
    private static final String USER_COLLECTION = "user";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0002.class);

    @Test
    public void addUserEmailAliases() throws Exception {
        insertUser(USER_ID);

        changeRule.runChanges();

        assertUser(USER_ID);
    }

    @Test
    public void addUserEmailAliases_ForAllUsers() throws Exception {
        insertUser(USER_ID);
        insertUser(OTHER_USER_ID);

        changeRule.runChanges();

        assertUser(USER_ID);
        assertUser(OTHER_USER_ID);
    }

    private void assertUser(String userId) {
        TestCollection<UserAfter> collectionAfter = changeRule.getCollection(USER_COLLECTION, UserAfter.class);
        UserAfter userAfter = collectionAfter.findOneById(userId);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.getEmailAliases()).isEmpty();
    }

    private void insertUser(String userId) {
        TestCollection<UserBefore> collectionBefore = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        UserBefore userBefore = new UserBefore(userId, OTHER_FIELD);
        collectionBefore.insert(userBefore);
    }
}
