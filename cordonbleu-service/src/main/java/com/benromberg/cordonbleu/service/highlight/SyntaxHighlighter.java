package com.benromberg.cordonbleu.service.highlight;

import static com.benromberg.cordonbleu.util.ClasspathUtil.readFileFromClasspath;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benromberg.cordonbleu.service.coderepository.CommitFileState;

@Singleton
public class SyntaxHighlighter {
    private static final String PYGMENTS_CODE_PATH = "highlight.py";
    private static final String PYGMENTS_CODE = readFileFromClasspath(PYGMENTS_CODE_PATH);
    private static final String VAR_RESULT = "result";
    private static final String VAR_SOURCE = "source";
    private static final String VAR_PATH = "path";
    private static final Logger LOGGER = LoggerFactory.getLogger(SyntaxHighlighter.class);

    private final HighlightingTimeout timeout;
    private final ExecutorService executorService;

    @Inject
    public SyntaxHighlighter(HighlightingTimeout timeout, ExecutorService executorService) {
        this.timeout = timeout;
        this.executorService = executorService;
    }

    public List<String> highlight(CommitFileState fileState) {
        if (fileState.isBinary()) {
            return asList(fileState.getContent());
        }
        return highlightWithTimeout(fileState.getPath(), fileState.getContent());
    }

    private List<String> highlightWithTimeout(String path, String source) {
        Future<List<String>> future = executorService.submit(() -> highlightWithPygments(path, source));
        try {
            return future.get(timeout.getHighlightingTimeoutInMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            LOGGER.info("Interrupted highlighting of {}.", path);
            return convertToLines(StringEscapeUtils.escapeHtml4(source));
        }
    }

    private List<String> highlightWithPygments(String path, String source) {
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            String result = executePygmentsHighlighter(path, source, interpreter);
            List<String> resultAsLines = convertToLines(result);
            return adjustLineBreaks(source, resultAsLines);
        }
    }

    public void warmup() {
        highlightWithPygments(PYGMENTS_CODE_PATH, PYGMENTS_CODE);
    }

    private List<String> adjustLineBreaks(String source, List<String> resultAsLines) {
        int expectedNumLines = StringUtils.countMatches(source, "\n") + 1;
        int linesToAdd = expectedNumLines - resultAsLines.size();
        for (int i = 0; i < linesToAdd; i++) {
            resultAsLines.add("\n");
        }
        if (expectedNumLines < resultAsLines.size()) {
            return resultAsLines.subList(0, expectedNumLines);
        }
        return resultAsLines;
    }

    private List<String> convertToLines(String result) {
        return Stream.of(result.split("\n", -1)).collect(toList());
    }

    private String executePygmentsHighlighter(String path, String source, PythonInterpreter interpreter) {
        interpreter.set(VAR_SOURCE, source);
        interpreter.set(VAR_PATH, path);
        interpreter.exec(PYGMENTS_CODE);
        String result = interpreter.get(VAR_RESULT, String.class);
        return result;
    }

}
