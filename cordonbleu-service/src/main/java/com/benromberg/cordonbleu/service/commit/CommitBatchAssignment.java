package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.User;

import java.util.List;

public class CommitBatchAssignment {

    private final User user;

    private final List<Commit> commits;

    public CommitBatchAssignment(User user, List<Commit> commits) {
        this.user = user;
        this.commits = commits;
    }

    public User getUser() {
        return user;
    }

    public List<Commit> getCommits() {
        return commits;
    }
}
