package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CommitFilePath;

import java.util.Optional;

public class CommitFile {
    private final Optional<CommitFileState> stateBefore;
    private final Optional<CommitFileState> stateAfter;
    private final CommitFilePath commitFilePath;

    private CommitFile(CommitFilePath commitFilePath, Optional<CommitFileContent> contentBefore,
            Optional<CommitFileContent> contentAfter) {
        this.commitFilePath = commitFilePath;
        this.stateBefore = commitFilePath.getBeforePath().map(path -> new CommitFileState(path, contentBefore.get()));
        this.stateAfter = commitFilePath.getAfterPath().map(path -> new CommitFileState(path, contentAfter.get()));
    }

    public static CommitFile added(String path, CommitFileContent content) {
        return new CommitFile(new CommitFilePath(Optional.empty(), Optional.of(path)), Optional.empty(),
                Optional.of(content));
    }

    public static CommitFile removed(String path, CommitFileContent content) {
        return new CommitFile(new CommitFilePath(Optional.of(path), Optional.empty()), Optional.of(content),
                Optional.empty());
    }

    public static CommitFile changed(String pathBefore, String pathAfter, CommitFileContent contentBefore,
            CommitFileContent contentAfter) {
        return new CommitFile(new CommitFilePath(Optional.of(pathBefore), Optional.of(pathAfter)),
                Optional.of(contentBefore), Optional.of(contentAfter));
    }

    public CommitFilePath getCommitFilePath() {
        return commitFilePath;
    }

    public Optional<CommitFileState> getStateBefore() {
        return stateBefore;
    }

    public Optional<CommitFileState> getStateAfter() {
        return stateAfter;
    }

}
