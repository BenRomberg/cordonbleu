package com.benromberg.cordonbleu.data.dao;

import static com.benromberg.cordonbleu.data.model.NamedEntity.NAME_PROPERTY;
import static com.benromberg.cordonbleu.data.model.User.EMAIL_ALIASES_PROPERTY;
import static com.benromberg.cordonbleu.data.model.User.EMAIL_PROPERTY;
import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.PROPERTY_UNIQUE;
import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.uniqueProperty;
import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.uniqueValue;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.data.model.UserTeam;
import com.benromberg.cordonbleu.data.model.UserTeamFlag;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;
import com.benromberg.cordonbleu.data.validation.UserValidation;

@Singleton
public class UserDao extends CacheDao<String, User> {
    private final UserValidation validation;

    @Inject
    public UserDao(DatabaseWithMigration db, UserValidation validation, TeamDao teamDao) {
        super(db, String.class, User.class, "user", createCustomModule(teamDao), validation);
        this.validation = validation;
        createUniqueIndex(uniqueProperty(User.EMAIL_PROPERTY));
        createUniqueIndex(uniqueProperty(User.NAME_PROPERTY));
    }

    private static CustomModule createCustomModule(TeamDao teamDao) {
        CustomModule customModule = new CustomModule();
        customModule.addSerializer(Team.class, new EntitySerializer<>());
        customModule.addDeserializer(Team.class, new EntityDeserializer<>(teamDao));
        return customModule;
    }

    public Optional<User> findByEmail(String email) {
        return findOne(DBQuery.is(uniqueProperty(EMAIL_PROPERTY), uniqueValue(email)));
    }

    public List<User> findByEmailOrAlias(String email) {
        String uniqueEmail = uniqueValue(email);
        Query emailQuery = DBQuery.is(uniqueProperty(EMAIL_PROPERTY), uniqueEmail);
        Query emailAliasQuery = DBQuery.elemMatch(EMAIL_ALIASES_PROPERTY, DBQuery.is(PROPERTY_UNIQUE, uniqueEmail));
        return find(DBQuery.or(emailQuery, emailAliasQuery)).toArray();
    }

    public Optional<User> addEmailAlias(User user, String aliasEmail) {
        return update(user.getId(), update().push(EMAIL_ALIASES_PROPERTY, aliasEmail));
    }

    public Optional<User> update(User user, String name, String email, List<String> emailAliases) {
        validation.validateName(name);
        validation.validateEmail(email, emailAliases);
        List<CaseInsensitiveUniqueValue> uniqueAliases = emailAliases.stream()
                .map(alias -> new CaseInsensitiveUniqueValue(alias)).collect(toList());
        return update(
                user.getId(),
                update().set(User.NAME_PROPERTY, name).set(User.EMAIL_PROPERTY, email)
                        .addToSet(User.EMAIL_ALIASES_PROPERTY, uniqueAliases));
    }

    public List<User> findByIds(List<String> userIds) {
        return find(DBQuery.in(ID_PROPERTY, userIds)).toArray();
    }

    public List<User> findAll() {
        return sortByName(find());
    }

    private List<User> sortByName(DBCursor<User> find) {
        return find.sort(object(uniqueProperty(NAME_PROPERTY), 1)).toArray();
    }

    public Optional<User> updateFlag(String userId, UserFlag flag, boolean flagValue) {
        if (flagValue) {
            return update(userId, update().addToSet(User.FLAGS_PROPERTY, flag));
        }
        return update(userId, update().pull(User.FLAGS_PROPERTY, flag));
    }

    public List<User> findByFlag(UserFlag flag, boolean flagValue) {
        Query query = DBQuery.is(User.FLAGS_PROPERTY, asList(flag));
        if (!flagValue) {
            query = DBQuery.nor(query);
        }
        return sortByName(find(query));
    }

    public Optional<User> addTeam(String userId, Team team) {
        return update(
                DBQuery.is(ID_PROPERTY, userId).notEquals(User.TEAMS_PROPERTY + "." + UserTeam.TEAM_PROPERTY, team),
                update().push(User.TEAMS_PROPERTY, convertToDbObject(new UserTeam(team))));
    }

    public Optional<User> removeTeam(String userId, Team team) {
        return update(DBQuery.is(ID_PROPERTY, userId).is(User.TEAMS_PROPERTY + "." + UserTeam.TEAM_PROPERTY, team),
                update().pull(User.TEAMS_PROPERTY, object(UserTeam.TEAM_PROPERTY, team.getId())));
    }

    public Optional<User> updateTeamFlag(String userId, Team team, UserTeamFlag flag, boolean flagValue) {
        Query selectUserTeam = DBQuery.is(ID_PROPERTY, userId).is(User.TEAMS_PROPERTY + "." + UserTeam.TEAM_PROPERTY,
                team);
        String updateProperty = User.TEAMS_PROPERTY + ".$." + UserTeam.FLAGS_PROPERTY;
        if (flagValue) {
            return update(selectUserTeam, update().addToSet(updateProperty, flag));
        }
        return update(selectUserTeam, update().pull(updateProperty, flag));
    }

    public List<User> findTeamMembers(Team team) {
        return sortByName(find(DBQuery.is(User.TEAMS_PROPERTY + "." + UserTeam.TEAM_PROPERTY, team)));
    }

    public List<User> findByNamePrefix(String prefix, int limit) {
        return sortByName(find(
                DBQuery.regex(uniqueProperty(User.NAME_PROPERTY),
                        Pattern.compile("^" + Pattern.quote(prefix), Pattern.CASE_INSENSITIVE))).limit(limit));
    }

    public Optional<User> findByName(String name) {
        return findOne(DBQuery.is(uniqueProperty(User.NAME_PROPERTY), uniqueValue(name)));
    }
}
