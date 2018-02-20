package com.benromberg.cordonbleu.service.commit;

import static com.benromberg.cordonbleu.service.commit.CommitNotificationActionType.APPROVE;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitApproval;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.util.ClockService;
import com.benromberg.cordonbleu.util.SystemTimeRule;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CommitServiceTest implements CommitFixture, CommentFixture {
    private static final String OTHER_COMMIT_HASH = "other-commit-hash";
    private static final String APPROVE_USER_EMAIL = "approve@user.com";
    private static final User COMMIT_USER = new User(COMMIT_AUTHOR_EMAIL, "commit-user", "password");
    private static final User APPROVE_USER = new User(APPROVE_USER_EMAIL, "approve-user", "password");

    @Rule
    public DaoRule databaseRule = new DaoRule().withCommentUser().withCommit();

    private final UserDao userDao = databaseRule.createUserDao();
    private final CommitDao dao = databaseRule.createCommitDao();
    private final CommitService service = new CommitService(dao, databaseRule.createTeamDao(), () -> 100);

    @Rule
    public SystemTimeRule systemTimeRule = new SystemTimeRule();

    @Before
    public void setUp() {
        userDao.insert(COMMIT_USER);
        userDao.insert(APPROVE_USER);
    }

    @Test
    public void findNotifications_WithOwnCommitWithoutComments_ReturnsEmptyList() throws Exception {
        CommitNotifications notifications = findNotifications(COMMIT_USER, 100);
        assertThat(notifications.getTotalPrompts()).isEqualTo(0);
        assertThat(notifications.getNotifications()).isEmpty();
    }

    @Test
    public void findNotifications_WithOwnCommitCommented_ReturnsNotificationWithPrompt() throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        findAndAssertNotifications(COMMIT_USER, true, CommitNotificationActionType.COMMENT, COMMENT_USER);
    }

    @Test
    public void findNotifications_WithOwnCommit_AndOwnCommentLast_ReturnsNotificationWithPrompt() throws Exception {
        dao.addComment(COMMIT_ID, comment().user(COMMIT_USER).build());
        findAndAssertNotifications(COMMIT_USER, true, CommitNotificationActionType.COMMENT, COMMIT_USER);
    }

    @Test
    public void findNotifications_WithOwnCommitCommentedAndApproved_ReturnsNotificationWithoutPrompt() throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        dao.updateApproval(COMMIT_ID, Optional.of(new CommitApproval(APPROVE_USER, ClockService.now())));
        findAndAssertNotifications(COMMIT_USER, false, APPROVE, APPROVE_USER);
    }

    @Test
    public void findNotifications_WithOtherUserCommit_WithOwnComment_ButLastCommentFromOtherUser_ReturnsNotificationWithPrompt()
            throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        dao.addComment(COMMIT_ID, comment().user(COMMIT_USER).build());
        findAndAssertNotifications(COMMENT_USER, true, CommitNotificationActionType.COMMENT, COMMIT_USER);
    }

    @Test
    public void findNotifications_WithOtherUserCommit_WithOwnCommentLast_ReturnsNotificationWithoutPrompt()
            throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        findAndAssertNotifications(COMMENT_USER, false, CommitNotificationActionType.COMMENT, COMMENT_USER);
    }

    @Test
    public void findNotifications_WithOtherUserCommitCommentedAndApproved_ReturnsNotificationWithoutPrompt()
            throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        dao.addComment(COMMIT_ID, comment().user(COMMIT_USER).build());
        dao.updateApproval(COMMIT_ID, Optional.of(new CommitApproval(APPROVE_USER, ClockService.now())));
        findAndAssertNotifications(COMMENT_USER, false, APPROVE, APPROVE_USER);
    }

    @Test
    public void findNotifications_WithOtherUserCommitCommentedAndAssigned_ReturnsNotificationWithoutPrompt()
            throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        dao.addComment(COMMIT_ID, comment().user(COMMIT_USER).build());
        dao.updateApproval(COMMIT_ID, Optional.of(new CommitApproval(APPROVE_USER, ClockService.now())));
        findAndAssertNotifications(COMMENT_USER, false, APPROVE, APPROVE_USER);
    }

    @Test
    public void findNotifications_WithLowLimit_ConsidersMoreCommitsThanLimit() throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        systemTimeRule.advanceBySeconds(1);
        Commit otherCommit = commit().id(OTHER_COMMIT_HASH).build();
        dao.insert(otherCommit);
        dao.addComment(otherCommit.getId(), createComment());
        CommitNotifications commitNotifications = findNotifications(COMMIT_USER, 1);
        assertThat(commitNotifications.getTotalPrompts()).isEqualTo(2);
        assertThat(commitNotifications.getNotifications()).extracting(
                notification -> notification.getCommit().getId().getHash()).containsExactly(OTHER_COMMIT_HASH);
    }

    @Test
    public void assignCommit_CommitIsAssigned() throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        service.assign(COMMIT_ID, APPROVE_USER);

        assertThat(dao.findById(COMMIT_ID).get().getAssignee().get()).isEqualToComparingFieldByField(APPROVE_USER);
    }

    @Test
    public void revertAssignment_CommitIsNotAssigned() throws Exception {
        dao.addComment(COMMIT_ID, createComment());
        service.assign(COMMIT_ID, APPROVE_USER);
        service.revertAssignment(COMMIT_ID);

        assertThat(dao.findById(COMMIT_ID).get().getAssignee()).isEmpty();
    }

    private Comment createComment() {
        return comment().build();
    }

    private CommitNotifications findNotifications(User user, int limit) {
        User userWithTeam = userDao.addTeam(user.getId(), TEAM).get();
        return service.findNotifications(userWithTeam, limit);
    }

    private void findAndAssertNotifications(User notificationUser, boolean prompt,
            CommitNotificationActionType lastActionType, User lastActionUser) {
        CommitNotifications commitNotifications = findNotifications(notificationUser, 100);
        assertThat(commitNotifications.getTotalPrompts()).isEqualTo(prompt ? 1 : 0);
        List<CommitNotification> notifications = commitNotifications.getNotifications();
        assertThat(notifications).hasSize(1);
        CommitNotification notification = notifications.get(0);
        assertThat(notification.getCommit().getId()).isEqualTo(COMMIT_ID);
        assertThat(notification.isPrompt()).isEqualTo(prompt);
        assertThat(notification.getLastAction().getType()).isEqualTo(lastActionType);
        assertThat(notification.getLastAction().getUser().getEmail()).isEqualTo(lastActionUser.getEmail());
        assertThat(notification.getLastAction().getTime()).isEqualTo(systemTimeRule.getDateTime());
    }
}
