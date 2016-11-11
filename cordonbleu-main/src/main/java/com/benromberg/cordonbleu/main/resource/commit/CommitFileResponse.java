package com.benromberg.cordonbleu.main.resource.commit;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;
import com.benromberg.cordonbleu.service.diff.DiffFragment;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommitFileResponse {
    private final List<DiffFragmentResponse> pathFragments;
    private final List<LineResponse> codeLines;
    private final HighlightedCommitFile file;

    public CommitFileResponse(HighlightedCommitFile file, List<DiffFragment> pathDiff, List<LineResponse> codeLines) {
        this.file = file;
        this.pathFragments = pathDiff.stream().map(fragment -> new DiffFragmentResponse(fragment)).collect(toList());
        this.codeLines = codeLines;
    }

    @JsonProperty
    public List<DiffFragmentResponse> getPathFragments() {
        return pathFragments;
    }

    @JsonProperty
    public List<LineResponse> getCodeLines() {
        return codeLines;
    }

    @JsonProperty
    public Optional<String> getBeforePath() {
        return file.getPath().getBeforePath();
    }

    @JsonProperty
    public Optional<String> getAfterPath() {
        return file.getPath().getAfterPath();
    }
}
