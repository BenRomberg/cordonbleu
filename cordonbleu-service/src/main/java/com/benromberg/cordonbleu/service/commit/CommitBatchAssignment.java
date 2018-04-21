package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.User;

import java.util.List;

public class CommitBatchAssignment {

    private final User assignee;

    private final CommitAuthor commitAuthor;

    private final List<Commit> commits;

    public CommitBatchAssignment(User assignee, CommitAuthor commitAuthor, List<Commit> commits) {
        this.assignee = assignee;
        this.commitAuthor = commitAuthor;
        this.commits = commits;
    }

    public User getAssignee() {
        return assignee;
    }

    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    public List<Commit> getCommits() {
        return commits;
    }
}
