package com.benromberg.cordonbleu.service.diff;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import com.benromberg.cordonbleu.data.model.CommitFilePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import javax.inject.Inject;

import com.benromberg.cordonbleu.service.commit.HighlightedComment;
import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;

public class RelevantDiffService {
    private static final int DEFAULT_CONTEXT_LINES = 4;
    private static final int DEFAULT_EXPAND_LINES = 12;

    private final DiffViewService diffViewService;
    private final int contextLines;
    private final int expandLines;

    @Inject
    public RelevantDiffService(DiffViewService diffViewService) {
        this(diffViewService, DEFAULT_CONTEXT_LINES, DEFAULT_EXPAND_LINES);
    }

    public RelevantDiffService(DiffViewService diffViewService, int contextLines, int expandLines) {
        this.diffViewService = diffViewService;
        this.contextLines = contextLines;
        this.expandLines = expandLines;
    }

    public List<RelevantCodeLine> diffCodeLines(List<HighlightedComment> comments, HighlightedCommitFile commitFile) {
        List<DiffViewCodeLine> diffCodeLines = diffViewService.diffCodeLines(commitFile);
        Set<Integer> relevantLines = findRelevantLines(diffCodeLines, findRelevantComments(comments, commitFile));
        Map<Integer, SpacerCodeLine> spacerLines = findSpacerLines(diffCodeLines, relevantLines);
        return IntStream.range(0, diffCodeLines.size())
                .filter(index -> relevantLines.contains(index) || spacerLines.keySet().contains(index))
                .mapToObj(index -> {
                    if (relevantLines.contains(index)) {
                        return diffCodeLines.get(index);
                    }
                    return spacerLines.get(index);
                }).collect(toList());
    }

    private List<HighlightedComment> findRelevantComments(List<HighlightedComment> comments,
            HighlightedCommitFile commitFile) {
        return comments.stream().filter(comment -> comment.getCommitFilePath().equals(commitFile.getPath()))
                .collect(toList());
    }

    private Set<Integer> findRelevantLines(List<DiffViewCodeLine> diffCodeLines, List<HighlightedComment> comments) {
        return IntStream.range(0, diffCodeLines.size())
                .filter(index -> lineIsChangedOrCommented(diffCodeLines.get(index), comments))
                .flatMap(changedLine -> IntStream.rangeClosed(changedLine - contextLines, changedLine + contextLines))
                .boxed().collect(toSet());
    }

    private boolean lineIsChangedOrCommented(DiffViewCodeLine line, List<HighlightedComment> comments) {
        boolean lineHasComment = comments.stream().anyMatch(
                comment -> comment.getCommitLineNumber().equals(line.getCommitLineNumber()));
        return line.getStatus() != DiffStatus.KEEP || lineHasComment;
    }

    // TODO: refactor, no idea how to do it more elegantly yet
    private Map<Integer, SpacerCodeLine> findSpacerLines(List<DiffViewCodeLine> diffCodeLines,
            Set<Integer> relevantLines) {
        Map<Integer, SpacerCodeLine> spacerLines = new HashMap<>();
        int spacerBegin = 0;
        boolean insideSpacer = false;
        for (int i = 0; i < diffCodeLines.size(); i++) {
            if (relevantLines.contains(i) && insideSpacer) {
                spacerLines.put(spacerBegin, new SpacerCodeLine(spacerBeginToIndex(spacerBegin), Optional.of(i - 1)));
                insideSpacer = false;
            }
            if (!relevantLines.contains(i) && !insideSpacer) {
                spacerBegin = i;
                insideSpacer = true;
            }
        }
        if (insideSpacer) {
            spacerLines.put(spacerBegin, new SpacerCodeLine(Optional.of(spacerBegin), Optional.empty()));
        }
        return spacerLines;
    }

    private Optional<Integer> spacerBeginToIndex(int spacerBegin) {
        if (spacerBegin == 0) {
            return Optional.empty();
        }
        return Optional.of(spacerBegin);
    }

    public List<DiffFragment> diffPaths(CommitFilePath commitFilePath) {
        return diffViewService.diffPaths(commitFilePath);
    }

    public List<RelevantCodeLine> expandSpacer(HighlightedCommitFile commitFile, SpacerCodeLine spacer) {
        List<DiffViewCodeLine> diffCodeLines = diffViewService.diffCodeLines(commitFile);
        if (!spacer.getBeginIndex().isPresent()) {
            return expandSpacerOnTop(spacer, diffCodeLines);
        }
        if (!spacer.getEndIndex().isPresent()) {
            return expandSpacerOnBottom(spacer, diffCodeLines);
        }
        return expandSpacerOnBothSides(spacer, diffCodeLines);
    }

    private List<RelevantCodeLine> expandSpacerOnTop(SpacerCodeLine spacer, List<DiffViewCodeLine> diffCodeLines) {
        int endIndex = spacer.getEndIndex().get();
        int beginIndex = endIndex - expandLines + 1;
        if (beginIndex <= 0) {
            return diffCodeLines.subList(0, endIndex + 1).stream().collect(toList());
        }
        List<RelevantCodeLine> spacedList = new ArrayList<>();
        spacedList.add(new SpacerCodeLine(Optional.empty(), Optional.of(beginIndex - 1)));
        spacedList.addAll(diffCodeLines.subList(beginIndex, endIndex + 1));
        return spacedList;
    }

    private List<RelevantCodeLine> expandSpacerOnBottom(SpacerCodeLine spacer, List<DiffViewCodeLine> diffCodeLines) {
        int beginIndex = spacer.getBeginIndex().get();
        int endIndex = beginIndex + expandLines - 1;
        if (endIndex >= diffCodeLines.size() - 1) {
            return diffCodeLines.subList(beginIndex, diffCodeLines.size()).stream().collect(toList());
        }
        List<RelevantCodeLine> spacedList = new ArrayList<>();
        spacedList.addAll(diffCodeLines.subList(beginIndex, endIndex + 1));
        spacedList.add(new SpacerCodeLine(Optional.of(endIndex + 1), Optional.empty()));
        return spacedList;
    }

    private List<RelevantCodeLine> expandSpacerOnBothSides(SpacerCodeLine spacer, List<DiffViewCodeLine> diffCodeLines) {
        int beginIndex = spacer.getBeginIndex().get();
        int endIndex = spacer.getEndIndex().get();
        if ((endIndex - beginIndex + 1) <= expandLines * 2) {
            return diffCodeLines.subList(beginIndex, endIndex + 1).stream().collect(toList());
        }
        List<RelevantCodeLine> spacedList = new ArrayList<>();
        spacedList.addAll(diffCodeLines.subList(beginIndex, beginIndex + expandLines));
        spacedList.add(new SpacerCodeLine(Optional.of(beginIndex + expandLines), Optional.of(endIndex - expandLines)));
        spacedList.addAll(diffCodeLines.subList(endIndex - expandLines + 1, endIndex + 1));
        return spacedList;
    }
}
