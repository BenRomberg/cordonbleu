package com.benromberg.cordonbleu.data.migration.change0005;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0005.Change0005;

public class Change0005Test {
    private static final String ALIAS_EMAIL = "ALIAS@email.com";
    private static final String OTHER_FIELD = "other field";
    private static final String USER_ID = "user-id";
    private static final String USER_COLLECTION = "user";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0005.class);

    @Test
    public void convertUniqueFields_CanReadValues() throws Exception {
        insertUserBefore();

        changeRule.runChanges();

        TestCollection<UserAfter> collectionAfter = changeRule.getCollection(USER_COLLECTION, UserAfter.class);
        UserAfter userAfter = collectionAfter.findOneById(USER_ID);
        assertThat(userAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(userAfter.getEmailAliases().get(0)).isEqualTo(ALIAS_EMAIL);
    }

    @Test
    public void convertUniqueFields_CanReadUniqueValues() throws Exception {
        insertUserBefore();

        changeRule.runChanges();

        TestCollection<UserAfterWithUniqueValue> uniqueCollectionAfter = changeRule.getCollection(USER_COLLECTION,
                UserAfterWithUniqueValue.class);
        UserAfterWithUniqueValue uniqueUserAfter = uniqueCollectionAfter.findOneById(USER_ID);
        assertThat(uniqueUserAfter.getOtherField()).isEqualTo(OTHER_FIELD);
        assertThat(uniqueUserAfter.getEmailAliases().get(0).getValue()).isEqualTo(ALIAS_EMAIL);
        assertThat(uniqueUserAfter.getEmailAliases().get(0).getUnique()).isEqualTo(ALIAS_EMAIL.toLowerCase());
    }

    private void insertUserBefore() {
        TestCollection<UserBefore> collectionBefore = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        UserBefore userBefore = new UserBefore(USER_ID, asList(ALIAS_EMAIL), OTHER_FIELD);
        collectionBefore.insert(userBefore);
    }
}
