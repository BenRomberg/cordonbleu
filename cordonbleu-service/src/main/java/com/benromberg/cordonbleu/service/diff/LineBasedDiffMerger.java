package com.benromberg.cordonbleu.service.diff;

import static com.benromberg.cordonbleu.service.diff.DiffFragment.fromDiff;
import static java.util.stream.Collectors.toList;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Stream;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Diff;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Operation;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatchWrapper;

public class LineBasedDiffMerger {
    public List<DiffFragment> merge(Optional<String> before, Optional<String> after) {
        LinkedList<Diff> diffs = DiffMatchPatchWrapper.diffLinesOnly(before.orElse(""), after.orElse(""));
        ensureSingleDiffForEmptySource(diffs, before, after);
        removeDisturbingLineEnds(diffs);
        return diffs
                .stream()
                .<DiffFragment> flatMap(
                        diff -> Stream.of(diff.text.split("\n", -1)).map(text -> fromDiff(diff.operation, text)))
                .collect(toList());
    }

    private void ensureSingleDiffForEmptySource(LinkedList<Diff> diffs, Optional<String> before, Optional<String> after) {
        if (!diffs.isEmpty()) {
            return;
        }
        Operation operation = before.map(
                beforeContent -> after.map(afterContent -> Operation.EQUAL).orElse(Operation.DELETE)).orElse(
                Operation.INSERT);
        diffs.add(new Diff(operation, ""));
    }

    private void removeDisturbingLineEnds(LinkedList<Diff> diffs) {
        ListIterator<Diff> iterator = diffs.listIterator(diffs.size() - diffsForLastLine(diffs));
        while (iterator.hasPrevious()) {
            Diff diff = iterator.previous();
            diff.text = diff.text.substring(0, diff.text.length() - 1);
        }
    }

    private int diffsForLastLine(LinkedList<Diff> diffs) {
        if (diffs.size() < 2) {
            return diffs.size();
        }
        if (diffs.getLast().operation == Operation.EQUAL || diffs.get(diffs.size() - 2).operation == Operation.EQUAL) {
            return 1;
        }
        return 2;
    }
}
