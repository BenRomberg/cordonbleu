package com.benromberg.cordonbleu.data.migration.change0015;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0015 extends Change {
    private static final String COMMIT_COLLECTION = "commit";
    private static final String CHANGELOG = "0015";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void addKeyPairs() {
        ChangeCollection collection = getCollection(COMMIT_COLLECTION);
        collection.updateAll($setOne("removed", false));
    }
}
