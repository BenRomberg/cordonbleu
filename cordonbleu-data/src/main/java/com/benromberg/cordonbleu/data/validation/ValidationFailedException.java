package com.benromberg.cordonbleu.data.validation;

public class ValidationFailedException extends RuntimeException {
    public ValidationFailedException(String format, Object... args) {
        super(String.format(format, args));
    }
}
