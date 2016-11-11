package com.benromberg.cordonbleu.main.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.TeamFlag;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFixture;
import com.benromberg.cordonbleu.main.config.TestModule;
import com.benromberg.cordonbleu.service.commit.RawCommitId;

import javax.ws.rs.ForbiddenException;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.main.config.GuiceModule;
import com.benromberg.cordonbleu.main.permission.CommentPermissionGuard;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.util.Modules;

public class CommentPermissionGuardTest implements UserFixture, CommentFixture, CommitFixture {
    private static final RawCommitId RAW_COMMIT_ID = new RawCommitId(COMMIT_HASH, TEAM_ID);

    private final Injector INJECTOR = createInjector();

    private Injector createInjector() {
        return Guice.createInjector(Stage.DEVELOPMENT,
                Modules.override(new GuiceModule()).with(new UnitTestModule(), new TestModule()));
    }

    @Rule
    public DaoRule databaseRule = new DaoRule().withCommit().withUser().withCommentUser();

    private final CommentPermissionGuard guard = INJECTOR.getInstance(CommentPermissionGuard.class);
    private final TeamDao teamDao = databaseRule.createTeamDao();

    @Test
    public void addComment_WithPublicTeam_RegularUserHasAccess() throws Exception {
        assertThat(guard.guardAddComment(createUser(USER), RAW_COMMIT_ID)).isEqualTo(COMMIT);
    }

    @Test
    public void addComment_WithPublicTeam_AndCommentMemberOnlyFlag_RegularUserAccessForbidden() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.COMMENT_MEMBER_ONLY, true);
        assertForbidden(() -> guard.guardAddComment(createUser(USER), RAW_COMMIT_ID));
    }

    @Test
    public void addComment_WithPublicTeam_AndCommentMemberOnlyFlag_TeamUserHasAccess() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.COMMENT_MEMBER_ONLY, true);
        assertThat(guard.guardAddComment(createTeamUser(USER), RAW_COMMIT_ID)).isEqualTo(COMMIT);
    }

    @Test
    public void addComment_WithPrivateTeam_RegularUserAccessForbidden() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        assertForbidden(() -> guard.guardAddComment(createUser(USER), RAW_COMMIT_ID));
    }

    @Test
    public void addComment_WithPrivateTeam_TeamUserHasAccess() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        assertThat(guard.guardAddComment(createTeamUser(USER), RAW_COMMIT_ID)).isEqualTo(COMMIT);
    }

    @Test
    public void changeComment_WithPublicTeam_RegularUserAccessForbidden() throws Exception {
        assertForbidden(() -> guard.guardChangeComment(createUser(USER), RAW_COMMIT_ID, createComment()));
    }

    @Test
    public void changeComment_WithPublicTeam_CommentUserHasAccess() throws Exception {
        assertThat(guard.guardChangeComment(createUser(COMMENT_USER), RAW_COMMIT_ID, createComment()))
                .isEqualTo(COMMIT);
    }

    @Test
    public void changeComment_WithPublicTeam_AndCommentMemberOnlyFlag_CommentUserAccessForbidden() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.COMMENT_MEMBER_ONLY, true);
        assertForbidden(() -> guard.guardChangeComment(createUser(COMMENT_USER), RAW_COMMIT_ID, createComment()));
    }

    @Test
    public void changeComment_WithPublicTeam_AndCommentMemberOnlyFlag_CommentUserInTeamHasAccess() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.COMMENT_MEMBER_ONLY, true);
        assertThat(guard.guardChangeComment(createTeamUser(COMMENT_USER), RAW_COMMIT_ID, createComment())).isEqualTo(
                COMMIT);
    }

    @Test
    public void changeComment_WithPrivateTeam_CommentUserAccessForbidden() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        assertForbidden(() -> guard.guardChangeComment(createUser(COMMENT_USER), RAW_COMMIT_ID, createComment()));
    }

    @Test
    public void changeComment_WithPrivateTeam_CommentUserInTeamHasAccess() throws Exception {
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        assertThat(guard.guardChangeComment(createTeamUser(COMMENT_USER), RAW_COMMIT_ID, createComment())).isEqualTo(
                COMMIT);
    }

    private UserWithPermissions createUser(User user) {
        return new UserWithPermissions(user);
    }

    private UserWithPermissions createTeamUser(User user) {
        return new UserWithPermissions(databaseRule.createUserDao().addTeam(user.getId(), TEAM).get());
    }

    private String createComment() {
        databaseRule.createCommitDao().addComment(COMMIT_ID, COMMENT);
        return COMMENT_ID;
    }

    private void assertForbidden(ThrowingCallable guardCall) {
        assertThatThrownBy(guardCall).isInstanceOf(ForbiddenException.class);
    }
}
