package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.Commit;

import java.util.Collection;

public interface CodeRepository {
    CommitDetail getCommitDetail(Commit commit);

    PullResult pull(Collection<Commit> existingCommits);

    void remove();

}
