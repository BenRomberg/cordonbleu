package com.benromberg.cordonbleu.service.highlight;

import static com.benromberg.cordonbleu.util.ClasspathUtil.readFileFromClasspath;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.util.ClasspathUtil;

import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.service.coderepository.CommitFileContent;
import com.benromberg.cordonbleu.service.coderepository.CommitFileState;
import com.benromberg.cordonbleu.service.highlight.SyntaxHighlighter;

public class SyntaxHighlighterTest {
    private static final String BINARY_CHECKSUM = "75dae7103cb68ad5bfbf30b1a25565c7";
    public static final SyntaxHighlighter TEST_INSTANCE = createHighlighter(Integer.MAX_VALUE);
    private static final String CHANGELOG_MD_PATH = "CHANGELOG.md";
    private static final String SMALL_JAVA_UNKNOWN_PATH = "highlighted-expected-unknown-file-extension.html";
    private static final String DIFF_AFTER_EXPECTED_PATH = "highlighted-expected-after.html";
    private static final String DIFF_AFTER_PATH = "diff-after.java";
    private static final String SMALL_JAVA_EXPECTED_PATH = "highlighted-expected.html";
    private static final String BIG_JAVA_PATH = "big-class.java";
    private static final String SMALL_JAVA_PATH = "diff-before.java";

    private SyntaxHighlighter highlighter = TEST_INSTANCE;

    @Rule
    public DaoRule databaseRule = new DaoRule();

    @Test
    public void highlightingSmallFile_ReturnsSameAmountOfLines() throws Exception {
        String source = readFileFromClasspath(SMALL_JAVA_PATH);
        List<String> highlightedLines = highlight(source);
        assertSameNumberOfLines(source, highlightedLines);
    }

    @Test
    public void highlightingSmallFile_WithNewlinesAtTheBeginning_ReturnsSameAmountOfLines() throws Exception {
        String source = "\n\n" + readFileFromClasspath(SMALL_JAVA_PATH);
        List<String> highlightedLines = highlight(source);
        assertSameNumberOfLines(source, highlightedLines);
    }

    @Test
    public void highlightingSmallFile_WithNewlineAtTheEnd_ReturnsSameAmountOfLines() throws Exception {
        String source = readFileFromClasspath(SMALL_JAVA_PATH) + "\n";
        List<String> highlightedLines = highlight(source);
        assertSameNumberOfLines(source, highlightedLines);
    }

    @Test
    public void highlightingSmallFile_WithNewlinesAtTheEnd_ReturnsSameAmountOfLines() throws Exception {
        String source = readFileFromClasspath(SMALL_JAVA_PATH) + "\n\n";
        List<String> highlightedLines = highlight(source);
        assertSameNumberOfLines(source, highlightedLines);
    }

    @Test
    public void highlightingBigFile_ReturnsSameAmountOfLines() throws Exception {
        highlighter = createHighlighter(1);
        String source = readFileFromClasspath(BIG_JAVA_PATH);
        List<String> highlightedLines = highlight(source);
        assertSameNumberOfLines(source, highlightedLines);
    }

    @Test
    public void highlightingBigFile_IsNotHighlighted() throws Exception {
        highlighter = createHighlighter(1);
        String source = readFileFromClasspath(BIG_JAVA_PATH);
        String highlighted = StringEscapeUtils.unescapeHtml4(highlightToString(source));
        assertEquals(source, highlighted);
    }

    @Test
    public void realWorldCodeChange() throws Exception {
        String source = readFileFromClasspath(SMALL_JAVA_PATH);
        String expectedHighlightedLines = readFileFromClasspath(SMALL_JAVA_EXPECTED_PATH);
        String highlightedLines = highlightToString(source);
        assertEquals(expectedHighlightedLines, highlightedLines);
    }

    @Test
    public void realWorldCodeChangeAfter() throws Exception {
        String source = readFileFromClasspath(DIFF_AFTER_PATH);
        String expectedHighlightedLines = readFileFromClasspath(DIFF_AFTER_EXPECTED_PATH);
        String highlightedLines = highlightToString(source);
        assertEquals(expectedHighlightedLines, highlightedLines);
    }

    @Test
    public void unknownFileExtension_HighlightsNothing() throws Exception {
        String source = readFileFromClasspath(SMALL_JAVA_PATH);
        String expectedHighlightedLines = readFileFromClasspath(SMALL_JAVA_UNKNOWN_PATH);
        String highlightedLines = highlight(source, "").stream().collect(joining("\n"));
        assertEquals(expectedHighlightedLines, highlightedLines);
    }

    // test case see https://github.com/rackt/react-modal/commit/1a0a069a7f91aa660433bdfa410636d4e3d81fcf
    @Test
    public void lessNumberOfLines_OnMarkdownLexer() throws Exception {
        String source = ClasspathUtil.readFileFromClasspath(CHANGELOG_MD_PATH) + "\n";
        List<String> highlightedLines = highlight(source, CHANGELOG_MD_PATH);
        assertSameNumberOfLines(source, highlightedLines);
    }

    @Test
    public void binaryContent_HasContentAsSingleLine() throws Exception {
        List<String> lines = highlighter
                .highlight(new CommitFileState("", CommitFileContent.ofBinary(BINARY_CHECKSUM)));
        assertThat(lines).containsExactly(BINARY_CHECKSUM);
    }

    private static SyntaxHighlighter createHighlighter(int timeoutIntMs) {
        return new SyntaxHighlighter(() -> timeoutIntMs, Executors.newSingleThreadExecutor());
    }

    private void assertSameNumberOfLines(String source, List<String> highlightedLines) {
        assertThat(highlightedLines).hasSize(source.split("\n", -1).length);
    }

    private String highlightToString(String source) {
        return highlight(source).stream().collect(joining("\n"));
    }

    private List<String> highlight(String source) {
        return highlight(source, SMALL_JAVA_PATH);
    }

    private List<String> highlight(String source, String path) {
        List<String> highlightedLines = highlighter.highlight(new CommitFileState(path, CommitFileContent
                .ofSource(source)));
        return highlightedLines;
    }
}
