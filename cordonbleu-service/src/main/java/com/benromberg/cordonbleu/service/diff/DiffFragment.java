package com.benromberg.cordonbleu.service.diff;

import java.util.HashMap;
import java.util.Map;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Operation;

public class DiffFragment {
    private static Map<Operation, DiffStatus> statusMap = createStatusMap();

    private static Map<Operation, DiffStatus> createStatusMap() {
        HashMap<Operation, DiffStatus> map = new HashMap<>();
        map.put(Operation.DELETE, DiffStatus.BEFORE);
        map.put(Operation.EQUAL, DiffStatus.KEEP);
        map.put(Operation.INSERT, DiffStatus.AFTER);
        return map;
    }

    public static DiffFragment fromDiff(Operation operation, String text) {
        return new DiffFragment(statusMap.get(operation), text);
    }

    private final DiffStatus status;
    private final String text;

    public DiffFragment(DiffStatus status, String text) {
        this.status = status;
        this.text = text;
    }

    public DiffStatus getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }

}
