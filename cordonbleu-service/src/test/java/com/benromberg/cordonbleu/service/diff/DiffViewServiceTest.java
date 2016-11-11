package com.benromberg.cordonbleu.service.diff;

import static com.benromberg.cordonbleu.util.OptionalHelper.toOptional;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.data.model.CommitHighlightCacheFile;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.benromberg.cordonbleu.service.coderepository.CommitFile;
import com.benromberg.cordonbleu.service.coderepository.CommitFileContent;
import com.benromberg.cordonbleu.service.coderepository.CommitFileState;
import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;
import com.benromberg.cordonbleu.service.diff.DiffFragment;
import com.benromberg.cordonbleu.service.diff.DiffStatus;
import com.benromberg.cordonbleu.service.diff.DiffViewCodeLine;
import com.benromberg.cordonbleu.service.diff.DiffViewService;

public class DiffViewServiceTest {
    private static final String PATH_AFTER = "after";
    private static final String PATH_BEFORE = "before";
    private static final String PATH = "filename.ext";
    private static final String FOLDER = "folder/";
    private static final String BEFORE_PATH = "diff-before.java";
    private static final String EMPTY = "";
    private static final String AFTER_2 = "but new";
    private static final String BEFORE_2 = "strange";
    private static final String AFTER_1 = "different";
    private static final String BEFORE_1 = "completely";
    private DiffViewService generator;

    @Before
    public void setUp() {
        generator = new DiffViewService();
    }

    @Test
    public void emptyBeforeAndAfter_GeneratesSingleEmptyLine() throws Exception {
        List<DiffViewCodeLine> lines = generate(EMPTY, EMPTY);
        assertThat(lines).hasSize(1);
        assertCodeLine(lines.get(0), DiffStatus.KEEP, 1, 1, EMPTY);
    }

    @Test
    public void newEmptyFile_GeneratesSingleEmptyLine() throws Exception {
        List<DiffViewCodeLine> lines = generator.diffCodeLines(createHighlightedFile(CommitFile.added(PATH_AFTER,
                CommitFileContent.ofSource(EMPTY))));
        assertThat(lines).hasSize(1);
        assertCodeLine(lines.get(0), DiffStatus.AFTER, -1, 1, EMPTY);
    }

    @Test
    public void nonExistentAfter_GeneratesSingleBeforeLine() throws Exception {
        List<DiffViewCodeLine> lines = generator.diffCodeLines(createHighlightedFile(CommitFile.removed(BEFORE_PATH,
                CommitFileContent.ofSource(BEFORE_1))));
        assertThat(lines).hasSize(1);
        assertCodeLine(lines.get(0), DiffStatus.BEFORE, 1, -1, BEFORE_1);
    }

    @Test
    public void completelyDifferentBeforeAndAfter_GeneratesTwoLines() throws Exception {
        List<DiffViewCodeLine> lines = generate(BEFORE_1, AFTER_1);
        assertThat(lines).hasSize(2);
        assertCodeLine(lines.get(0), DiffStatus.BEFORE, 1, -1, BEFORE_1);
        assertCodeLine(lines.get(1), DiffStatus.AFTER, -1, 1, AFTER_1);
    }

    @Test
    public void completelyDifferentMultiline_GeneratesTwoLinePairs() throws Exception {
        List<DiffViewCodeLine> lines = generate(BEFORE_1 + "\n" + BEFORE_2, AFTER_1 + "\n" + AFTER_2);
        assertThat(lines).hasSize(4);
        assertCodeLine(lines.get(0), DiffStatus.BEFORE, 1, -1, BEFORE_1);
        assertCodeLine(lines.get(1), DiffStatus.BEFORE, 2, -1, BEFORE_2);
        assertCodeLine(lines.get(2), DiffStatus.AFTER, -1, 1, AFTER_1);
        assertCodeLine(lines.get(3), DiffStatus.AFTER, -1, 2, AFTER_2);
    }

    @Test
    public void diffPath_ForSameName_DoesNothing() throws Exception {
        List<DiffFragment> fragments = generator.diffPaths(new CommitFilePath(Optional.of(PATH), Optional.of(PATH)));
        assertThat(fragments).hasSize(1);
        assertFragment(fragments.get(0), DiffStatus.KEEP, PATH);
    }

    @Test
    public void diffPath_ForNewFile_GivesAfterFragment() throws Exception {
        List<DiffFragment> fragments = generator.diffPaths(new CommitFilePath(Optional.empty(), Optional.of(PATH)));
        assertThat(fragments).hasSize(1);
        assertFragment(fragments.get(0), DiffStatus.AFTER, PATH);
    }

    @Test
    public void diffPath_ForDeletedFile_GivesBeforeFragment() throws Exception {
        List<DiffFragment> fragments = generator.diffPaths(new CommitFilePath(Optional.of(PATH), Optional.empty()));
        assertThat(fragments).hasSize(1);
        assertFragment(fragments.get(0), DiffStatus.BEFORE, PATH);
    }

    @Test
    public void diffPath_ForDifferentPath_SplitsIntoParts() throws Exception {
        List<DiffFragment> fragments = generator.diffPaths(new CommitFilePath(Optional.of(FOLDER + PATH_BEFORE),
                Optional.of(FOLDER + PATH_AFTER)));
        assertThat(fragments).hasSize(3);
        assertFragment(fragments.get(0), DiffStatus.KEEP, FOLDER);
        assertFragment(fragments.get(1), DiffStatus.BEFORE, PATH_BEFORE);
        assertFragment(fragments.get(2), DiffStatus.AFTER, PATH_AFTER);
    }

    private void assertFragment(DiffFragment fragment, DiffStatus status, String text) {
        assertThat(fragment.getStatus()).isEqualTo(status);
        assertThat(fragment.getText()).isEqualTo(text);
    }

    private HighlightedCommitFile createHighlightedFile(CommitFile commitFile) {
        return new HighlightedCommitFile(commitFile, new CommitHighlightCacheFile(
                stateToString(commitFile.getStateBefore()), stateToString(commitFile.getStateAfter())));
    }

    private List<String> stateToString(Optional<CommitFileState> optionalState) {
        return optionalState.map(state -> asList(state.getContent().split("\n"))).orElse(asList());
    }

    private List<DiffViewCodeLine> generate(String beforeSource, String afterSource) {
        return generator.diffCodeLines(createHighlightedFile(CommitFile.changed(PATH, PATH,
                CommitFileContent.ofSource(beforeSource), CommitFileContent.ofSource(afterSource))));
    }

    private void assertCodeLine(DiffViewCodeLine line, DiffStatus status, int beforeLineNumber, int afterLineNumber,
            String highlightedCode) {
        assertThat(line.getHighlightedCode()).isEqualTo(highlightedCode);
        assertThat(line.getCommitLineNumber().getBeforeLineNumber()).isEqualTo(toOptional(beforeLineNumber));
        assertThat(line.getCommitLineNumber().getAfterLineNumber()).isEqualTo(toOptional(afterLineNumber));
        assertThat(line.getStatus()).isEqualTo(status);
    }
}
