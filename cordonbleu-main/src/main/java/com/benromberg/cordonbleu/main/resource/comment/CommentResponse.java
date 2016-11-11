package com.benromberg.cordonbleu.main.resource.comment;

import com.benromberg.cordonbleu.service.commit.HighlightedComment;

import java.time.LocalDateTime;

import com.benromberg.cordonbleu.main.resource.team.UserResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentResponse {
    private final HighlightedComment comment;

    public CommentResponse(HighlightedComment comment) {
        this.comment = comment;
    }

    @JsonProperty
    public String getId() {
        return comment.getId();
    }

    @JsonProperty
    public UserResponse getUser() {
        return new UserResponse(comment.getUser());
    }

    @JsonProperty
    public LocalDateTime getCreated() {
        return comment.getCreated();
    }

    @JsonProperty
    public String getText() {
        return comment.getText();
    }

    @JsonProperty
    public String getTextAsHtml() {
        return comment.getHighlightedText();
    }

    public HighlightedComment getComment() {
        return comment;
    }
}
