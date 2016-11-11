package com.benromberg.cordonbleu.service.commit;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitHighlightCache;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheFile;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;
import com.benromberg.cordonbleu.data.model.CommitId;

import java.util.List;
import java.util.Map;

import com.benromberg.cordonbleu.service.coderepository.CommitDetail;
import com.benromberg.cordonbleu.service.coderepository.CommitFile;

public class CommitHighlightServiceMock extends CommitHighlightService {
    private final List<CommitFile> files;
    private boolean updateCommentCalled;

    public CommitHighlightServiceMock(CommitFile... files) {
        super(null, null, null, null);
        this.files = asList(files);
    }

    @Override
    public HighlightedCommit highlight(Commit commit) {
        return new HighlightedCommit(new CommitDetail(commit, files), new CommitHighlightCache(commit.getId(), 1,
                createHighlightFiles(), new CommitHighlightCacheText("highlighted commit message", asList()),
                createCommentMap(commit.getComments())));
    }

    private List<CommitHighlightCacheFile> createHighlightFiles() {
        return files.stream()
                .map(file -> new CommitHighlightCacheFile(asList("content before"), asList("content after")))
                .collect(toList());
    }

    private Map<String, CommitHighlightCacheText> createCommentMap(List<Comment> comments) {
        return comments.stream().collect(
                toMap(Comment::getId, comment -> new CommitHighlightCacheText(comment.getText(), asList())));
    }

    @Override
    public void updateComment(CommitId commitId, String commentId, String text) {
        this.updateCommentCalled = true;
    }

    public boolean isUpdateCommentCalled() {
        return updateCommentCalled;
    }
}
