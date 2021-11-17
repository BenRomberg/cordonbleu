package com.benromberg.cordonbleu.data.dao;

import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserSession;
import com.benromberg.cordonbleu.util.SystemTimeRule;

import org.junit.Rule;
import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class UserSessionDaoTest {
    private static final String SESSION_ID = "session id";
    private static final String ENCRYPTED_PASSWORD = "encrypted password";
    private static final String USER_EMAIL = "user@email.com";

    @Rule
    public DaoRule databaseRule = new DaoRule();

    @Rule
    public SystemTimeRule systemTimeRule = new SystemTimeRule();

    private final UserDao userDao = databaseRule.createUserDao();
    private final UserSessionDao dao = databaseRule.createUserSessionDao();

    @Test
    public void insertedUser_CanBeFoundByEmail() throws Exception {
        User user = new User(USER_EMAIL, "user", ENCRYPTED_PASSWORD);
        userDao.insert(user);
        dao.insert(new UserSession(SESSION_ID, user));
        UserSession foundSession = dao.findById(SESSION_ID).get();
        assertThat(foundSession.getUser()).isEqualTo(user);
        assertThat(foundSession.getCreated()).isEqualTo(systemTimeRule.getDateTime().truncatedTo(ChronoUnit.MILLIS));
    }
}
