package com.benromberg.cordonbleu.data.migration.change0016;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0016 extends Change {
    private static final String REPOSITORY_COLLECTION = "codeRepositoryMetadata";
    private static final String CHANGELOG = "0016";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void removeNameIndex() {
        ChangeCollection collection = getCollection(REPOSITORY_COLLECTION);
        collection.dropIndexIfExists("name.unique");
    }
}
