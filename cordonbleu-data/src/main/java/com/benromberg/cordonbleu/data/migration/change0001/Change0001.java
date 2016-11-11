package com.benromberg.cordonbleu.data.migration.change0001;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DBObject;

@ChangeLog
public class Change0001 extends Change {
    private static final String CHANGELOG = "0001";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void adjustCommitAuthor() {
        ChangeCollection collection = getCollection("commit");
        collection.updateAll(commit -> {
            DBObject newAuthor = object("name", commit.get("author")).append("email", commit.get("authorEmail"));
            return merge($setOne("author", newAuthor), $unset("authorEmail"));
        });
    }
}
