package com.benromberg.cordonbleu.data.dao;

import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.uniqueProperty;
import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.uniqueValue;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;

import com.benromberg.cordonbleu.data.model.NamedEntity;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.TeamFlag;
import com.benromberg.cordonbleu.data.validation.NameValidation;

@Singleton
public class TeamDao extends CacheDao<String, Team> {
    private final NameValidation<NamedEntity> validation;

    @Inject
    public TeamDao(DatabaseWithMigration db, NameValidation<NamedEntity> validation) {
        super(db, String.class, Team.class, "team", validation);
        this.validation = validation;
        createUniqueIndex(uniqueProperty(Team.NAME_PROPERTY));
    }

    public List<Team> findPublic() {
        return find(DBQuery.nor(DBQuery.is(Team.FLAGS_PROPERTY, asList(TeamFlag.PRIVATE)))).toArray();
    }

    public Optional<Team> updateFlag(String teamId, TeamFlag flag, boolean flagValue) {
        if (flagValue) {
            return update(teamId, update().addToSet(Team.FLAGS_PROPERTY, flag));
        }
        return update(teamId, update().pull(Team.FLAGS_PROPERTY, flag));
    }

    public Optional<Team> findByName(String name) {
        return findOne(DBQuery.is(uniqueProperty(Team.NAME_PROPERTY), uniqueValue(name)));
    }

    public Optional<Team> updateTeam(Team team, String name, Set<TeamFlag> flags) {
        validation.validateName(name);
        return update(team.getId(), update().set(Team.NAME_PROPERTY, name).set(Team.FLAGS_PROPERTY, flags));
    }
}
