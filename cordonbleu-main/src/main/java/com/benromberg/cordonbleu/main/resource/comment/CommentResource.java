package com.benromberg.cordonbleu.main.resource.comment;

import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.service.comment.CommentService;
import com.benromberg.cordonbleu.service.commit.CommitHighlightService;
import com.benromberg.cordonbleu.service.commit.HighlightedCommit;
import io.dropwizard.auth.Auth;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.benromberg.cordonbleu.main.permission.CommentPermissionGuard;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.codahale.metrics.annotation.Timed;

@Path("/comment")
@Produces(MediaType.APPLICATION_JSON)
public class CommentResource {
    private final CommentService commentService;
    private final CommentEnhancer commentEnhancer;
    private final CommitHighlightService commitHighlightService;
    private final CommentPermissionGuard permissionGuard;

    @Inject
    public CommentResource(CommentService commentService, CommentEnhancer commentEnhancer,
            CommitHighlightService commitHighlightService, CommentPermissionGuard permissionGuard) {
        this.commentService = commentService;
        this.commentEnhancer = commentEnhancer;
        this.commitHighlightService = commitHighlightService;
        this.permissionGuard = permissionGuard;
    }

    @POST
    @Path("/add")
    @Timed
    public List<CommentResponse> addComment(AddCommentRequest request, @Auth UserWithPermissions user) {
        Commit commit = permissionGuard.guardAddComment(user, request.getCommitId());
        Comment comment = new Comment(user.getUser(), request.getText(), request.getPath(), request.getLineNumber());
        Commit updatedCommit = commentService.addComment(commit, comment).get();
        return highlightCommentsForLine(updatedCommit, comment);
    }

    @POST
    @Path("/edit")
    @Timed
    public List<CommentResponse> editComment(EditCommentRequest request, @Auth UserWithPermissions user) {
        Commit commit = permissionGuard.guardChangeComment(user, request.getCommitId(), request.getCommentId());
        Commit updatedCommit = commentService.updateComment(commit, request.getCommentId(), request.getText()).get();
        return getCommentsFromSameLine(request.getCommentId(), updatedCommit.getComments(), updatedCommit);
    }

    @POST
    @Path("/delete")
    @Timed
    public List<CommentResponse> deleteComment(DeleteCommentRequest request, @Auth UserWithPermissions user) {
        Commit commit = permissionGuard.guardChangeComment(user, request.getCommitId(), request.getCommentId());
        List<Comment> oldComments = commit.getComments();
        Commit updatedCommit = commentService.removeComment(commit, request.getCommentId()).get();
        return getCommentsFromSameLine(request.getCommentId(), oldComments, updatedCommit);
    }

    private List<CommentResponse> getCommentsFromSameLine(String commentId, List<Comment> commentsToFindCommentIn,
            Commit commit) {
        Comment comment = commentsToFindCommentIn.stream().filter(item -> item.getId().equals(commentId)).findFirst()
                .get();
        return highlightCommentsForLine(commit, comment);
    }

    private List<CommentResponse> highlightCommentsForLine(Commit commit, Comment comment) {
        HighlightedCommit highlightedCommit = commitHighlightService.highlight(commit);
        return CommentEnhancer.extractCommentsForLine(commentEnhancer.convertComments(highlightedCommit),
                comment.getCommitFilePath(), comment.getCommitLineNumber());
    }

}
