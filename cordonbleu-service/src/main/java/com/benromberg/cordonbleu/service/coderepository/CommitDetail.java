package com.benromberg.cordonbleu.service.coderepository;

import java.util.List;

import com.benromberg.cordonbleu.data.model.Commit;

public class CommitDetail {
    private Commit commit;
    private List<CommitFile> files;

    public CommitDetail(Commit commit, List<CommitFile> files) {
        this.commit = commit;
        this.files = files;
    }

    public Commit getCommit() {
        return commit;
    }

    public List<CommitFile> getFiles() {
        return files;
    }
}
