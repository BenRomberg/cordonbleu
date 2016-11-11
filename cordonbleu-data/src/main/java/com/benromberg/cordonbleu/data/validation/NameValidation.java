package com.benromberg.cordonbleu.data.validation;

import com.benromberg.cordonbleu.data.model.NamedEntity;

public interface NameValidation<E extends NamedEntity> extends Validation<E> {
    int getNameMaximumLength();

    String getNamePattern();

    @Override
    default void validateEntity(NamedEntity entity) {
        validateName(entity.getName());
    }

    default void validateName(String name) {
        Validation.validateStringNotEmpty(NamedEntity.NAME_PROPERTY, name);
        Validation.validateStringMaximumLength(NamedEntity.NAME_PROPERTY, name, getNameMaximumLength());
        Validation.validateStringPattern(NamedEntity.NAME_PROPERTY, name, getNamePattern());
    }
}
