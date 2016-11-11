package com.benromberg.cordonbleu.data.migration.change0012;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0012 extends Change {
    private static final String REPOSITORY_COLLECTION = "codeRepositoryMetadata";
    private static final String TEAM_COLLECTION = "team";
    private static final String CHANGELOG = "0012";
    private static final String CHANGESET01 = CHANGELOG + "_01";
    private static final String CHANGESET02 = CHANGELOG + "_02";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void uniqueTeamNames() {
        convertToUniqueNames(getCollection(TEAM_COLLECTION));
    }

    @ChangeSet(order = CHANGESET02, id = CHANGESET02, author = CHANGESET02)
    public void uniqueRepositoryNames() {
        convertToUniqueNames(getCollection(REPOSITORY_COLLECTION));
    }

    private void convertToUniqueNames(ChangeCollection collection) {
        collection.updateAll(entity -> {
            String name = entity.get("name").toString();
            return $setOne("name", object("value", name).append("unique", name.toLowerCase()));
        });
    }
}
