package com.benromberg.cordonbleu.service.user;

import com.benromberg.cordonbleu.data.validation.Validation;

public interface PasswordValidation {
    int getPasswordMinimumLength();

    default void validatePassword(String password) {
        Validation.validateStringMinimumLength("password", password, getPasswordMinimumLength());
    }
}
