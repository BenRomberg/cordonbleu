package com.benromberg.cordonbleu.service.user;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.dao.UserSessionDao;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.data.model.UserSession;
import com.benromberg.cordonbleu.data.model.UserTeamFlag;
import com.benromberg.cordonbleu.util.ClockService;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Hex;

public class UserService {
    private static final String SESSION_TOKEN_HASH_ALGORITHM = "SHA-512";
    private static final Duration USER_SESSION_EXPIRATION = Duration.ofDays(30);

    private final PasswordAuthentication passwordAuthentication;
    private final UserDao userDao;
    private final UserSessionDao userSessionDao;
    private final TeamDao teamDao;

    @Inject
    public UserService(PasswordAuthentication passwordAuthentication, UserDao userDao, UserSessionDao userSessionDao,
            TeamDao teamDao) {
        this.passwordAuthentication = passwordAuthentication;
        this.userDao = userDao;
        this.userSessionDao = userSessionDao;
        this.teamDao = teamDao;
    }

    public Optional<User> findUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public List<User> findUserByEmailOrAlias(String email) {
        return userDao.findByEmailOrAlias(email);
    }

    public User registerUser(String email, String name, String password) {
        User user = new User(email, name, passwordAuthentication.encrypt(password));
        userDao.insert(user);
        return user;
    }

    public Optional<String> startSession(User user, String password) {
        boolean passwordVerified = passwordAuthentication.verify(password, user.getEncryptedPassword());
        if (!passwordVerified) {
            return Optional.empty();
        }
        String token = UUID.randomUUID().toString();
        String hashedHexToken = tokenToStoredHash(token);
        userSessionDao.insert(new UserSession(hashedHexToken, user));
        return Optional.of(token);
    }

    private String tokenToStoredHash(String token) {
        return Hex.encodeHexString(createThreadUnsafeDigest().digest(token.getBytes()));
    }

    private MessageDigest createThreadUnsafeDigest() {
        return convertException(() -> MessageDigest.getInstance(SESSION_TOKEN_HASH_ALGORITHM));
    }

    public Optional<User> verifySession(String sessionToken) {
        String storedHash = tokenToStoredHash(sessionToken);
        Optional<UserSession> session = userSessionDao.findById(storedHash);
        if (!session.isPresent() || isSessionExpired(session.get()) || !isLoginAllowed(session.get().getUser())) {
            return Optional.empty();
        }
        return Optional.of(session.get().getUser());
    }

    public boolean isLoginAllowed(User user) {
        return !user.isInactive() || user.isAdmin();
    }

    private boolean isSessionExpired(UserSession session) {
        return USER_SESSION_EXPIRATION.minus(Duration.between(session.getCreated(), ClockService.now())).isNegative();
    }

    public Optional<User> findUserById(String id) {
        return userDao.findById(id);
    }

    public Duration getUserSessionExpiration() {
        return USER_SESSION_EXPIRATION;
    }

    public Optional<User> updateUser(User user, String name, String email, List<String> emailAliases) {
        return userDao.update(user, name, email, emailAliases);
    }

    public Optional<User> updateFlag(String userId, UserFlag flag, boolean flagValue) {
        return userDao.updateFlag(userId, flag, flagValue);
    }

    public List<User> findAllUsers() {
        return userDao.findAll();
    }

    public List<User> findActiveTeamUsers(Team team) {
        return userDao.findByFlag(UserFlag.INACTIVE, false).stream()
                .filter(user -> user.getTeams().stream().anyMatch(userTeam -> userTeam.getTeam().equals(team)))
                .collect(toList());
    }

    public Optional<User> joinTeam(String userId, String teamId) {
        Team team = teamDao.findById(teamId).get();
        return userDao.addTeam(userId, team);
    }

    public void updateTeamFlag(String userId, String teamId, UserTeamFlag flag, boolean value) {
        Team team = teamDao.findById(teamId).get();
        userDao.updateTeamFlag(userId, team, flag, value);
    }

    public List<User> findByNamePrefix(String prefix, int limit) {
        return userDao.findByNamePrefix(prefix, limit);
    }
}
