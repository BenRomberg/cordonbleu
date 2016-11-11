package com.benromberg.cordonbleu.service.commit;

import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.model.CommitId;

public class RawCommitId {
    private final String commitHash;
    private final String teamId;

    public RawCommitId(String commitHash, String teamId) {
        this.commitHash = commitHash;
        this.teamId = teamId;
    }

    public CommitId toCommitId(TeamDao teamDao) {
        return new CommitId(commitHash, teamDao.findById(teamId).get());
    }
}
