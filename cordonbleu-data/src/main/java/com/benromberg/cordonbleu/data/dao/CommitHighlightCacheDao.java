package com.benromberg.cordonbleu.data.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.benromberg.cordonbleu.data.model.CommitHighlightCache;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;

@Singleton
public class CommitHighlightCacheDao extends CacheDao<CommitId, CommitHighlightCache> {
    @Inject
    public CommitHighlightCacheDao(DatabaseWithMigration db, UserDao userDao, TeamDao teamDao) {
        super(db, CommitId.class, CommitHighlightCache.class, "commitHighlightCache", createCustomModule(userDao,
                teamDao));
    }

    private static CustomModule createCustomModule(UserDao userDao, TeamDao teamDao) {
        CustomModule customModule = new CustomModule();
        customModule.addSerializer(User.class, new EntitySerializer<>());
        customModule.addDeserializer(User.class, new EntityDeserializer<>(userDao));
        customModule.addSerializer(Team.class, new EntitySerializer<>());
        customModule.addDeserializer(Team.class, new EntityDeserializer<>(teamDao));
        return customModule;
    }

    public void updateComment(CommitId commitId, String commentId, CommitHighlightCacheText text) {
        update(commitId, update()
                .set(CommitHighlightCache.COMMENTS_PROPERTY + "." + commentId, convertToDbObject(text)));
    }
}
