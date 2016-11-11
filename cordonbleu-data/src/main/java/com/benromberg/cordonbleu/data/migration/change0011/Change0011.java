package com.benromberg.cordonbleu.data.migration.change0011;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DBObject;

@ChangeLog
public class Change0011 extends Change {
    private static final String COMMIT_COLLECTION = "commit";
    private static final String CHANGELOG = "0011";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void uniqueCommitAuthorEmails() {
        ChangeCollection commitCollection = getCollection(COMMIT_COLLECTION);
        commitCollection.updateAll(commit -> {
            String email = ((DBObject) commit.get("author")).get("email").toString();
            return $setOne("author.email", object("value", email).append("unique", email.toLowerCase()));
        });
    }
}
