package com.benromberg.cordonbleu.main.resource.commit;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitFileResponse {
    @JsonProperty
    private List<ReadDiffFragmentResponse> pathFragments;

    @JsonProperty
    private List<ReadLineResponse> codeLines;

    @JsonProperty
    private Optional<String> beforePath;

    @JsonProperty
    private Optional<String> afterPath;

    @JsonCreator
    public ReadCommitFileResponse(List<ReadDiffFragmentResponse> pathFragments, List<ReadLineResponse> codeLines,
            Optional<String> beforePath, Optional<String> afterPath) {
        this.pathFragments = pathFragments;
        this.codeLines = codeLines;
        this.beforePath = beforePath;
        this.afterPath = afterPath;
    }

    public List<ReadDiffFragmentResponse> getPathFragments() {
        return pathFragments;
    }

    public List<ReadLineResponse> getCodeLines() {
        return codeLines;
    }

    public Optional<String> getBeforePath() {
        return beforePath;
    }

    public Optional<String> getAfterPath() {
        return afterPath;
    }
}
