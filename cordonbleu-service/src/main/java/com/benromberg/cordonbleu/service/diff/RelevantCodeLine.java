package com.benromberg.cordonbleu.service.diff;

import java.util.NoSuchElementException;

public interface RelevantCodeLine {
    boolean isSpacer();

    default DiffViewCodeLine getLine() {
        throw new NoSuchElementException();
    }

    default SpacerCodeLine getSpacer() {
        throw new NoSuchElementException();
    }
}
