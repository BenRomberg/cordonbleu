package com.benromberg.cordonbleu.data.migration.change0008;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0008 extends Change {
    private static final String CHANGELOG = "0008";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void dropHighlightCache() {
        ChangeCollection collection = getCollection("highlightCache");
        collection.drop();
    }
}
