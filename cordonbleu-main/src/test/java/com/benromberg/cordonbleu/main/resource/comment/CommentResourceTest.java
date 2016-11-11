package com.benromberg.cordonbleu.main.resource.comment;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.main.CordonBleuTestRule;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryMock;
import com.benromberg.cordonbleu.service.comment.CommentService;
import com.benromberg.cordonbleu.service.user.UserService;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.main.resource.comment.AddCommentRequest;
import com.benromberg.cordonbleu.main.resource.comment.DeleteCommentRequest;
import com.benromberg.cordonbleu.main.resource.comment.EditCommentRequest;

public class CommentResourceTest implements CommentFixture, CommitFixture {
    private static final String NEW_TEXT = "new text";
    private static final Optional<Integer> AFTER_LINE_NUMBER = Optional.of(1);
    private static final Optional<Integer> BEFORE_LINE_NUMBER = Optional.empty();
    private static final Optional<String> AFTER_PATH = Optional.of(CodeRepositoryMock.COMMIT_PATH_AFTER);
    private static final Optional<String> BEFORE_PATH = Optional.of(CodeRepositoryMock.COMMIT_PATH_BEFORE);

    @Rule
    @ClassRule
    public static final CordonBleuTestRule RULE = new CordonBleuTestRule().withCommit();

    private final CommentService commentService = RULE.getInstance(CommentService.class);
    private final UserService userService = RULE.getInstance(UserService.class);
    private final CommitDao commitDao = RULE.getInstance(CommitDao.class);
    private final User otherUser = userService.registerUser("other@email.com", "otherUser", "other user's password");

    @Test
    public void addComment_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Response response = RULE.post("/api/comment/add", new AddCommentRequest(COMMIT_HASH, TEAM_ID, COMMENT_TEXT,
                BEFORE_PATH, AFTER_PATH, BEFORE_LINE_NUMBER, AFTER_LINE_NUMBER));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void addComment_WithoutCommit_YieldsNotFound() throws Exception {
        commitDao.remove(COMMIT_ID);
        Response response = RULE.withAuthenticatedUser().post(
                "/api/comment/add",
                new AddCommentRequest(COMMIT_HASH, TEAM_ID, COMMENT_TEXT, BEFORE_PATH, AFTER_PATH, BEFORE_LINE_NUMBER,
                        AFTER_LINE_NUMBER));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addComment_WithWrongFilePath_YieldsNotFound() throws Exception {
        Response response = RULE.withAuthenticatedUser().post(
                "/api/comment/add",
                new AddCommentRequest(COMMIT_HASH, TEAM_ID, COMMENT_TEXT, Optional.of("non-existing path"), AFTER_PATH,
                        BEFORE_LINE_NUMBER, AFTER_LINE_NUMBER));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addComment_WithWrongLineNumber_YieldsNotFound() throws Exception {
        Response response = RULE.withAuthenticatedUser().post(
                "/api/comment/add",
                new AddCommentRequest(COMMIT_HASH, TEAM_ID, COMMENT_TEXT, BEFORE_PATH, AFTER_PATH, BEFORE_LINE_NUMBER,
                        Optional.of(2)));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addComment_WithExistingCommit_ReturnsAllCommentsOnLine() throws Exception {
        saveComment();
        Response response = RULE.withAuthenticatedUser().post(
                "/api/comment/add",
                new AddCommentRequest(COMMIT_HASH, TEAM_ID, NEW_TEXT, BEFORE_PATH, AFTER_PATH, BEFORE_LINE_NUMBER,
                        AFTER_LINE_NUMBER));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadCommentResponse> comments = response.readEntity(new GenericType<List<ReadCommentResponse>>() {
        });
        assertThat(comments).extracting(ReadCommentResponse::getText).containsExactly(COMMENT_TEXT, NEW_TEXT);
    }

    @Test
    public void editComment_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Comment comment = saveComment();
        Response response = RULE.post("/api/comment/edit", new EditCommentRequest(COMMIT_HASH, TEAM_ID,
                comment.getId(), NEW_TEXT));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void editComment_WithoutCommit_YieldsNotFound() throws Exception {
        commitDao.remove(COMMIT_ID);
        Response response = RULE.withAuthenticatedUser().post("/api/comment/edit",
                new EditCommentRequest(COMMIT_HASH, TEAM_ID, "comment id", NEW_TEXT));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void editComment_WithoutComment_YieldsNotFound() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/comment/edit",
                new EditCommentRequest(COMMIT_HASH, TEAM_ID, "comment id", NEW_TEXT));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void editComment_WithDifferentUser_YieldsForbidden() throws Exception {
        Comment comment = saveComment(otherUser);
        Response response = RULE.withAuthenticatedUser().post("/api/comment/edit",
                new EditCommentRequest(COMMIT_HASH, TEAM_ID, comment.getId(), NEW_TEXT));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void editComment_WithComment_ReturnsAllCommentsOnLine() throws Exception {
        Comment comment = saveComment();
        saveComment();
        Response response = RULE.withAuthenticatedUser().post("/api/comment/edit",
                new EditCommentRequest(COMMIT_HASH, TEAM_ID, comment.getId(), NEW_TEXT));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadCommentResponse> comments = response.readEntity(new GenericType<List<ReadCommentResponse>>() {
        });
        assertThat(comments).extracting(ReadCommentResponse::getText).containsExactly(NEW_TEXT, COMMENT_TEXT);
    }

    @Test
    public void deleteComment_WithoutAuthenticatedUser_YieldsUnauthorized() throws Exception {
        Comment comment = saveComment();
        Response response = RULE.post("/api/comment/delete",
                new DeleteCommentRequest(COMMIT_HASH, TEAM_ID, comment.getId()));
        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void deleteComment_WithoutCommit_YieldsNotFound() throws Exception {
        commitDao.remove(COMMIT_ID);
        Response response = RULE.withAuthenticatedUser().post("/api/comment/delete",
                new DeleteCommentRequest(COMMIT_HASH, TEAM_ID, "comment id"));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteComment_WithoutComment_YieldsNotFound() throws Exception {
        Response response = RULE.withAuthenticatedUser().post("/api/comment/delete",
                new DeleteCommentRequest(COMMIT_HASH, TEAM_ID, "comment id"));
        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteComment_WithDifferentUser_YieldsForbidden() throws Exception {
        Comment comment = saveComment(otherUser);
        Response response = RULE.withAuthenticatedUser().post("/api/comment/delete",
                new DeleteCommentRequest(COMMIT_HASH, TEAM_ID, comment.getId()));
        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void deleteComment_WithComment_ReturnsAllCommentsOnLine() throws Exception {
        Comment comment = saveComment();
        saveComment();
        Response response = RULE.withAuthenticatedUser().post("/api/comment/delete",
                new DeleteCommentRequest(COMMIT_HASH, TEAM_ID, comment.getId()));
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        List<ReadCommentResponse> comments = response.readEntity(new GenericType<List<ReadCommentResponse>>() {
        });
        assertThat(comments).extracting(ReadCommentResponse::getText).containsExactly(COMMENT_TEXT);
    }

    private Comment saveComment() {
        return saveComment(RULE.getAuthenticatedUser());
    }

    private Comment saveComment(User user) {
        Comment comment = new Comment(user, COMMENT_TEXT, new CommitFilePath(BEFORE_PATH, AFTER_PATH),
                new CommitLineNumber(BEFORE_LINE_NUMBER, AFTER_LINE_NUMBER));
        commentService.addComment(COMMIT, comment);
        return comment;
    }
}
