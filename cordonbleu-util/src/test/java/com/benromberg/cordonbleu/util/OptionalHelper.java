package com.benromberg.cordonbleu.util;

import java.util.Optional;

public class OptionalHelper {
    public static Optional<String> toOptional(String value) {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public static Optional<Integer> toOptional(int value) {
        if (value < 0) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
