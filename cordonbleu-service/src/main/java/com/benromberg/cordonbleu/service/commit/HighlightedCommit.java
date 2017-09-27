package com.benromberg.cordonbleu.service.commit;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitApproval;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitHighlightCache;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheFile;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.benromberg.cordonbleu.service.coderepository.CommitDetail;
import com.benromberg.cordonbleu.service.coderepository.CommitFile;

public class HighlightedCommit {
    private final Commit commit;
    private final List<HighlightedCommitFile> files;
    private final List<HighlightedComment> comments;
    private final HighlightedCommitMessage message;

    public HighlightedCommit(CommitDetail commitDetail, CommitHighlightCache highlightCache) {
        this.commit = commitDetail.getCommit();
        this.files = createHighlightedFiles(commitDetail.getFiles(), highlightCache.getFiles());
        this.comments = createHighlightedComments(commitDetail.getCommit().getComments(), highlightCache.getComments());
        this.message = new HighlightedCommitMessage(commitDetail.getCommit().getMessage(), highlightCache.getMessage());
    }

    private List<HighlightedComment> createHighlightedComments(List<Comment> comments,
            Map<String, CommitHighlightCacheText> highlightedComments) {
        return comments.stream()
                .map(comment -> new HighlightedComment(comment, highlightedComments.get(comment.getId())))
                .collect(toList());
    }

    private List<HighlightedCommitFile> createHighlightedFiles(List<CommitFile> files,
            List<CommitHighlightCacheFile> highlightCacheFiles) {
        List<HighlightedCommitFile> highlightedFiles = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            highlightedFiles.add(new HighlightedCommitFile(files.get(i), highlightCacheFiles.get(i)));
        }
        return highlightedFiles;
    }

    public List<HighlightedCommitFile> getFiles() {
        return files;
    }

    public CommitAuthor getAuthor() {
        return commit.getAuthor();
    }

    public LocalDateTime getCreated() {
        return commit.getCreated();
    }

    public HighlightedCommitMessage getMessage() {
        return message;
    }

    public List<CommitRepository> getRepositories() {
        return commit.getRepositories();
    }

    public Optional<CommitApproval> getApproval() {
        return commit.getApproval();
    }
    
    public boolean getProposeToCollectiveReview() {
    	return commit.getProposeToCollectiveReview();
    }

    public List<HighlightedComment> getComments() {
        return comments;
    }

    public CommitId getId() {
        return commit.getId();
    }
}
