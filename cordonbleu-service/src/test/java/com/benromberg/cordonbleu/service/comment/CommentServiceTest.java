package com.benromberg.cordonbleu.service.comment;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.CommitDao;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.model.CommentFixture;
import com.benromberg.cordonbleu.data.model.CommitFixture;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.service.comment.CommentService;
import com.benromberg.cordonbleu.service.commit.CommitHighlightServiceMock;

public class CommentServiceTest implements CommitFixture, CommentFixture {
    @Rule
    public DaoRule databaseRule = new DaoRule().withCommit().withCommentUser();

    private final CommitDao commitDao = databaseRule.createCommitDao();
    private final CommitHighlightServiceMock commitHighlightService = new CommitHighlightServiceMock();
    private final CommentService service = new CommentService(commitDao, new CommentEmailServiceMock(),
            commitHighlightService);

    @Test
    public void addComment_AddsCommentToCommit() throws Exception {
        service.addComment(COMMIT, COMMENT);
        assertThat(commitDao.findById(COMMIT_ID).get().getComments()).hasSize(1);
    }

    @Test
    public void addComment_UpdatesHighlightCache() throws Exception {
        service.addComment(COMMIT, COMMENT);
        assertThat(commitHighlightService.isUpdateCommentCalled()).isTrue();
    }

    @Test
    public void editComment_UpdatesHighlightCache() throws Exception {
        service.updateComment(COMMIT, COMMENT.getId(), "edited text");
        assertThat(commitHighlightService.isUpdateCommentCalled()).isTrue();
    }
}
