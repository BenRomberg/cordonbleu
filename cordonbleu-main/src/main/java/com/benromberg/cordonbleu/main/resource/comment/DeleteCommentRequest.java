package com.benromberg.cordonbleu.main.resource.comment;

import com.benromberg.cordonbleu.service.commit.RawCommitId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteCommentRequest {
    @JsonProperty
    private final String commitHash;

    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final String commentId;

    @JsonCreator
    public DeleteCommentRequest(String commitHash, String teamId, String commentId) {
        this.commitHash = commitHash;
        this.teamId = teamId;
        this.commentId = commentId;
    }

    public RawCommitId getCommitId() {
        return new RawCommitId(commitHash, teamId);
    }

    public String getCommentId() {
        return commentId;
    }

}
