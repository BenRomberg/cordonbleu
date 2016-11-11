package com.benromberg.cordonbleu.data.migration.change0009;

import com.benromberg.cordonbleu.data.migration.Change;
import com.benromberg.cordonbleu.data.util.RandomIdGenerator;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;

@ChangeLog
public class Change0009 extends Change {
    private static final String REPOSITORY_COLLECTION = "codeRepositoryMetadata";
    private static final String DEFAULT_TEAM_NAME = "default-team";
    private static final String NEW_TEAM_NAME_FIELD = "name";
    private static final String COMMIT_COLLECTION = "commit";
    private static final String COMMIT_HIGHLIGHT_CACHE_COLLECTION = "commitHighlightCache";
    private static final String NEW_COMMIT_HASH_FIELD = "hash";
    private static final String NEW_COMMIT_TEAM_FIELD = "team";
    private static final String NEW_REPOSITORY_TEAM_FIELD = "team";
    private static final String TEAM_COLLECTION = "team";
    private static final String CHANGELOG = "0009";
    private static final String CHANGESET01 = CHANGELOG + "_01";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void addDefaultTeamIfNecessary() {
        ChangeCollection repositoryCollection = getCollection(REPOSITORY_COLLECTION);
        if (repositoryCollection.isEmpty()) {
            return;
        }
        addDefaultTeam(repositoryCollection);
    }

    private void addDefaultTeam(ChangeCollection repositoryCollection) {
        ChangeCollection teamCollection = getCollection(TEAM_COLLECTION);
        String teamId = RandomIdGenerator.generate();
        teamCollection.insert(object(ID_PROPERTY, teamId).append(NEW_TEAM_NAME_FIELD, DEFAULT_TEAM_NAME));
        updateRepositoriesWithDefaultTeam(repositoryCollection, teamId);
        updateCommitLikeWithDefaultTeam(teamId, getCollection(COMMIT_COLLECTION));
        updateCommitLikeWithDefaultTeam(teamId, getCollection(COMMIT_HIGHLIGHT_CACHE_COLLECTION));
    }

    private void updateRepositoriesWithDefaultTeam(ChangeCollection repositoryCollection, String teamId) {
        repositoryCollection.updateAll($setOne(NEW_REPOSITORY_TEAM_FIELD, teamId));
    }

    private void updateCommitLikeWithDefaultTeam(String teamId, ChangeCollection collection) {
        collection.updateAllIds(commitLike -> {
            String commitHash = commitLike.get(ID_PROPERTY).toString();
            return object(NEW_COMMIT_HASH_FIELD, commitHash).append(NEW_COMMIT_TEAM_FIELD, teamId);
        });
    }
}
