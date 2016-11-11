package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.main.resource.comment.ReadCommentResponse;
import com.benromberg.cordonbleu.service.diff.DiffStatus;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCodeLineResponse {
    @JsonProperty
    private String highlightedCode;

    @JsonProperty
    private Optional<Integer> beforeLineNumber;

    @JsonProperty
    private Optional<Integer> afterLineNumber;

    @JsonProperty
    private DiffStatus status;

    @JsonProperty
    private List<ReadCommentResponse> comments;

    @JsonCreator
    public ReadCodeLineResponse(String highlightedCode, Optional<Integer> beforeLineNumber,
            Optional<Integer> afterLineNumber, DiffStatus status, List<ReadCommentResponse> comments) {
        this.highlightedCode = highlightedCode;
        this.beforeLineNumber = beforeLineNumber;
        this.afterLineNumber = afterLineNumber;
        this.status = status;
        this.comments = comments;
    }

    public String getHighlightedCode() {
        return highlightedCode;
    }

    public Optional<Integer> getBeforeLineNumber() {
        return beforeLineNumber;
    }

    public Optional<Integer> getAfterLineNumber() {
        return afterLineNumber;
    }

    public DiffStatus getStatus() {
        return status;
    }

    public List<ReadCommentResponse> getComments() {
        return comments;
    }
}
