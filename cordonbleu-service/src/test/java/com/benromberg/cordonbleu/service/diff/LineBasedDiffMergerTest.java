package com.benromberg.cordonbleu.service.diff;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import com.benromberg.cordonbleu.util.ClasspathUtil;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.benromberg.cordonbleu.service.diff.DiffFragment;
import com.benromberg.cordonbleu.service.diff.DiffStatus;
import com.benromberg.cordonbleu.service.diff.LineBasedDiffMerger;

public class LineBasedDiffMergerTest {
    private static final String EMPTY_MULTI_LINES_TEXT = "\n\n";
    private static final String MANY_LINES_TEXT = "many\nlines\ntext";
    private static final String MULTI_LINE_TEXT_2 = "text";
    private static final String MULTI_LINE_TEXT_1 = "multi line";
    private static final String MULTI_LINE_TEXT = MULTI_LINE_TEXT_1 + "\n" + MULTI_LINE_TEXT_2;
    private static final String EMPTY_TEXT = "";
    private static final String PREFIXED_TEXT = "other but some is still the ";
    private static final String INSERT_END = "</i>";
    private static final String INSERT_START = "<i>";
    private static final String DELETE_END = "</d>";
    private static final String DELETE_START = "<d>";

    private static final String OTHER_TEXT = "other";
    private static final String SAME_TEXT = "same";
    private LineBasedDiffMerger merger;

    @Before
    public void setUp() {
        merger = new LineBasedDiffMerger();
    }

    @Test
    public void emptyText_ReturnsTheSame() throws Exception {
        List<String> mergedText = merge(EMPTY_TEXT, EMPTY_TEXT);
        assertThat(mergedText).containsExactly(EMPTY_TEXT);
    }

    @Test
    public void emptyInsertedText_ReturnsInsertion() throws Exception {
        List<String> mergedText = merge(null, EMPTY_TEXT);
        assertThat(mergedText).containsExactly(insert(EMPTY_TEXT));
    }

    @Test
    public void emptyDeletedText_ReturnsDeletion() throws Exception {
        List<String> mergedText = merge(EMPTY_TEXT, null);
        assertThat(mergedText).containsExactly(delete(EMPTY_TEXT));
    }

    @Test
    public void equalText_ReturnsTheSame() throws Exception {
        List<String> mergedText = merge(SAME_TEXT, SAME_TEXT);
        assertThat(mergedText).containsExactly(SAME_TEXT);
    }

    @Test
    public void insertText_ReturnsJustInsertion() throws Exception {
        List<String> mergedText = merge(null, SAME_TEXT);
        assertThat(mergedText).containsExactly(insert(SAME_TEXT));
    }

    @Test
    public void deleteText_ReturnsJustDeletion() throws Exception {
        List<String> mergedText = merge(SAME_TEXT, null);
        assertThat(mergedText).containsExactly(delete(SAME_TEXT));
    }

    @Test
    public void totallyDifferentText_ReturnsDeletionAndInsertionOnSeparateLines() throws Exception {
        List<String> mergedText = merge(SAME_TEXT, OTHER_TEXT);
        assertThat(mergedText).hasSize(2);
        assertThat(mergedText.get(0)).isEqualTo(delete(SAME_TEXT));
        assertThat(mergedText.get(1)).isEqualTo(insert(OTHER_TEXT));
    }

    @Test
    public void prefixedBeforeText_ReturnsDeletionAndInsertionOnSeparateLines() throws Exception {
        List<String> mergedText = merge(PREFIXED_TEXT + SAME_TEXT, SAME_TEXT);
        assertThat(mergedText).containsExactly(delete(PREFIXED_TEXT + SAME_TEXT), insert(SAME_TEXT));
    }

    @Test
    public void sameMultiLine_ReturnsSameLines() throws Exception {
        List<String> mergedText = merge(MULTI_LINE_TEXT, MULTI_LINE_TEXT);
        assertThat(mergedText).containsExactly(MULTI_LINE_TEXT_1, MULTI_LINE_TEXT_2);
    }

    @Test
    public void manyMultiLines_ReturnsSameLines() throws Exception {
        List<String> mergedText = merge(MANY_LINES_TEXT, MANY_LINES_TEXT);
        assertThat(mergedText).hasSize(3);
    }

