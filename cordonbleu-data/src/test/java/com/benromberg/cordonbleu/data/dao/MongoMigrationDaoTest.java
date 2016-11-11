package com.benromberg.cordonbleu.data.dao;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.CommitHighlightCacheDao;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.dao.UserSessionDao;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitHighlightCache;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheFile;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserSession;
import com.benromberg.cordonbleu.data.model.UserTeam;
import com.benromberg.cordonbleu.data.util.MongoCommand;
import com.mongodb.DBObject;

public class MongoMigrationDaoTest implements MongoCommand {
    private static final List<String> COMMIT_HIGHLIGHT_AFTER_CONTENT = asList("commit-highlight-after-content");
    private static final List<String> COMMIT_HIGHLIGHT_BEFORE_CONTENT = asList("commit-highlight-before-content");
    private static final String COMMIT_HIGHLIGHT_COMMENT_TEXT = "commit-highlight-comment-text";
    private static final String COMMIT_HIGHLIGHT_COMMENT_ID = "commit-highlight-comment-id";
    private static final String COMMIT_HIGHLIGHT_MESSAGE = "commit highlight message";
    private static final int COMMIT_HIGHLIGHT_VERSION = 1;
    private static final String TEAM_NAME = "default-team";
    private static final int COMMIT_COMMENT_BEFORE_LINE_NUMBER = 1;
    private static final String COMMIT_COMMENT_AFTER_PATH = "commit-comment-after.path";
    private static final String COMMIT_COMMENT_BEFORE_PATH = "commit-comment-before.path";
    private static final String COMMIT_COMMENT_TEXT = "commit comment text";
    private static final LocalDateTime COMMIT_COMMENT_CREATED = LocalDateTime.parse("2013-12-03T10:15:30");
    private static final String COMMIT_COMMENT_ID = "commit-comment-id";
    private static final LocalDateTime COMMIT_APPROVAL_TIME = LocalDateTime.parse("2011-02-03T10:15:30");
    private static final String COMMIT_BRANCH = "commit-branch";
    private static final String COMMIT_MESSAGE = "commit message";
    private static final LocalDateTime COMMIT_CREATED = LocalDateTime.parse("2010-12-03T10:15:30");
    private static final String COMMIT_AUTHOR_EMAIL = "commit_author@email.com";
    private static final String COMMIT_AUTHOR = "commit author";
    private static final String COMMIT_ID = "commit-id";
    private static final String USER_SESSION_ID = "user-session-id";
    private static final LocalDateTime USER_SESSION_CREATED = LocalDateTime.parse("2007-12-03T10:15:30");
    private static final String REPOSITORY_SOURCE_URL = "http://repository.source/url/";
    private static final String REPOSITORY_NAME = "repository name";
    private static final String REPOSITORY_ID = "repository-id";
    private static final String USER_ENCRYPTED_PASSWORD = "user encrypted password";
    private static final String USER_EMAIL = "user@email.com";
    private static final String USER_ID = "user-id";

    @Rule
    public DaoRule databaseRule = new DaoRule().withMigration();

    private CodeRepositoryMetadataDao repositoryDao;
    private UserDao userDao;
    private UserSessionDao userSessionDao;
    private CommitDao commitDao;
    private TeamDao teamDao;
    private CommitHighlightCacheDao commitHighlightCacheDao;

    @Test
    public void applyingAllMigrations_DoesNotCorruptDatabase() throws Exception {
        insert("user", createOriginalUser());
        insert("user_session", createOriginalUserSession());
        insert("codeRepositoryMetadata", createOriginalRepository());
        insert("commit", createOriginalCommit());
        insert("commitHighlightCache", createOriginalCommitHighlightCache());

        createDaosTriggeringMigrations();

        Team team = assertTeam();
        assertUser(team);
        assertUserSession();
        assertRepository(team);
        assertCommit(team);
        assertCommitHighlightCache(team);
    }

    @Test
    public void applyingAllMigrations_OnEmptyDatabase_LeavesDatabaseEmpty() throws Exception {
        createDaosTriggeringMigrations();

        assertEmptyCollection(userDao.getCollectionName());
        assertEmptyCollection(userSessionDao.getCollectionName());
        assertEmptyCollection(repositoryDao.getCollectionName());
        assertEmptyCollection(commitDao.getCollectionName());
        assertEmptyCollection(teamDao.getCollectionName());
        assertEmptyCollection(commitHighlightCacheDao.getCollectionName());
    }

