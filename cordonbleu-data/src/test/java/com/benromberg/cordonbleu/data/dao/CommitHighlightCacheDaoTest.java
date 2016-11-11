package com.benromberg.cordonbleu.data.dao;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.CommitFixture;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.dao.CommitHighlightCacheDao;
import com.benromberg.cordonbleu.data.model.CommitHighlightCache;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;

public class CommitHighlightCacheDaoTest implements CommitFixture, CommentFixture {
    private static final String COMMENT_ID = "comment-id";
    private static final String HIGHLIGHTED_TEXT = "highlighted text";

    @Rule
    public DaoRule databaseRule = new DaoRule().withTeam();

    private final CommitHighlightCacheDao highlightDao = databaseRule.createCommitHighlightCacheDao();

    @Test
    public void updateComment_WithoutCachedCommit_DoesNothing() throws Exception {
        highlightDao.updateComment(COMMIT_ID, COMMENT_ID, createHighlightCacheText());
        assertThat(highlightDao.findById(COMMIT_ID)).isEmpty();
    }

    @Test
    public void updateComment_WithCachedCommit_UpdatesHighlightedComment() throws Exception {
        highlightDao.insert(new CommitHighlightCache(COMMIT_ID, 1, asList(), createHighlightCacheText(), emptyMap()));
        highlightDao.updateComment(COMMIT_ID, COMMENT_ID, createHighlightCacheText());
        assertThat(highlightDao.findById(COMMIT_ID).get().getComments().get(COMMENT_ID).getText()).isEqualTo(
                HIGHLIGHTED_TEXT);
    }

    private CommitHighlightCacheText createHighlightCacheText() {
        return new CommitHighlightCacheText(HIGHLIGHTED_TEXT, asList());
    }
}
