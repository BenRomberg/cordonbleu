package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.service.email.EmailTemplate;

import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BatchAssignmentEmailTemplate extends EmailTemplate {
    private final CommitAuthor commitAuthor;
    private final List<CommitEmailItem> commitEmailItems;
    private final User assignedBy;

    public BatchAssignmentEmailTemplate(CommitBatchAssignment batch, Function<CommitId, String> commitPathResolver, User assignedBy) {
        super("batch-assignment.stg");
        this.commitAuthor = batch.getCommitAuthor();
        this.commitEmailItems = batch.getCommits()
                .stream()
                .map(commit -> new CommitEmailItem(commit, commitPathResolver.apply(commit.getId())))
                .collect(Collectors.toList());
        this.assignedBy = assignedBy;
    }

    @Override
    protected ST getSubjectTemplate() {
        return injectCommonAttributes(super.getSubjectTemplate());
    }

    @Override
    protected ST getPlainBodyTemplate() {
        return injectCommonAttributes(super.getPlainBodyTemplate());
    }

    @Override
    protected ST getHtmlBodyTemplate() {
        return injectCommonAttributes(super.getHtmlBodyTemplate());
    }

    private ST injectCommonAttributes(ST template) {
        return template.add("commitAuthor", commitAuthor)
                .add("commitEmailItems", commitEmailItems)
                .add("assignedBy", assignedBy);
    }

    private class CommitEmailItem {
        private final Commit commit;

        private final String commitPath;

        public CommitEmailItem(Commit commit, String commitPath) {
            this.commit = commit;
            this.commitPath = commitPath;
        }

        public Commit getCommit() {
            return commit;
        }

        public String getCommitPath() {
            return commitPath;
        }
    }
}