    @Test
    public void emptyMultiLines_ReturnsSameLines() throws Exception {
        List<String> mergedText = merge(EMPTY_MULTI_LINES_TEXT, EMPTY_MULTI_LINES_TEXT);
        assertThat(mergedText).hasSize(3);
    }

    @Test
    public void prefixedBeforeMultiLine_ReturnsDeletionAndThenEqualLines() throws Exception {
        List<String> mergedText = merge(PREFIXED_TEXT + MULTI_LINE_TEXT, MULTI_LINE_TEXT);
        assertThat(mergedText).containsExactly(delete(PREFIXED_TEXT + MULTI_LINE_TEXT_1), insert(MULTI_LINE_TEXT_1),
                MULTI_LINE_TEXT_2);
    }

    @Test
    public void prefixedLineBeforeMultiLine_ReturnsDeletionAndThenEqualLines() throws Exception {
        List<String> mergedText = merge(PREFIXED_TEXT + "\n" + MULTI_LINE_TEXT, MULTI_LINE_TEXT);
        assertThat(mergedText).containsExactly(delete(PREFIXED_TEXT), MULTI_LINE_TEXT_1, MULTI_LINE_TEXT_2);
    }

    @Test
    public void prefixedNewlineBeforeMultiLine_ReturnsDeletionAndThenEqualLines() throws Exception {
        List<String> mergedText = merge("\n" + MULTI_LINE_TEXT, MULTI_LINE_TEXT);
        assertThat(mergedText).containsExactly(delete(""), MULTI_LINE_TEXT_1, MULTI_LINE_TEXT_2);
    }

    @Test
    public void prefixedMultiLineBeforeMultiLine_ReturnsDeletionAndThenEqualLines() throws Exception {
        List<String> mergedText = merge(MULTI_LINE_TEXT + SAME_TEXT + MULTI_LINE_TEXT, MULTI_LINE_TEXT + OTHER_TEXT);
        assertThat(mergedText).containsExactly(MULTI_LINE_TEXT_1,
                delete(MULTI_LINE_TEXT_2 + SAME_TEXT + MULTI_LINE_TEXT_1), delete(MULTI_LINE_TEXT_2),
                insert(MULTI_LINE_TEXT_2 + OTHER_TEXT));
    }

    @Test
    public void deletedTextWithPrefixedNewline_DoesntIncludeInsertion() throws Exception {
        List<String> mergedText = merge(OTHER_TEXT + "\n" + SAME_TEXT + "\n\n", OTHER_TEXT + "\n\n");
        assertThat(mergedText).containsExactly(OTHER_TEXT, delete(SAME_TEXT), "", "");
    }

    @Test
    public void realWorldCodeChange() throws Exception {
        // test case see
        // https://bitbucket.org/jobheroes/backend/diff/rest/src/test/java/com/jobheroes/rest/integration/GatewayIntegrationTest.java?diff2=dcee62942e41&at=master
        String beforeSource = ClasspathUtil.readFileFromClasspath("diff-before.java");
        String afterSource = ClasspathUtil.readFileFromClasspath("diff-after.java");
        String expectedResult = ClasspathUtil.readFileFromClasspath("diff-expected.html");
        String mergedText = merge(beforeSource, afterSource).stream().collect(joining("\n"));
        assertEquals(expectedResult, mergedText);
    }

    private String delete(String text) {
        return DELETE_START + text + DELETE_END;
    }

    private String insert(String text) {
        return INSERT_START + text + INSERT_END;
    }

    private List<String> merge(String before, String after) {
        return merger.merge(Optional.ofNullable(before), Optional.ofNullable(after)).stream()
                .map(codeLine -> wrap(codeLine)).collect(toList());
    }

    public static String wrap(DiffFragment codeLine) {
        if (codeLine.getStatus() == DiffStatus.AFTER) {
            return INSERT_START + codeLine.getText() + INSERT_END;
        }
        if (codeLine.getStatus() == DiffStatus.BEFORE) {
            return DELETE_START + codeLine.getText() + DELETE_END;
        }
        return codeLine.getText();
    }
}
