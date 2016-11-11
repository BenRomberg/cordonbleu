package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;
import com.benromberg.cordonbleu.data.model.User;

import java.time.LocalDateTime;

public class HighlightedComment {
    private final Comment comment;
    private final CommitHighlightCacheText commitHighlightCacheText;

    public HighlightedComment(Comment comment, CommitHighlightCacheText commitHighlightCacheText) {
        this.comment = comment;
        this.commitHighlightCacheText = commitHighlightCacheText;
    }

    public CommitFilePath getCommitFilePath() {
        return comment.getCommitFilePath();
    }

    public CommitLineNumber getCommitLineNumber() {
        return comment.getCommitLineNumber();
    }

    public User getUser() {
        return comment.getUser();
    }

    public String getText() {
        return comment.getText();
    }

    public LocalDateTime getCreated() {
        return comment.getCreated();
    }

    public String getId() {
        return comment.getId();
    }

    public String getHighlightedText() {
        return commitHighlightCacheText.getText();
    }

}
