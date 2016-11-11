package com.benromberg.cordonbleu.service.comment;

import static com.benromberg.cordonbleu.data.model.CommentFixture.COMMENT_USER;
import static com.benromberg.cordonbleu.data.model.CommentFixture.COMMENT_USER_EMAIL;
import static com.benromberg.cordonbleu.data.model.CommentFixture.COMMENT_USER_NAME;
import static com.benromberg.cordonbleu.service.user.PasswordAuthenticationTest.PASSWORD_AUTHENTICATION_TEST_INSTANCE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock;
import com.benromberg.cordonbleu.service.email.EmailRule;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.service.coderepository.CommitFile;
import com.benromberg.cordonbleu.service.coderepository.CommitFileContent;
import com.benromberg.cordonbleu.service.comment.CommentEmailService;
import com.benromberg.cordonbleu.service.commit.CommitHighlightServiceMock;
import com.benromberg.cordonbleu.service.diff.DiffViewService;
import com.benromberg.cordonbleu.service.highlight.TextHighlightService;
import com.benromberg.cordonbleu.service.user.UserService;

public class CommentEmailServiceTest implements CommitFixture {
    private static final String COMMIT_MESSAGE_AFTER_NEWLINES = "with-newlines";
    private static final CommitId OTHER_COMMIT_ID = new CommitId("other-hash", TEAM);
    private static final String COMMIT_EMAIL_OTHER = "commit@email.other";
    private static final String COMMENT_COMMIT_REFERENCE = "67f4509c06348292f93be9b23ecb43445fec3f09";
    private static final String OTHER_COMMENTER_EMAIL = "other_commenter@email.com";
    private static final String COMMENT_TEXT = "comment text";

    @Rule
    public DaoRule databaseRule = new DaoRule().withCommit().withCommentUser();

    @Rule
    public final EmailRule emailRule = new EmailRule();

    private final UserDao userDao = databaseRule.createUserDao();
    private final CommitDao commitDao = databaseRule.createCommitDao();
    private CommentEmailService service;
    private final User otherCommenter = createUser(OTHER_COMMENTER_EMAIL);

    @Before
    public void setUp() {
        service = new CommentEmailService(emailRule.getEmailService(), new UserService(
                PASSWORD_AUTHENTICATION_TEST_INSTANCE, userDao, databaseRule.createUserSessionDao(),
                databaseRule.createTeamDao()), new DiffViewService(), new TextHighlightService(() -> Long.MAX_VALUE,
                userDao), new CommitHighlightServiceMock(CommitFile.changed(CodeRepositoryMock.COMMIT_PATH_BEFORE,
                CodeRepositoryMock.COMMIT_PATH_AFTER, CommitFileContent.ofSource("file content before"),
                CommitFileContent.ofSource("file content after"))), 1);
    }

    @Test
    public void sendNotificationEmail_SendsNoEmailToUnknownCommitAuthor() throws Exception {
        queueAndSendNotificationEmails(COMMIT, createComment(COMMENT_USER));
        assertThat(emailRule.getReceivedMessages()).isEmpty();
    }

    @Test
    public void sendNotificationEmail_SendsEmailToKnownCommitAuthor() throws Exception {
        createUser(COMMIT_AUTHOR_EMAIL);
        queueAndSendNotificationEmails(COMMIT, createComment(COMMENT_USER));
        assertThat(emailRule.getRecipientsTo()).contains(COMMIT_AUTHOR_EMAIL);
    }

    @Test
    public void sendNotificationEmail_WithNewlinesInCommitMessage_SendsOnlyFirstLineAsSubject() throws Exception {
        createUser(COMMIT_AUTHOR_EMAIL);
        Commit commit = commit().id(OTHER_COMMIT_ID).message(COMMIT_MESSAGE + "\n\n" + COMMIT_MESSAGE_AFTER_NEWLINES)
                .build();
        commitDao.insert(commit);
        queueAndSendNotificationEmails(commit, createComment(COMMENT_USER));
        assertThat(emailRule.getPlainBody()).doesNotStartWith(COMMIT_MESSAGE_AFTER_NEWLINES);
    }

    @Test
    public void sendNotificationEmail_SendsEmailToKnownCommitAuthor_WithAlias() throws Exception {
        User committer = createUser(COMMIT_EMAIL_OTHER);
        userDao.update(committer, committer.getName(), committer.getEmail(), asList(COMMIT_AUTHOR_EMAIL));
        queueAndSendNotificationEmails(COMMIT, createComment(COMMENT_USER));
        assertThat(emailRule.getRecipientsTo()).contains(COMMIT_EMAIL_OTHER);
    }

