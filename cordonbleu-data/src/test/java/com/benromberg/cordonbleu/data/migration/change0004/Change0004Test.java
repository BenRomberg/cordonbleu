package com.benromberg.cordonbleu.data.migration.change0004;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0004.Change0004;

public class Change0004Test {
    private static final String USER_ID_2 = "user-id-2";
    private static final String USER_ID_1 = "user-id-1";
    private static final String OTHER_FIELD_1 = "other field 1";
    private static final String OTHER_FIELD_2 = "other field 2";
    private static final String USER_COLLECTION = "user";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0004.class);

    @Test
    public void addEmptyFlagsToAllUsers() throws Exception {
        TestCollection<UserBefore> collectionBefore = changeRule.getCollection(USER_COLLECTION, UserBefore.class);
        collectionBefore.insert(new UserBefore(USER_ID_1, OTHER_FIELD_1));
        collectionBefore.insert(new UserBefore(USER_ID_2, OTHER_FIELD_2));

        changeRule.runChanges();

        TestCollection<UserAfter> collectionAfter = changeRule.getCollection(USER_COLLECTION, UserAfter.class);
        List<UserAfter> usersAfter = collectionAfter.find().toArray();
        assertThat(usersAfter).extracting(UserAfter::getOtherField, UserAfter::getFlags).containsOnly(
                tuple(OTHER_FIELD_1, asList()), tuple(OTHER_FIELD_2, asList()));
    }

}
