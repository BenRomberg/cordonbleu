package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFixture;
import com.benromberg.cordonbleu.service.email.EmailRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class AssignmentEmailServiceTest implements CommitFixture, UserFixture {
    private static final User ASSIGNED_TO_USER = new UserBuilder().name("assign-to").email("assign-to@mail.com").build();
    private static final User ASSIGNED_BY_USER = new UserBuilder().name("assign-by").email("assign-by@mail.com").build();

    private static final String EXPECTED_COMMIT_PATH = EmailRule.EMAIL_ROOT_PATH + "/team/" + TEAM_NAME + "/commit/" + COMMIT_HASH;

    private static final String SINGLE_ASSIGNMENT_TEXT_PLAINTEXT = String.format("The commit %s was assigned to you by %s.", COMMIT_HASH,
            ASSIGNED_BY_USER.getName());

    private static final String SINGLE_ASSIGNMENT_TEXT_HTML = String.format(
            "The commit <a href=\"%s\"><b>%s</b></a> was assigned to you by %s.", EXPECTED_COMMIT_PATH, COMMIT_HASH,
            ASSIGNED_BY_USER.getName());

    private static final Commit OTHER_COMMIT = new CommitBuilder().id("otherId").build();

    private static final CommitAuthor COMMIT_AUTHOR = new CommitAuthor("authorName", "author@mail.com");

    private static final CommitBatchAssignment COMMIT_BATCH_ASSIGNMENT = new CommitBatchAssignment(ASSIGNED_TO_USER, COMMIT_AUTHOR,
            Arrays.asList(COMMIT, OTHER_COMMIT));

    @Rule
    public DaoRule databaseRule = new DaoRule().withCommit().withCommentUser();

    @Rule
    public final EmailRule emailRule = new EmailRule();

    private final UserDao userDao = databaseRule.createUserDao();
    private AssignmentEmailService service = new AssignmentEmailService(emailRule.getEmailService());

    @Before
    public void insertUser() {
        userDao.insert(ASSIGNED_TO_USER);
        userDao.insert(ASSIGNED_BY_USER);
    }

    @Test
    public void sendSingleAssignmentEmail_SendsEmailContainingRelevantDetails() throws Exception {
        service.sendSingleAssignmentEmail(COMMIT, ASSIGNED_TO_USER, ASSIGNED_BY_USER);
        emailRule.sendQueuedEmails();

        assertSingleAssignmentEmailContent(emailRule.getPlainBody(), SINGLE_ASSIGNMENT_TEXT_PLAINTEXT);
        assertSingleAssignmentEmailContent(emailRule.getHtmlBody(), SINGLE_ASSIGNMENT_TEXT_HTML);
    }

    private void assertSingleAssignmentEmailContent(String emailBody, String expectedAssignmentText) {
        assertThat(emailBody).contains(REPOSITORY_NAME);
        assertThat(emailBody).contains(COMMIT_HASH);
        assertThat(emailBody).contains(TEAM_NAME);
        assertThat(emailBody).contains(COMMIT_BRANCH);
        assertThat(emailBody).contains(COMMIT_AUTHOR_NAME);
        assertThat(emailBody).contains(COMMIT_CREATED.toString());
        assertThat(emailBody).contains(COMMIT_MESSAGE);
        assertThat(emailBody).contains(EXPECTED_COMMIT_PATH);
        assertThat(emailBody).contains(expectedAssignmentText);
    }

    @Test
    public void sendBatchAssignmentEmail() throws Exception {
        service.sendBatchAssignmentEmail(COMMIT_BATCH_ASSIGNMENT, ASSIGNED_BY_USER);
        emailRule.sendQueuedEmails();

        assertThat(emailRule.getPlainBody()).contains(ASSIGNED_BY_USER.getName());
        assertThat(emailRule.getPlainBody()).contains(COMMIT_BATCH_ASSIGNMENT.getCommitAuthor().getName());

        assertThat(emailRule.getHtmlBody()).contains(ASSIGNED_BY_USER.getName());
        assertThat(emailRule.getHtmlBody()).contains(COMMIT_BATCH_ASSIGNMENT.getCommitAuthor().getName());
        COMMIT_BATCH_ASSIGNMENT.getCommits().forEach(commit -> assertThat(emailRule.getHtmlBody()).contains(commit.getId().getHash()));
    }
}
