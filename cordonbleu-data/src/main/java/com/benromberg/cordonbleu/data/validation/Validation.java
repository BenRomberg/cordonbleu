package com.benromberg.cordonbleu.data.validation;

import java.util.regex.Pattern;

public interface Validation<E> {
    void validateEntity(E entity);

    static void validateStringMaximumLength(String name, String text, int maximumLength) {
        if (text.length() > maximumLength) {
            throw new ValidationFailedException("%s with value '%s' is longer than maximum length %d.", name, text,
                    maximumLength);
        }
    }

    static void validateStringMinimumLength(String name, String text, int minimumLength) {
        if (text.length() < minimumLength) {
            throw new ValidationFailedException("%s with value '%s' is shorter than minimum length %d.", name, text,
                    minimumLength);
        }
    }

    static void validateStringNotEmpty(String name, String text) {
        if (text.isEmpty()) {
            throw new ValidationFailedException("%s cannot be empty.", name);
        }
    }

    static void validateStringPattern(String name, String text, String pattern) {
        if (!Pattern.matches(pattern, text)) {
            throw new ValidationFailedException("%s with value '%s' does not match pattern '%s'.", name, text, pattern);
        }
    }
}
