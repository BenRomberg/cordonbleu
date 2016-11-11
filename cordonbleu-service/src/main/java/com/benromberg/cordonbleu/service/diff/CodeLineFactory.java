package com.benromberg.cordonbleu.service.diff;

import com.benromberg.cordonbleu.data.model.CommitLineNumber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CodeLineFactory {
    private final Counter beforeCounter = new Counter();
    private final Counter afterCounter = new Counter();
    private final Map<DiffStatus, Incrementable> incrementorMap = createIncrementorMap();
    private final List<String> highlightBefore;
    private final List<String> highlightAfter;

    public CodeLineFactory(List<String> highlightBefore, List<String> highlightAfter) {
        this.highlightBefore = highlightBefore;
        this.highlightAfter = highlightAfter;
    }

    private Map<DiffStatus, Incrementable> createIncrementorMap() {
        HashMap<DiffStatus, Incrementable> map = new HashMap<>();
        map.put(DiffStatus.BEFORE, beforeCounter);
        map.put(DiffStatus.AFTER, afterCounter);
        map.put(DiffStatus.KEEP, new BackedIncrementor(beforeCounter, afterCounter));
        return map;
    }

    public DiffViewCodeLine nextLine(DiffStatus status) {
        incrementorMap.get(status).increment();
        Optional<Integer> beforeLine = getBeforeCounter(status);
        Optional<Integer> afterLine = getAfterCounter(status);
        return new DiffViewCodeLine(new CommitLineNumber(beforeLine, afterLine), status, beforeLine.map(
                line -> highlightBefore.get(line - 1)).orElseGet(() -> highlightAfter.get(afterLine.get() - 1)));
    }

    public Optional<Integer> getBeforeCounter(DiffStatus status) {
        if (status == DiffStatus.AFTER) {
            return Optional.empty();
        }
        return Optional.of(beforeCounter.get());
    }

    public Optional<Integer> getAfterCounter(DiffStatus status) {
        if (status == DiffStatus.BEFORE) {
            return Optional.empty();
        }
        return Optional.of(afterCounter.get());
    }

    private static class Counter implements Incrementable {
        private int count;

        @Override
        public void increment() {
            count++;
        }

        public int get() {
            return count;
        }
    }

    private static class BackedIncrementor implements Incrementable {
        private final List<Incrementable> baseIncrementors;

        public BackedIncrementor(Incrementable... baseIncrementors) {
            this.baseIncrementors = Arrays.asList(baseIncrementors);
        }

        @Override
        public void increment() {
            baseIncrementors.stream().forEach(baseIncrementor -> baseIncrementor.increment());
        }
    }

    private interface Incrementable {
        void increment();
    }
}
