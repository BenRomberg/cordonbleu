package com.benromberg.cordonbleu.data.validation;

import java.util.List;

import com.benromberg.cordonbleu.data.model.NamedEntity;
import com.benromberg.cordonbleu.data.model.User;

public interface UserValidation extends NameValidation<User> {
    @Override
    default void validateEntity(User entity) {
        NameValidation.super.validateEntity((NamedEntity) entity);
        validateEmail(entity.getEmail(), entity.getEmailAliases());
    }

    default void validateEmail(String email, List<String> emailAliases) {
        Validation.validateStringNotEmpty(User.EMAIL_PROPERTY, email);
        emailAliases.forEach(alias -> Validation.validateStringNotEmpty(User.EMAIL_ALIASES_PROPERTY, alias));
    }
}
