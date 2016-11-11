package com.benromberg.cordonbleu.data.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserSession;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;

@Singleton
public class UserSessionDao extends MongoDao<String, UserSession> {
    @Inject
    public UserSessionDao(DatabaseWithMigration database, UserDao userDao) {
        super(database, String.class, UserSession.class, "user_session", createCustomModule(userDao));
    }

    private static CustomModule createCustomModule(UserDao userDao) {
        CustomModule customModule = new CustomModule();
        customModule.addSerializer(User.class, new EntitySerializer<>());
        customModule.addDeserializer(User.class, new EntityDeserializer<>(userDao));
        return customModule;
    }
}
