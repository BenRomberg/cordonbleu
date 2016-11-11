package com.benromberg.cordonbleu.data.validation;

import com.benromberg.cordonbleu.data.model.NamedEntity;
import com.benromberg.cordonbleu.data.validation.NameValidation;

public class NameValidationMock<E extends NamedEntity> implements NameValidation<E> {
    public static final String NAME_PATTERN = "[a-zA-Z0-9-_]+";
    public static final int NAME_MAXIMUM_LENGTH = 16;

    @Override
    public int getNameMaximumLength() {
        return NAME_MAXIMUM_LENGTH;
    }

    @Override
    public String getNamePattern() {
        return NAME_PATTERN;
    }
}
