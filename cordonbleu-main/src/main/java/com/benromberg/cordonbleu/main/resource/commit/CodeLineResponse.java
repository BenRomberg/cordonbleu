package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;
import com.benromberg.cordonbleu.service.diff.DiffStatus;
import com.benromberg.cordonbleu.service.diff.DiffViewCodeLine;

import java.util.List;
import java.util.Optional;

import com.benromberg.cordonbleu.main.resource.comment.CommentEnhancer;
import com.benromberg.cordonbleu.main.resource.comment.CommentResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeLineResponse {
    private final DiffViewCodeLine line;
    private final List<CommentResponse> comments;

    public CodeLineResponse(List<CommentResponse> comments, HighlightedCommitFile file, DiffViewCodeLine line) {
        this.line = line;
        this.comments = CommentEnhancer.extractCommentsForLine(comments, file.getPath(), line.getCommitLineNumber());
    }

    @JsonProperty
    public String getHighlightedCode() {
        return line.getHighlightedCode();
    }

    @JsonProperty
    public Optional<Integer> getBeforeLineNumber() {
        return line.getCommitLineNumber().getBeforeLineNumber();
    }

    @JsonProperty
    public Optional<Integer> getAfterLineNumber() {
        return line.getCommitLineNumber().getAfterLineNumber();
    }

    @JsonProperty
    public DiffStatus getStatus() {
        return line.getStatus();
    }

    @JsonProperty
    public List<CommentResponse> getComments() {
        return comments;
    }
}
