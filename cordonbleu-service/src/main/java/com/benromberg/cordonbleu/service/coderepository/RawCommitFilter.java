package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.dao.CommitFilter;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;

import java.util.List;
import java.util.Optional;

public class RawCommitFilter {
    private final List<CommitAuthor> authors;
    private final boolean approved;
    private final boolean collectiveReview;
    private final Optional<String> lastCommitHash;
    private final int limit;
    private final List<String> userIds;

    public RawCommitFilter(List<CommitAuthor> authors, List<String> userIds, boolean approved,
            Optional<String> lastCommitHash, int limit, boolean collectiveReview) {
        this.authors = authors;
        this.userIds = userIds;
        this.approved = approved;
        this.lastCommitHash = lastCommitHash;
        this.limit = limit;
        this.collectiveReview=collectiveReview;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public CommitFilter toCommitFilter(Team team, List<CodeRepositoryMetadata> repositories, List<User> users) {
        return new CommitFilter(team, repositories, authors, users, approved, lastCommitHash, limit, collectiveReview);
    }
}