    private void createDaosTriggeringMigrations() {
        teamDao = databaseRule.createTeamDao();
        repositoryDao = databaseRule.createRepositoryDao();
        userDao = databaseRule.createUserDao();
        userSessionDao = databaseRule.createUserSessionDao();
        commitDao = databaseRule.createCommitDao();
        commitHighlightCacheDao = databaseRule.createCommitHighlightCacheDao();
    }

    private Team assertTeam() {
        List<Team> teams = teamDao.findPublic();
        assertThat(teams).extracting(Team::getName).containsExactly(TEAM_NAME);
        return teams.get(0);
    }

    private void assertCommit(Team team) {
        Commit commit = commitDao.findById(new CommitId(COMMIT_ID, team)).get();
        assertThat(commit.getAuthor().getName()).isEqualTo(COMMIT_AUTHOR);
        assertThat(commit.getAuthor().getEmail()).isEqualTo(COMMIT_AUTHOR_EMAIL);
        assertThat(commit.getCreated()).isEqualTo(COMMIT_CREATED);
        assertThat(commit.getMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(commit.getRepositories()).extracting(repository -> repository.getRepository().getId(),
                CommitRepository::getBranches).containsExactly(tuple(REPOSITORY_ID, asList(COMMIT_BRANCH)));
        assertThat(commit.getApproval().get().getApprover().getId()).isEqualTo(USER_ID);
        assertThat(commit.getApproval().get().getTime()).isEqualTo(COMMIT_APPROVAL_TIME);
        assertThat(commit.getComments()).hasSize(1);
        assertCommitComment(commit.getComments().get(0));
        assertThat(commit.isRemoved()).isFalse();
    }

    private void assertCommitComment(Comment comment) {
        assertThat(comment.getCreated()).isEqualTo(COMMIT_COMMENT_CREATED);
        assertThat(comment.getText()).isEqualTo(COMMIT_COMMENT_TEXT);
        assertThat(comment.getUser().getId()).isEqualTo(USER_ID);
        assertThat(comment.getCommitFilePath().getBeforePath()).hasValue(COMMIT_COMMENT_BEFORE_PATH);
        assertThat(comment.getCommitFilePath().getAfterPath()).hasValue(COMMIT_COMMENT_AFTER_PATH);
        assertThat(comment.getCommitLineNumber().getBeforeLineNumber()).hasValue(COMMIT_COMMENT_BEFORE_LINE_NUMBER);
        assertThat(comment.getCommitLineNumber().getAfterLineNumber()).isEmpty();
    }

    private void assertRepository(Team team) {
        CodeRepositoryMetadata repository = repositoryDao.findById(REPOSITORY_ID).get();
        assertThat(repository.getName()).isEqualTo(REPOSITORY_NAME);
        assertThat(repository.getSourceUrl()).isEqualTo(REPOSITORY_SOURCE_URL);
        assertThat(repository.isRemoveOnNextUpdate()).isFalse();
        assertThat(repository.getTeam()).isEqualTo(team);
    }

    private void assertUserSession() {
        UserSession userSession = userSessionDao.findById(USER_SESSION_ID).get();
        assertThat(userSession.getCreated()).isEqualTo(USER_SESSION_CREATED);
        assertThat(userSession.getUser().getId()).isEqualTo(USER_ID);
    }

    private void assertUser(Team team) {
        User user = userDao.findById(USER_ID).get();
        assertThat(user.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(user.getEncryptedPassword()).isEqualTo(USER_ENCRYPTED_PASSWORD);
        assertThat(user.getName()).isEqualTo(USER_ID);
        assertThat(user.getEmailAliases()).isEmpty();
        assertThat(user.isAdmin()).isFalse();
        assertThat(user.isInactive()).isFalse();
        assertThat(user.getTeams()).extracting(UserTeam::getTeam, UserTeam::isOwner).containsExactly(tuple(team, true));
    }

    private void assertCommitHighlightCache(Team team) {
        CommitHighlightCache commitHighlight = commitHighlightCacheDao.findById(new CommitId(COMMIT_ID, team)).get();
        assertThat(commitHighlight.getVersion()).isEqualTo(COMMIT_HIGHLIGHT_VERSION);
        assertThat(commitHighlight.getFiles()).extracting(CommitHighlightCacheFile::getContentHighlightedBefore,
                CommitHighlightCacheFile::getContentHighlightedAfter).containsExactly(
                tuple(COMMIT_HIGHLIGHT_BEFORE_CONTENT, COMMIT_HIGHLIGHT_AFTER_CONTENT));
        assertCommitHighlightText(commitHighlight.getMessage(), COMMIT_HIGHLIGHT_MESSAGE);
        assertThat(commitHighlight.getComments()).containsOnlyKeys(COMMIT_HIGHLIGHT_COMMENT_ID);
        assertCommitHighlightText(commitHighlight.getComments().get(COMMIT_HIGHLIGHT_COMMENT_ID),
                COMMIT_HIGHLIGHT_COMMENT_TEXT);
    }

    private void assertCommitHighlightText(CommitHighlightCacheText commitHighlightText, String message) {
        assertThat(commitHighlightText.getText()).isEqualTo(message);
        assertThat(commitHighlightText.getUserReferences()).extracting(User::getId).containsExactly(USER_ID);
    }

    private DBObject createOriginalCommitHighlightCache() {
        return object(ID_PROPERTY, COMMIT_ID)
                .append("version", COMMIT_HIGHLIGHT_VERSION)
                .append("files", asList(createOriginalCommitHighlightCacheFile()))
                .append("message", createCommitHighlightCacheText(COMMIT_HIGHLIGHT_MESSAGE))
                .append("comments",
                        object(COMMIT_HIGHLIGHT_COMMENT_ID,
                                createCommitHighlightCacheText(COMMIT_HIGHLIGHT_COMMENT_TEXT)));
    }

    private DBObject createCommitHighlightCacheText(String text) {
        return object("text", text).append("userReferences", asList(USER_ID));
    }

    private DBObject createOriginalCommitHighlightCacheFile() {
        return object("beforeContentHighlighted", COMMIT_HIGHLIGHT_BEFORE_CONTENT).append("afterContentHighlighted",
                COMMIT_HIGHLIGHT_AFTER_CONTENT);
    }

    private DBObject createOriginalCommit() {
        return object(ID_PROPERTY, COMMIT_ID).append("repository", REPOSITORY_ID).append("author", COMMIT_AUTHOR)
                .append("authorEmail", COMMIT_AUTHOR_EMAIL).append("created", date(COMMIT_CREATED))
                .append("message", COMMIT_MESSAGE).append("branches", asList(COMMIT_BRANCH))
                .append("approval", createOriginalCommitApproval())
                .append("comments", asList(createOriginalCommitComment()));
    }

    private Object createOriginalCommitComment() {
        return object(ID_PROPERTY, COMMIT_COMMENT_ID)
                .append("created", date(COMMIT_COMMENT_CREATED))
                .append("user", USER_ID)
                .append("text", COMMIT_COMMENT_TEXT)
                .append("commitFilePath",
                        object("beforePath", COMMIT_COMMENT_BEFORE_PATH).append("afterPath", COMMIT_COMMENT_AFTER_PATH))
                .append("commitLineNumber",
                        object("beforeLineNumber", COMMIT_COMMENT_BEFORE_LINE_NUMBER).append("afterLineNumber", null));
    }

    private Object createOriginalCommitApproval() {
        return object("approver", USER_ID).append("time", date(COMMIT_APPROVAL_TIME));
    }

    private DBObject createOriginalUserSession() {
        return object(ID_PROPERTY, USER_SESSION_ID).append("user", USER_ID).append("created",
                date(USER_SESSION_CREATED));
    }

    private DBObject createOriginalRepository() {
        return object(ID_PROPERTY, REPOSITORY_ID).append("name", REPOSITORY_NAME).append("sourceUrl",
                REPOSITORY_SOURCE_URL);
    }

    private void insert(String collectionName, DBObject entity) {
        DaoRule.getDB().getCollection(collectionName).insert(entity);
    }

    private void assertEmptyCollection(String collectionName) {
        assertThat(DaoRule.getDB().getCollection(collectionName).count()).isEqualTo(0);
    }

    private DBObject createOriginalUser() {
        return object(ID_PROPERTY, USER_ID).append("email", USER_EMAIL).append("encryptedPassword",
                USER_ENCRYPTED_PASSWORD);
    }

    private Date date(LocalDateTime dateTime) {
        return new Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
    }
}
