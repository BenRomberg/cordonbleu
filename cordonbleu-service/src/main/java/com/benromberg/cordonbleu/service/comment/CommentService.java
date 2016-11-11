package com.benromberg.cordonbleu.service.comment;

import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;

import java.util.Optional;

import javax.inject.Inject;

import com.benromberg.cordonbleu.service.commit.CommitHighlightService;

public class CommentService {
    private final CommitDao commitDao;
    private final CommentEmailService commentEmailService;
    private final CommitHighlightService commitHighlightService;

    @Inject
    public CommentService(CommitDao commitDao, CommentEmailService commentEmailService,
            CommitHighlightService commitHighlightService) {
        this.commitDao = commitDao;
        this.commentEmailService = commentEmailService;
        this.commitHighlightService = commitHighlightService;
    }

    public Optional<Commit> addComment(Commit commit, Comment comment) {
        sendNotificationEmail(commit, comment);
        commitHighlightService.updateComment(commit.getId(), comment.getId(), comment.getText());
        return commitDao.addComment(commit.getId(), comment);
    }

    private void sendNotificationEmail(Commit commit, Comment comment) {
        commentEmailService.sendNotificationEmail(commit, comment);
    }

    public Optional<Commit> updateComment(Commit commit, String commentId, String text) {
        commitHighlightService.updateComment(commit.getId(), commentId, text);
        return commitDao.updateComment(commit.getId(), commentId, text);
    }

    public Optional<Commit> removeComment(Commit commit, String commentId) {
        return commitDao.removeComment(commit.getId(), commentId);
    }

    public Optional<Comment> findComment(Commit commit, String commentId) {
        return commit.getComments().stream().filter(comment -> comment.getId().equals(commentId)).findFirst();
    }

}
