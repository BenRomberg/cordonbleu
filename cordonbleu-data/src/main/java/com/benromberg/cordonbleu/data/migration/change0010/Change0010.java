package com.benromberg.cordonbleu.data.migration.change0010;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import com.benromberg.cordonbleu.data.migration.Change;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.BasicDBObject;

@ChangeLog
public class Change0010 extends Change {
    private static final String USER_COLLECTION = "user";
    private static final String TEAM_COLLECTION = "team";
    private static final String CHANGELOG = "0010";
    private static final String CHANGESET01 = CHANGELOG + "_01";
    private static final String CHANGESET02 = CHANGELOG + "_02";

    @ChangeSet(order = CHANGESET01, id = CHANGESET01, author = CHANGESET01)
    public void addTeamFlags() {
        ChangeCollection teamCollection = getCollection(TEAM_COLLECTION);
        teamCollection.updateAll($setOne("flags", asList()));
    }

    @ChangeSet(order = CHANGESET02, id = CHANGESET02, author = CHANGESET02)
    public void addUserTeams() {
        ChangeCollection teamCollection = getCollection(TEAM_COLLECTION);
        List<Object> allTeamIds = teamCollection.findAllIds();

        List<BasicDBObject> allUserTeams = allTeamIds.stream()
                .map(teamId -> object("team", teamId).append("flags", asList("OWNER"))).collect(toList());
        ChangeCollection userCollection = getCollection(USER_COLLECTION);
        userCollection.updateAll($setOne("teams", allUserTeams));
    }

}
