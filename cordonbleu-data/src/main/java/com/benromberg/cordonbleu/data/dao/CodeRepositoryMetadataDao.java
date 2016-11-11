package com.benromberg.cordonbleu.data.dao;

import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.uniqueProperty;
import static java.util.Arrays.asList;
import com.benromberg.cordonbleu.util.CollectionUtil;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.NamedEntity;
import com.benromberg.cordonbleu.data.model.RepositoryFlag;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;
import com.benromberg.cordonbleu.data.validation.NameValidation;

@Singleton
public class CodeRepositoryMetadataDao extends CacheDao<String, CodeRepositoryMetadata> {
    @Inject
    public CodeRepositoryMetadataDao(DatabaseWithMigration db, NameValidation<NamedEntity> validation, TeamDao teamDao) {
        super(db, String.class, CodeRepositoryMetadata.class, "codeRepositoryMetadata", createCustomModule(teamDao),
                validation);
        createUniqueIndex(uniqueProperty(CodeRepositoryMetadata.NAME_PROPERTY), CodeRepositoryMetadata.TEAM_PROPERTY);
    }

    private static CustomModule createCustomModule(TeamDao teamDao) {
        CustomModule customModule = new CustomModule();
        customModule.addSerializer(Team.class, new EntitySerializer<>());
        customModule.addDeserializer(Team.class, new EntityDeserializer<>(teamDao));
        return customModule;
    }

    public List<CodeRepositoryMetadata> findByIds(List<String> ids) {
        return find(DBQuery.in(ID_PROPERTY, ids)).toArray();
    }

    public List<CodeRepositoryMetadata> findActive() {
        return findByFlag(RepositoryFlag.REMOVE_ON_NEXT_UPDATE, false);
    }

    private List<CodeRepositoryMetadata> findAndSortByName(Query query) {
        return CollectionUtil.sortCaseInsensitive(find(query).toArray(), CodeRepositoryMetadata::getName);
    }

    public Optional<CodeRepositoryMetadata> updateFlag(String repositoryId, RepositoryFlag flag, boolean flagValue) {
        if (flagValue) {
            return update(repositoryId, DBUpdate.push(CodeRepositoryMetadata.FLAGS_PROPERTY, flag));
        }
        return update(repositoryId, DBUpdate.pull(CodeRepositoryMetadata.FLAGS_PROPERTY, flag));
    }

    public List<CodeRepositoryMetadata> findByFlag(RepositoryFlag flag, boolean flagValue) {
        return findAndSortByName(queryByFlag(flag, flagValue));
    }

    private Query queryByFlag(RepositoryFlag flag, boolean flagValue) {
        Query query = DBQuery.is(CodeRepositoryMetadata.FLAGS_PROPERTY, asList(flag));
        if (!flagValue) {
            query = DBQuery.nor(query);
        }
        return query;
    }

    public List<CodeRepositoryMetadata> findByTeam(Team team) {
        Query activeQuery = queryByFlag(RepositoryFlag.REMOVE_ON_NEXT_UPDATE, false);
        return findAndSortByName(activeQuery.is(CodeRepositoryMetadata.TEAM_PROPERTY, team));
    }
}
