package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.CommitId;

import java.util.List;

public class PullResult {
    private final List<CommitWithRepository> newCommits;
    private final List<CommitId> removedCommitIds;

    public PullResult(List<CommitWithRepository> newCommits, List<CommitId> removedCommitIds) {
        this.newCommits = newCommits;
        this.removedCommitIds = removedCommitIds;
    }

    public List<CommitWithRepository> getNewCommits() {
        return newCommits;
    }

    public List<CommitId> getRemovedCommitIds() {
        return removedCommitIds;
    }
}
