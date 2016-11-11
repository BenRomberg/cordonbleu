package com.benromberg.cordonbleu.main.resource.comment;

import com.benromberg.cordonbleu.service.commit.RawCommitId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EditCommentRequest {
    @JsonProperty
    private final String commitHash;

    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final String commentId;

    @JsonProperty
    private final String text;

    @JsonCreator
    public EditCommentRequest(String commitHash, String teamId, String commentId, String text) {
        this.commitHash = commitHash;
        this.teamId = teamId;
        this.commentId = commentId;
        this.text = text;
    }

    public RawCommitId getCommitId() {
        return new RawCommitId(commitHash, teamId);
    }

    public String getCommentId() {
        return commentId;
    }

    public String getText() {
        return text;
    }
}
