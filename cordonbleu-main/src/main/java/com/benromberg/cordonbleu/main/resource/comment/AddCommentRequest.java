package com.benromberg.cordonbleu.main.resource.comment;

import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;
import com.benromberg.cordonbleu.service.commit.RawCommitId;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddCommentRequest {
    @JsonProperty
    private final String commitHash;

    @JsonProperty
    private final String teamId;

    @JsonProperty
    private final String text;

    @JsonProperty
    private final Optional<String> beforePath;

    @JsonProperty
    private final Optional<String> afterPath;

    @JsonProperty
    private final Optional<Integer> beforeLineNumber;

    @JsonProperty
    private final Optional<Integer> afterLineNumber;

    @JsonCreator
    public AddCommentRequest(String commitHash, String teamId, String text, Optional<String> beforePath,
            Optional<String> afterPath, Optional<Integer> beforeLineNumber, Optional<Integer> afterLineNumber) {
        this.commitHash = commitHash;
        this.teamId = teamId;
        this.text = text;
        this.beforePath = beforePath;
        this.afterPath = afterPath;
        this.beforeLineNumber = beforeLineNumber;
        this.afterLineNumber = afterLineNumber;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getText() {
        return text;
    }

    public RawCommitId getCommitId() {
        return new RawCommitId(commitHash, teamId);
    }

    public CommitFilePath getPath() {
        return new CommitFilePath(beforePath, afterPath);
    }

    public CommitLineNumber getLineNumber() {
        return new CommitLineNumber(beforeLineNumber, afterLineNumber);
    }
}
