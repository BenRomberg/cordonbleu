package com.benromberg.cordonbleu.service.diff;

import static com.benromberg.cordonbleu.service.diff.DiffStatus.AFTER;
import static com.benromberg.cordonbleu.service.diff.DiffStatus.BEFORE;
import static com.benromberg.cordonbleu.service.diff.DiffStatus.KEEP;
import static com.benromberg.cordonbleu.util.OptionalHelper.toOptional;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheFile;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheText;
import com.benromberg.cordonbleu.data.model.CommitLineNumber;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.benromberg.cordonbleu.service.coderepository.CommitFile;
import com.benromberg.cordonbleu.service.coderepository.CommitFileContent;
import com.benromberg.cordonbleu.service.coderepository.CommitFileState;
import com.benromberg.cordonbleu.service.commit.HighlightedComment;
import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;
import com.benromberg.cordonbleu.service.diff.DiffStatus;
import com.benromberg.cordonbleu.service.diff.DiffViewCodeLine;
import com.benromberg.cordonbleu.service.diff.DiffViewService;
import com.benromberg.cordonbleu.service.diff.RelevantCodeLine;
import com.benromberg.cordonbleu.service.diff.RelevantDiffService;
import com.benromberg.cordonbleu.service.diff.SpacerCodeLine;

public class RelevantDiffServiceTest {
    private static final CommitFilePath COMMIT_FILE_PATH = new CommitFilePath(Optional.of("beforePath"),
            Optional.of("afterPath"));
    private static final CommitLineNumber COMMENT_LINE_NUMBER = new CommitLineNumber(Optional.of(2), Optional.of(2));
    private static final Comment COMMENT = new Comment(null, null, COMMIT_FILE_PATH, COMMENT_LINE_NUMBER);
    private static final Comment COMMENT_OTHER_FILE = new Comment(null, null, new CommitFilePath(
            Optional.of("otherPath"), Optional.of("otherPath")), COMMENT_LINE_NUMBER);
    private static final RelevantCodeLine COMMENT_LINE = new DiffViewCodeLine(COMMENT_LINE_NUMBER, DiffStatus.KEEP, "");
    private static final RelevantCodeLine NON_COMMENT_LINE = new DiffViewCodeLine(new CommitLineNumber(Optional.of(3),
            Optional.of(3)), DiffStatus.KEEP, "");
    private static final RelevantCodeLine KEEP_LINE = createLine(KEEP);
    private static final RelevantCodeLine AFTER_LINE = createLine(AFTER);
    private static final RelevantCodeLine BEFORE_LINE = createLine(BEFORE);
    private static final CommitFileContent EMPTY_CONTENT = CommitFileContent.ofSource("");

    private static RelevantCodeLine createLine(DiffStatus status) {
        return new DiffViewCodeLine(new CommitLineNumber(Optional.empty(), Optional.empty()), status, "");
    }

    private static SpacerCodeLine createSpacer(int beginIndex, int endIndex) {
        return new SpacerCodeLine(toOptional(beginIndex), toOptional(endIndex));
    }

