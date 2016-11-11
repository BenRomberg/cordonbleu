package com.benromberg.cordonbleu.data.migration.change0003;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0003 extends Change {
    private static final String CHANGELOG = "0003";
    private static final String CHANGESET01 = CHANGELOG + "_01";
    private static final String CHANGESET02 = CHANGELOG + "_02";
    private static final String CHANGESET03 = CHANGELOG + "_03";
    private static final String CHANGESET04 = CHANGELOG + "_04";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void adjustUserName() {
        ChangeCollection collection = getCollection("user");
        collection.updateFieldIfNotSet("name", user -> user.get(ID_PROPERTY));
    }

    @ChangeSet(order = CHANGESET02, id = CHANGESET02, author = CHANGESET02)
    public void dropEmailUniqueIndex() {
        ChangeCollection collection = getCollection("user");
        collection.dropIndexIfExists("email");
    }

    @ChangeSet(order = CHANGESET03, id = CHANGESET03, author = CHANGESET03)
    public void uniqueEmail() {
        ChangeCollection collection = getCollection("user");
        collection.updateAll(user -> {
            String email = user.get("email").toString();
            return $setOne("email", object("value", email).append("unique", email.toLowerCase()));
        });
    }

    @ChangeSet(order = CHANGESET04, id = CHANGESET04, author = CHANGESET04)
    public void uniqueUserName() {
        ChangeCollection collection = getCollection("user");
        collection.updateAll(user -> {
            String userName = user.get("name").toString();
            return $setOne("name", object("value", userName).append("unique", userName.toLowerCase()));
        });
    }
}
