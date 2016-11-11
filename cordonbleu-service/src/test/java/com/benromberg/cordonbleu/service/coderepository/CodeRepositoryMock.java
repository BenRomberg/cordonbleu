package com.benromberg.cordonbleu.service.coderepository;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;

import java.util.Collection;
import java.util.List;

import com.benromberg.cordonbleu.service.coderepository.CodeRepository;
import com.benromberg.cordonbleu.service.coderepository.CommitDetail;
import com.benromberg.cordonbleu.service.coderepository.CommitFile;
import com.benromberg.cordonbleu.service.coderepository.CommitFileContent;
import com.benromberg.cordonbleu.service.coderepository.CommitWithRepository;
import com.benromberg.cordonbleu.service.coderepository.PullResult;

public class CodeRepositoryMock implements CodeRepository {
    public static final CommitAuthor COMMIT_AUTHOR = new CommitAuthor("commit author", "commit@author.email");
    public static final String COMMIT_ID = "commit id";
    public static final String COMMIT_PATH_AFTER = "path after";
    public static final String COMMIT_PATH_BEFORE = "path before";
    public static final CommitFileContent COMMIT_CONTENT_AFTER = CommitFileContent.ofSource("content after");
    public static final CommitFileContent COMMIT_CONTENT_BEFORE = CommitFileContent.ofSource("content before");

    private boolean removeCalled;
    private List<CommitId> removedCommitIds = emptyList();
    private List<CommitWithRepository> newCommits;

    public CodeRepositoryMock(CodeRepositoryMetadata repository) {
        newCommits = asList(createCommit(repository));
    }

    public void setRemovedCommitIds(List<CommitId> removedCommitIds) {
        this.removedCommitIds = removedCommitIds;
    }

    public void setNewCommits(List<CommitWithRepository> newCommits) {
        this.newCommits = newCommits;
    }

    @Override
    public PullResult pull(Collection<Commit> existingCommits) {
        return new PullResult(newCommits, removedCommitIds);
    }

    public static CommitWithRepository createCommit(CodeRepositoryMetadata repository) {
        CommitRepository commitRepository = new CommitRepository(repository, asList("commit branch"));
        return new CommitWithRepository(new CommitFixture.CommitBuilder().repositories(commitRepository).build(),
                commitRepository);
    }

    @Override
    public CommitDetail getCommitDetail(Commit commit) {
        return new CommitDetail(commit, singletonList(CommitFile.changed(COMMIT_PATH_BEFORE, COMMIT_PATH_AFTER,
                COMMIT_CONTENT_BEFORE, COMMIT_CONTENT_AFTER)));
    }

    @Override
    public void remove() {
        removeCalled = true;
    }

    public boolean wasRemoveCalled() {
        return removeCalled;
    }
}