    @Test
    public void onlyKeepLines_NothingRelevant() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(KEEP_LINE, KEEP_LINE, KEEP_LINE);
        assertThat(diffLines).hasSize(1);
        assertHasLines(diffLines, createSpacer(0, -1));
    }

    @Test
    public void oneContextLine_EverythingRelevant() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(KEEP_LINE, AFTER_LINE, KEEP_LINE);
        assertThat(diffLines).hasSize(3);
        assertHasLines(diffLines, KEEP_LINE, AFTER_LINE, KEEP_LINE);
    }

    @Test
    public void twoContextLines_EndsWithSpacer() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(KEEP_LINE, AFTER_LINE, KEEP_LINE, KEEP_LINE);
        assertHasLines(diffLines, KEEP_LINE, AFTER_LINE, KEEP_LINE, createSpacer(3, -1));
    }

    @Test
    public void twoContextLines_BeginsWithSpacer() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(KEEP_LINE, KEEP_LINE, KEEP_LINE, BEFORE_LINE);
        assertHasLines(diffLines, createSpacer(-1, 1), KEEP_LINE, BEFORE_LINE);
    }

    @Test
    public void threeContextLines_WithHole_HasSpacer() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(KEEP_LINE, AFTER_LINE, KEEP_LINE, KEEP_LINE,
                KEEP_LINE, BEFORE_LINE);
        assertHasLines(diffLines, KEEP_LINE, AFTER_LINE, KEEP_LINE, createSpacer(3, 3), KEEP_LINE, BEFORE_LINE);
    }

    @Test
    public void expand_WithOneLineToExpand() throws Exception {
        List<RelevantCodeLine> diffLines = callExpandSpacer(createSpacer(1, 1), KEEP_LINE, KEEP_LINE, KEEP_LINE);
        assertHasLines(diffLines, KEEP_LINE);
    }

    @Test
    public void expand_WithSpacerInBetween() throws Exception {
        List<RelevantCodeLine> diffLines = callExpandSpacer(createSpacer(0, 4), KEEP_LINE, KEEP_LINE, KEEP_LINE,
                KEEP_LINE, KEEP_LINE);
        assertHasLines(diffLines, KEEP_LINE, KEEP_LINE, createSpacer(2, 2), KEEP_LINE, KEEP_LINE);
    }

    @Test
    public void expand_OnTop() throws Exception {
        List<RelevantCodeLine> diffLines = callExpandSpacer(createSpacer(-1, 1), KEEP_LINE, KEEP_LINE);
        assertHasLines(diffLines, KEEP_LINE, KEEP_LINE);
    }

    @Test
    public void expand_OnTop_WithNewSpacer() throws Exception {
        List<RelevantCodeLine> diffLines = callExpandSpacer(createSpacer(-1, 2), KEEP_LINE, KEEP_LINE, KEEP_LINE);
        assertHasLines(diffLines, createSpacer(-1, 0), KEEP_LINE, KEEP_LINE);
    }

    @Test
    public void expand_OnBottom() throws Exception {
        List<RelevantCodeLine> diffLines = callExpandSpacer(createSpacer(0, -1), KEEP_LINE, KEEP_LINE);
        assertHasLines(diffLines, KEEP_LINE, KEEP_LINE);
    }

    @Test
    public void expand_OnBottom_WithNewSpacer() throws Exception {
        List<RelevantCodeLine> diffLines = callExpandSpacer(createSpacer(0, -1), KEEP_LINE, KEEP_LINE, KEEP_LINE);
        assertHasLines(diffLines, KEEP_LINE, KEEP_LINE, createSpacer(2, -1));
    }

    @Test
    public void commentedLine_IsRelevant() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(COMMENT, KEEP_LINE, COMMENT_LINE, KEEP_LINE);
        assertThat(diffLines).hasSize(3);
        assertHasLines(diffLines, KEEP_LINE, COMMENT_LINE, KEEP_LINE);
    }

    @Test
    public void commentedLine_InOtherFile_IsNotRelevant() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(COMMENT_OTHER_FILE, KEEP_LINE, COMMENT_LINE,
                KEEP_LINE);
        assertThat(diffLines).hasSize(1);
        assertHasLines(diffLines, createSpacer(0, -1));
    }

    @Test
    public void commentedLine_InOtherLines_IsNotRelevant() throws Exception {
        List<RelevantCodeLine> diffLines = callRelevantDiffService(COMMENT, KEEP_LINE, NON_COMMENT_LINE, KEEP_LINE);
        assertThat(diffLines).hasSize(1);
        assertHasLines(diffLines, createSpacer(0, -1));
    }

    private void assertHasLines(List<RelevantCodeLine> lines, RelevantCodeLine... expected) {
        assertThat(lines).hasSize(expected.length);
        IntStream.range(0, lines.size()).forEach(
                index -> {
                    assertThat(lines.get(index).isSpacer()).isEqualTo(expected[index].isSpacer());
                    if (!lines.get(index).isSpacer()) {
                        assertThat(lines.get(index).getLine()).isEqualTo(expected[index].getLine());
                    } else {
                        assertThat(lines.get(index).getSpacer().getBeginIndex()).isEqualTo(
                                expected[index].getSpacer().getBeginIndex());
                        assertThat(lines.get(index).getSpacer().getEndIndex()).isEqualTo(
                                expected[index].getSpacer().getEndIndex());
                    }
                });
    }

    private List<RelevantCodeLine> callRelevantDiffService(RelevantCodeLine... lines) {
        RelevantDiffService relevantDiffService = setupService(lines);
        return relevantDiffService.diffCodeLines(emptyList(),
                createHighlightedFile(CommitFile.changed("", "", EMPTY_CONTENT, EMPTY_CONTENT)));
    }

    private List<RelevantCodeLine> callRelevantDiffService(Comment comment, RelevantCodeLine... lines) {
        RelevantDiffService relevantDiffService = setupService(lines);
        return relevantDiffService.diffCodeLines(asList(new HighlightedComment(comment, new CommitHighlightCacheText(
                comment.getText(), asList()))), createHighlightedFile(CommitFile.changed(COMMIT_FILE_PATH
                .getBeforePath().get(), COMMIT_FILE_PATH.getAfterPath().get(), EMPTY_CONTENT, EMPTY_CONTENT)));
    }

    private List<RelevantCodeLine> callExpandSpacer(SpacerCodeLine spacer, RelevantCodeLine... lines) {
        RelevantDiffService relevantDiffService = setupService(lines);
        return relevantDiffService.expandSpacer(createHighlightedFile(CommitFile.added("", EMPTY_CONTENT)), spacer);
    }

    private RelevantDiffService setupService(RelevantCodeLine... lines) {
        List<DiffViewCodeLine> sourceLines = Stream.of(lines).map(line -> line.getLine()).collect(toList());
        RelevantDiffService relevantDiffService = new RelevantDiffService(new DummyDiffViewService(sourceLines), 1, 2);
        return relevantDiffService;
    }

    private HighlightedCommitFile createHighlightedFile(CommitFile commitFile) {
        return new HighlightedCommitFile(commitFile, new CommitHighlightCacheFile(
                stateToString(commitFile.getStateBefore()), stateToString(commitFile.getStateAfter())));
    }

    private List<String> stateToString(Optional<CommitFileState> state) {
        return asList(state.map(CommitFileState::getContent).orElse("").split("\n"));
    }

    private static class DummyDiffViewService extends DiffViewService {
        private final List<DiffViewCodeLine> lines;

        public DummyDiffViewService(List<DiffViewCodeLine> lines) {
            this.lines = lines;
        }

        @Override
        public List<DiffViewCodeLine> diffCodeLines(HighlightedCommitFile highlightedFile) {
            return lines;
        }
    }
}
