package com.benromberg.cordonbleu.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.dao.UserSessionDao;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.data.model.UserSession;
import com.benromberg.cordonbleu.util.SystemTimeRule;

import java.time.Duration;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.service.user.UserService;

public class UserServiceTest {
    private static final User USER = new User("user@email.com", "user-name", "password");
    private static final String SESSION_TOKEN = "session-token";
    private static final String SESSION_HASH = "4a7e952c45175bd7ead9def46d792f014e7b393a36d86b0278e39bc9c330c53c045f634476ddcb8808f87a7a47e11a81050af2ccb06425e2e64ec87a4f7b0298";

    @Rule
    public SystemTimeRule systemTimeRule = new SystemTimeRule();

    @Rule
    public DaoRule databaseRule = new DaoRule();

    private final UserDao userDao = databaseRule.createUserDao();
    private final UserSessionDao userSessionDao = databaseRule.createUserSessionDao();
    private final UserService service = new UserService(new PasswordAuthenticationMock(), userDao, userSessionDao,
            databaseRule.createTeamDao());

    @Test
    public void verifySession_WithMissingSession_YieldsEmptyUser() throws Exception {
        Optional<User> sessionUser = service.verifySession(SESSION_TOKEN);
        assertThat(sessionUser).isEmpty();
    }

    @Test
    public void verifySession_WithExpiredSession_YieldsEmptyUser() throws Exception {
        userDao.insert(USER);
        userSessionDao.insert(new UserSession(SESSION_HASH, USER));
        systemTimeRule.advanceByDuration(Duration.ofDays(31));
        Optional<User> sessionUser = service.verifySession(SESSION_TOKEN);
        assertThat(sessionUser).isEmpty();
    }

    @Test
    public void verifySession_WithInactiveUser_YieldsEmptyUser() throws Exception {
        userDao.insert(USER);
        userSessionDao.insert(new UserSession(SESSION_HASH, USER));
        userDao.updateFlag(USER.getId(), UserFlag.INACTIVE, true);
        Optional<User> sessionUser = service.verifySession(SESSION_TOKEN);
        assertThat(sessionUser).isEmpty();
    }

    @Test
    public void verifySession_WithInactiveAdminUser_ReturnsUser() throws Exception {
        userDao.insert(USER);
        userSessionDao.insert(new UserSession(SESSION_HASH, USER));
        userDao.updateFlag(USER.getId(), UserFlag.INACTIVE, true);
        userDao.updateFlag(USER.getId(), UserFlag.ADMIN, true);
        User sessionUser = service.verifySession(SESSION_TOKEN).get();
        assertThat(sessionUser.getEmail()).isEqualTo(USER.getEmail());
    }

    @Test
    public void verifySession_WithMatchingSession_ReturnsUser() throws Exception {
        userDao.insert(USER);
        userSessionDao.insert(new UserSession(SESSION_HASH, USER));
        User sessionUser = service.verifySession(SESSION_TOKEN).get();
        assertThat(sessionUser.getEmail()).isEqualTo(USER.getEmail());
    }
}