    @Test
    public void sendNotificationEmail_SendsEmailToOtherCommentAuthor() throws Exception {
        Commit commit = commitDao.addComment(COMMIT.getId(), createComment(otherCommenter)).get();
        queueAndSendNotificationEmails(commit, createComment(COMMENT_USER));
        assertThat(emailRule.getRecipientsTo()).contains(OTHER_COMMENTER_EMAIL);
    }

    @Test
    public void sendNotificationEmail_SendsEmailToReferencedUser() throws Exception {
        queueAndSendNotificationEmails(COMMIT, createComment("@" + otherCommenter.getName()));
        assertThat(emailRule.getRecipientsTo()).contains(OTHER_COMMENTER_EMAIL);
    }

    @Test
    public void sendNotificationEmail_SendsNoEmailToCommentAuthorOnRepeatedComment() throws Exception {
        Commit commit = commitDao.addComment(COMMIT.getId(), createComment(COMMENT_USER)).get();
        queueAndSendNotificationEmails(commit, createComment(COMMENT_USER));
        assertThat(emailRule.getReceivedMessages()).isEmpty();
    }

    @Test
    public void sendNotificationEmail_SendsEmailWithReplyToCommentAuthor() throws Exception {
        createUser(COMMIT_AUTHOR_EMAIL);
        queueAndSendNotificationEmails(COMMIT, createComment(COMMENT_USER));
        assertThat(emailRule.getReplyTo()).isEqualTo(COMMENT_USER_EMAIL);
    }

    @Test
    public void sendNotificationEmail_SendsEmailWithProperCommitLinks() throws Exception {
        createUser(COMMIT_AUTHOR_EMAIL);
        queueAndSendNotificationEmails(COMMIT, createComment(COMMENT_COMMIT_REFERENCE));
        String expectedCommitPath = EmailRule.EMAIL_ROOT_PATH + "/team/" + TEAM_NAME + "/commit/"
                + COMMENT_COMMIT_REFERENCE;
        assertThat(emailRule.getHtmlBody()).contains(expectedCommitPath);
    }

    @Test
    public void sendNotificationEmail_SendsEmailContainingRelevantDetails() throws Exception {
        createUser(COMMIT_AUTHOR_EMAIL);
        queueAndSendNotificationEmails(COMMIT, createComment(COMMENT_TEXT));
        assertEmailContent(emailRule.getPlainBody());
        assertEmailContent(emailRule.getHtmlBody());
    }

    private void assertEmailContent(String emailBody) {
        assertThat(emailBody).contains(REPOSITORY_NAME);
        assertThat(emailBody).contains(COMMIT_HASH);
        assertThat(emailBody).contains(TEAM_NAME);
        assertThat(emailBody).contains(COMMIT_BRANCH);
        assertThat(emailBody).contains(COMMIT_AUTHOR_NAME);
        assertThat(emailBody).contains(COMMIT_CREATED.toString());
        assertThat(emailBody).contains(COMMIT_MESSAGE);
        assertThat(emailBody).contains(COMMENT_TEXT);
        assertThat(emailBody).contains(COMMENT_USER_NAME);
    }

    private void queueAndSendNotificationEmails(Commit commit, Comment comment) {
        service.sendNotificationEmail(commit, comment);
        emailRule.sendQueuedEmails();
    }

    private Comment createComment(User user) {
        return new Comment(user, COMMENT_TEXT, new CommitFilePath(Optional.of(CodeRepositoryMock.COMMIT_PATH_BEFORE),
                Optional.of(CodeRepositoryMock.COMMIT_PATH_AFTER)), new CommitLineNumber(Optional.empty(),
                Optional.of(1)));
    }

    private Comment createComment(String text) {
        return new Comment(COMMENT_USER, text, new CommitFilePath(Optional.of(CodeRepositoryMock.COMMIT_PATH_BEFORE),
                Optional.of(CodeRepositoryMock.COMMIT_PATH_AFTER)), new CommitLineNumber(Optional.empty(),
                Optional.of(1)));
    }

    private User createUser(String email) {
        User user = new User(email, UUID.randomUUID().toString().substring(0, 16), "user password");
        userDao.insert(user);
        return user;
    }

}
