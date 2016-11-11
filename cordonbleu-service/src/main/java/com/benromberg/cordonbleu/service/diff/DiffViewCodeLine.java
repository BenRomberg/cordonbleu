package com.benromberg.cordonbleu.service.diff;

import com.benromberg.cordonbleu.data.model.CommitLineNumber;

public class DiffViewCodeLine implements RelevantCodeLine {
    private CommitLineNumber commitLineNumber;
    private final DiffStatus status;
    private final String highlightedCode;

    public DiffViewCodeLine(CommitLineNumber commitLineNumber, DiffStatus status, String highlightedCode) {
        this.commitLineNumber = commitLineNumber;
        this.status = status;
        this.highlightedCode = highlightedCode;
    }

    public CommitLineNumber getCommitLineNumber() {
        return commitLineNumber;
    }

    public DiffStatus getStatus() {
        return status;
    }

    public String getHighlightedCode() {
        return highlightedCode;
    }

    @Override
    public boolean isSpacer() {
        return false;
    }

    @Override
    public DiffViewCodeLine getLine() {
        return this;
    }

}
