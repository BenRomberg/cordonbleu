package com.benromberg.cordonbleu.main.permission;

import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.service.comment.CommentService;
import com.benromberg.cordonbleu.service.commit.CommitService;
import com.benromberg.cordonbleu.service.commit.RawCommitId;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

public class CommentPermissionGuard {
    private final CommitService commitService;
    private final CommentService commentService;

    @Inject
    public CommentPermissionGuard(CommitService commitService, CommentService commentService) {
        this.commitService = commitService;
        this.commentService = commentService;
    }

    public Commit guardAddComment(UserWithPermissions user, RawCommitId commitId) {
        Commit commit = commitService.findById(commitId).get();
        Team team = commit.getId().getTeam();
        if (!user.hasTeamPermission(TeamPermission.COMMENT, team)) {
            throw new ForbiddenException();
        }
        return commit;
    }

    public Commit guardChangeComment(UserWithPermissions user, RawCommitId commitId, String commentId) {
        Commit commit = guardAddComment(user, commitId);
        Comment comment = commentService.findComment(commit, commentId).get();
        if (!comment.getUser().equals(user.getUser())) {
            throw new ForbiddenException();
        }
        return commit;
    }
}
