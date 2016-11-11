package com.benromberg.cordonbleu.service.comment;

import com.benromberg.cordonbleu.service.comment.CommentEmailService;

import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;

public class CommentEmailServiceMock extends CommentEmailService {
    public CommentEmailServiceMock() {
        super(null, null, null, null, null);
    }

    @Override
    public void sendNotificationEmail(Commit commit, Comment comment) {
    }
}
