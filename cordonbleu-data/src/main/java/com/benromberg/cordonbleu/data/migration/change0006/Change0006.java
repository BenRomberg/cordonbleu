package com.benromberg.cordonbleu.data.migration.change0006;

import static java.util.Arrays.asList;

import java.util.List;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DBObject;

@ChangeLog
public class Change0006 extends Change {
    private static final String NEW_REPOSITORIES_FIELD = "repositories";
    private static final String OLD_BRANCHES_FIELD = "branches";
    private static final String OLD_REPOSITORY_FIELD = "repository";
    private static final String CHANGELOG = "0006";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void adjustCommitRepositories() {
        ChangeCollection collection = getCollection("commit");
        collection.updateAll(commit -> {
            List<DBObject> repositoryList = asList(object(OLD_REPOSITORY_FIELD, commit.get(OLD_REPOSITORY_FIELD))
                    .append(OLD_BRANCHES_FIELD, commit.get(OLD_BRANCHES_FIELD)));
            return merge($setOne(NEW_REPOSITORIES_FIELD, repositoryList),
                    $unset(OLD_REPOSITORY_FIELD, OLD_BRANCHES_FIELD));
        });
    }
}