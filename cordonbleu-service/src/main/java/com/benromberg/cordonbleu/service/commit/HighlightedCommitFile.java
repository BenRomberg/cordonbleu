package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheFile;

import java.util.List;
import java.util.Optional;

import com.benromberg.cordonbleu.service.coderepository.CommitFile;
import com.benromberg.cordonbleu.service.coderepository.CommitFileState;

public class HighlightedCommitFile {
    private final CommitFile commitFile;
    private final CommitHighlightCacheFile highlightFile;

    public HighlightedCommitFile(CommitFile commitFile, CommitHighlightCacheFile highlightFile) {
        this.commitFile = commitFile;
        this.highlightFile = highlightFile;
    }

    public Optional<CommitFileState> getStateBefore() {
        return commitFile.getStateBefore();
    }

    public Optional<CommitFileState> getStateAfter() {
        return commitFile.getStateAfter();
    }

    public CommitFilePath getPath() {
        return commitFile.getCommitFilePath();
    }

    public List<String> getContentHighlightedBefore() {
        return highlightFile.getContentHighlightedBefore();
    }

    public List<String> getContentHighlightedAfter() {
        return highlightFile.getContentHighlightedAfter();
    }

}
