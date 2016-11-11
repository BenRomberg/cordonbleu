package com.benromberg.cordonbleu.data.migration.change0007;

import static java.util.Arrays.asList;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0007 extends Change {
    private static final String CHANGELOG = "0007";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void addRepositoryFlags() {
        ChangeCollection collection = getCollection("codeRepositoryMetadata");
        collection.updateAll($setOne("flags", asList()));
    }
}
