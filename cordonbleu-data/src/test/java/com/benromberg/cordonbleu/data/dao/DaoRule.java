package com.benromberg.cordonbleu.data.dao;

import com.benromberg.cordonbleu.data.migration.TestMigration;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.UserFixture;
import com.benromberg.cordonbleu.data.testutil.DatabaseRule;
import com.benromberg.cordonbleu.data.validation.NameValidationMock;
import com.benromberg.cordonbleu.data.validation.UserValidationMock;

import java.util.Optional;

import com.benromberg.cordonbleu.data.dao.CodeRepositoryMetadataDao;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.CommitHighlightCacheDao;
import com.benromberg.cordonbleu.data.dao.DatabaseWithMigration;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.dao.UserSessionDao;
import com.benromberg.cordonbleu.data.migration.MongoMigration;

public class DaoRule extends DatabaseRule implements CommitFixture, CommentFixture, UserFixture {
    private Optional<UserDao> userDao = Optional.empty();
    private Optional<UserSessionDao> userSessionDao = Optional.empty();
    private Optional<CodeRepositoryMetadataDao> repositoryDao = Optional.empty();
    private Optional<TeamDao> teamDao = Optional.empty();
    private Optional<CommitDao> commitDao = Optional.empty();
    private Optional<CommitHighlightCacheDao> commitHighlightCacheDao = Optional.empty();
    private MongoMigration migration = new NullMongoMigration();

    @Override
    public DaoRule withRealMongo() {
        super.withRealMongo();
        return this;
    }

    @Override
    public DaoRule withDropIndexes() {
        super.withDropIndexes();
        return this;
    }

    public DaoRule withMigration() {
        migration = new TestMigration();
        return this;
    }

    public DatabaseWithMigration getDatabaseWithMigration() {
        return new DatabaseWithMigration(getDB(), migration);
    }

    public UserSessionDao createUserSessionDao() {
        if (!userSessionDao.isPresent()) {
            userSessionDao = Optional.of(new UserSessionDao(getDatabaseWithMigration(), createUserDao()));
        }
        return userSessionDao.get();
    }

    public UserDao createUserDao() {
        if (!userDao.isPresent()) {
            userDao = Optional.of(new UserDao(getDatabaseWithMigration(), new UserValidationMock(), createTeamDao()));
        }
        return userDao.get();
    }

    public CodeRepositoryMetadataDao createRepositoryDao() {
        if (!repositoryDao.isPresent()) {
            repositoryDao = Optional.of(new CodeRepositoryMetadataDao(getDatabaseWithMigration(),
                    new NameValidationMock<>(), createTeamDao()));
        }
        return repositoryDao.get();
    }

    public TeamDao createTeamDao() {
        if (!teamDao.isPresent()) {
            teamDao = Optional.of(new TeamDao(getDatabaseWithMigration(), new NameValidationMock<>()));
        }
        return teamDao.get();
    }

    public CommitDao createCommitDao() {
        if (!commitDao.isPresent()) {
            commitDao = Optional.of(new CommitDao(getDatabaseWithMigration(), createRepositoryDao(), createUserDao(),
                    createTeamDao()));
        }
        return commitDao.get();
    }

    public CommitHighlightCacheDao createCommitHighlightCacheDao() {
        if (!commitHighlightCacheDao.isPresent()) {
            commitHighlightCacheDao = Optional.of(new CommitHighlightCacheDao(getDatabaseWithMigration(),
                    createUserDao(), createTeamDao()));
        }
        return commitHighlightCacheDao.get();
    }

    public DaoRule withUser() {
        createUserDao().insertIfNotExists(USER);
        return this;
    }

    public DaoRule withTeam() {
        createTeamDao().insertIfNotExists(TEAM);
        return this;
    }

    public DaoRule withRepository() {
        withTeam();
        createRepositoryDao().insertIfNotExists(REPOSITORY);
        return this;
    }

    public DaoRule withCommit() {
        withRepository();
        createCommitDao().insertIfNotExists(COMMIT);
        return this;
    }

    public DaoRule withCommentUser() {
        createUserDao().insertIfNotExists(COMMENT_USER);
        return this;
    }

    public static class NullMongoMigration extends MongoMigration {
        public NullMongoMigration() {
            super(null, null);
        }

        @Override
        public void run() {
        }
    }
}