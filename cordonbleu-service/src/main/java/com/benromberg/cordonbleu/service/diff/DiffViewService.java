package com.benromberg.cordonbleu.service.diff;

import static com.benromberg.cordonbleu.service.diff.DiffFragment.fromDiff;
import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.CommitFilePath;

import java.util.List;
import java.util.Optional;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatchWrapper;

import com.benromberg.cordonbleu.service.coderepository.CommitFileState;
import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;

public class DiffViewService {
    private final LineBasedDiffMerger merger = new LineBasedDiffMerger();

    public List<DiffViewCodeLine> diffCodeLines(HighlightedCommitFile highlightedFile) {
        CodeLineFactory counter = new CodeLineFactory(highlightedFile.getContentHighlightedBefore(),
                highlightedFile.getContentHighlightedAfter());
        return merger
                .merge(mapStateToContent(highlightedFile.getStateBefore()),
                        mapStateToContent(highlightedFile.getStateAfter())).stream()
                .map(codeLine -> counter.nextLine(codeLine.getStatus())).collect(toList());
    }

    private Optional<String> mapStateToContent(Optional<CommitFileState> state) {
        return state.map(CommitFileState::getContent);
    }

    public List<DiffFragment> diffPaths(CommitFilePath commitFilePath) {
        return DiffMatchPatchWrapper
                .diffWithinLine(commitFilePath.getBeforePath().orElse(""), commitFilePath.getAfterPath().orElse(""))
                .stream().map(diff -> fromDiff(diff.operation, diff.text)).collect(toList());
    }
}
