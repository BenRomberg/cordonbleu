package com.benromberg.cordonbleu.main.resource.commit;

import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;
import com.benromberg.cordonbleu.service.diff.RelevantCodeLine;

import java.util.List;
import java.util.Optional;

import com.benromberg.cordonbleu.main.resource.comment.CommentResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LineResponse {
    private Optional<LineSpacerResponse> spacer = Optional.empty();
    private Optional<CodeLineResponse> line = Optional.empty();

    public LineResponse(List<CommentResponse> comments, HighlightedCommitFile file, RelevantCodeLine codeLine) {
        if (codeLine.isSpacer()) {
            this.spacer = Optional.of(new LineSpacerResponse(codeLine.getSpacer()));
            return;
        }
        this.line = Optional.of(new CodeLineResponse(comments, file, codeLine.getLine()));
    }

    @JsonProperty
    public Optional<LineSpacerResponse> getSpacer() {
        return spacer;
    }

    @JsonProperty
    public Optional<CodeLineResponse> getLine() {
        return line;
    }

}
