package com.benromberg.cordonbleu.service.commit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.CommitHighlightCacheDao;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitHighlightCache;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryServiceMock;
import com.benromberg.cordonbleu.service.highlight.SyntaxHighlighterTest;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.service.commit.CommitHighlightService;
import com.benromberg.cordonbleu.service.highlight.TextHighlightResult;
import com.benromberg.cordonbleu.service.highlight.TextHighlightServiceMock;

public class CommitHighlightServiceTest implements CommitFixture, CommentFixture {
    private static final String HIGHLIGHTED_TEXT = "highlighted text";

    @Rule
    public DaoRule databaseRule = new DaoRule().withTeam();

    private final CommitHighlightCacheDao highlightCacheDao = databaseRule.createCommitHighlightCacheDao();
    private final CommitHighlightService service = new CommitHighlightService(highlightCacheDao,
            new CodeRepositoryServiceMock("path before", "path after"), new TextHighlightServiceMock(
                    new TextHighlightResult(HIGHLIGHTED_TEXT, asList())), SyntaxHighlighterTest.TEST_INSTANCE);

    @Test
    public void uncachedCommit_IsCached() throws Exception {
        service.highlight(COMMIT);
        assertThat(highlightCacheDao.findById(COMMIT_ID)).isPresent();
    }

    @Test
    public void updateComment_WithCachedCommit_UpdatesHighlightedComment() throws Exception {
        Commit commit = COMMIT;
        service.highlight(commit);
        Comment comment = COMMENT;
        updateComment(commit, comment);
        CommitHighlightCache commitHighlight = highlightCacheDao.findById(COMMIT_ID).get();
        assertThat(commitHighlight.getComments().get(comment.getId()).getText()).isEqualTo(HIGHLIGHTED_TEXT);
    }

    @Test
    public void updateComment_WithNonCachedCommit_DoesNothing() throws Exception {
        updateComment(COMMIT, COMMENT);
        assertThat(highlightCacheDao.findById(COMMIT_ID)).isEmpty();
    }

    private void updateComment(Commit commit, Comment comment) {
        service.updateComment(commit.getId(), comment.getId(), comment.getText());
    }
}
