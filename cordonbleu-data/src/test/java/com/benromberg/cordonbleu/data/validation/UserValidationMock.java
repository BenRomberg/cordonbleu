package com.benromberg.cordonbleu.data.validation;

import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.validation.UserValidation;

public class UserValidationMock extends NameValidationMock<User> implements UserValidation {
    @Override
    public void validateEntity(User entity) {
        UserValidation.super.validateEntity(entity);
    }
}
