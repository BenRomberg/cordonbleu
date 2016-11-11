package com.benromberg.cordonbleu.service.commit;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.benromberg.cordonbleu.data.dao.CommitHighlightCacheDao;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitHighlightCache;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheFile;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;
import com.benromberg.cordonbleu.data.model.CommitId;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import com.benromberg.cordonbleu.service.coderepository.CommitDetail;
import com.benromberg.cordonbleu.service.coderepository.CommitFile;
import com.benromberg.cordonbleu.service.coderepository.CommitFileState;
import com.benromberg.cordonbleu.service.highlight.SyntaxHighlighter;
import com.benromberg.cordonbleu.service.highlight.TextHighlightResult;
import com.benromberg.cordonbleu.service.highlight.TextHighlightService;

public class CommitHighlightService {
    private static final int VERSION = 1;

    private final CommitHighlightCacheDao highlightCacheDao;
    private final CodeRepositoryService repositoryService;
    private final TextHighlightService textHighlightService;
    private final SyntaxHighlighter syntaxHighlighter;

    @Inject
    public CommitHighlightService(CommitHighlightCacheDao highlightCacheDao, CodeRepositoryService repositoryService,
            TextHighlightService textHighlightService, SyntaxHighlighter syntaxHighlighter) {
        this.highlightCacheDao = highlightCacheDao;
        this.repositoryService = repositoryService;
        this.textHighlightService = textHighlightService;
        this.syntaxHighlighter = syntaxHighlighter;
    }

    public HighlightedCommit highlight(Commit commit) {
        CommitDetail commitDetail = repositoryService.getCommitDetail(commit);
        CommitHighlightCache cachedCommit = highlightCacheDao.findById(commit.getId()).orElseGet(
                () -> cacheCommitHighlight(commitDetail));
        return highlightedCommit(commitDetail, cachedCommit);
    }

    private HighlightedCommit highlightedCommit(CommitDetail commitDetail, CommitHighlightCache cachedCommit) {
        return new HighlightedCommit(commitDetail, cachedCommit);
    }

    private CommitHighlightCache cacheCommitHighlight(CommitDetail commitDetail) {
        CommitHighlightCacheText message = highlightResultToCacheText(textHighlightService.textToHtml(commitDetail
                .getCommit().getMessage()));
        Map<String, CommitHighlightCacheText> comments = commitDetail.getCommit().getComments().stream()
                .collect(toMap(Comment::getId, comment -> toHighlightedText(comment.getText())));
        List<CommitHighlightCacheFile> files = commitDetail.getFiles().stream().map(file -> toHighlightedFile(file))
                .collect(toList());
        CommitHighlightCache commitHighlightCache = new CommitHighlightCache(commitDetail.getCommit().getId(), VERSION,
                files, message, comments);
        highlightCacheDao.insert(commitHighlightCache);
        return commitHighlightCache;
    }

    private CommitHighlightCacheFile toHighlightedFile(CommitFile file) {
        List<String> highlightedBefore = highlightFile(file.getStateBefore());
        List<String> highlightedAfter = highlightFile(file.getStateAfter());
        return new CommitHighlightCacheFile(highlightedBefore, highlightedAfter);
    }

    private List<String> highlightFile(Optional<CommitFileState> fileState) {
        if (!fileState.isPresent()) {
            return Collections.emptyList();
        }
        return syntaxHighlighter.highlight(fileState.get());
    }

    private CommitHighlightCacheText toHighlightedText(String text) {
        return highlightResultToCacheText(textHighlightService.markdownToHtml(text));
    }

    private CommitHighlightCacheText highlightResultToCacheText(TextHighlightResult result) {
        return new CommitHighlightCacheText(result.getText(), result.getReferencedUsers());
    }

    public void updateComment(CommitId commitId, String commentId, String text) {
        highlightCacheDao.updateComment(commitId, commentId, toHighlightedText(text));
    }
}
