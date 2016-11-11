package com.benromberg.cordonbleu.service.diff;

import java.util.Optional;

public class SpacerCodeLine implements RelevantCodeLine {
    private final Optional<Integer> beginIndex;
    private final Optional<Integer> endIndex;

    public SpacerCodeLine(Optional<Integer> beginIndex, Optional<Integer> endIndex) {
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    public Optional<Integer> getBeginIndex() {
        return beginIndex;
    }

    public Optional<Integer> getEndIndex() {
        return endIndex;
    }

    @Override
    public SpacerCodeLine getSpacer() {
        return this;
    }

    @Override
    public boolean isSpacer() {
        return true;
    }

}
