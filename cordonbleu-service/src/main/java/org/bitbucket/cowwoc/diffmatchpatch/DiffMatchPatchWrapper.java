package org.bitbucket.cowwoc.diffmatchpatch;

import java.util.LinkedList;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Diff;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.LinesToCharsResult;

public class DiffMatchPatchWrapper {
    private static final DiffMatchPatch DIFF_MATCH_PATCH = new DiffMatchPatch();

    public static LinkedList<Diff> diffLinesOnly(String text1, String text2) {
        LinesToCharsResult linesAsChars = DIFF_MATCH_PATCH.diffLinesToChars(text1, text2);
        LinkedList<Diff> diffs = DIFF_MATCH_PATCH.diffMain(linesAsChars.chars1, linesAsChars.chars2, false);
        DIFF_MATCH_PATCH.diffCharsToLines(diffs, linesAsChars.lineArray);
        return diffs;
    }

    public static LinkedList<Diff> diffWithinLine(String text1, String text2) {
        LinkedList<Diff> diffs = DIFF_MATCH_PATCH.diffMain(text1, text2);
        DIFF_MATCH_PATCH.diffCleanupSemantic(diffs);
        return diffs;
    }
}
