package com.benromberg.cordonbleu.service.coderepository;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitRepository;

public class CommitWithRepository {
    private final Commit commit;
    private final CommitRepository repository;

    public CommitWithRepository(Commit commit, CommitRepository repository) {
        this.commit = commit;
        this.repository = repository;
    }

    public Commit getCommit() {
        return commit;
    }

    public CommitRepository getRepository() {
        return repository;
    }

}
