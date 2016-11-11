package com.benromberg.cordonbleu.main.config;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import com.benromberg.cordonbleu.data.model.NamedEntity;
import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;
import com.benromberg.cordonbleu.data.validation.NameValidation;
import com.benromberg.cordonbleu.data.validation.UserValidation;
import com.benromberg.cordonbleu.service.user.PasswordValidation;
import com.benromberg.cordonbleu.util.ClasspathUtil;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SharedConfig implements UserValidation, PasswordValidation {
    private static final SharedConfig INSTANCE = convertException(() -> JsonMapper.getInstance().readValue(
            ClasspathUtil.readFileFromClasspath("webpack/sharedConfig.json"), SharedConfig.class));

    public static SharedConfig sharedConfig() {
        return INSTANCE;
    }

    @JsonProperty
    private final int passwordMinimumLength;

    @JsonProperty
    private final int nameMaximumLength;

    @JsonProperty
    private final String namePattern;

    @JsonCreator
    private SharedConfig(int passwordMinimumLength, int nameMaximumLength, String namePattern) {
        this.passwordMinimumLength = passwordMinimumLength;
        this.nameMaximumLength = nameMaximumLength;
        this.namePattern = namePattern;
    }

    @Override
    public int getPasswordMinimumLength() {
        return passwordMinimumLength;
    }

    @Override
    public int getNameMaximumLength() {
        return nameMaximumLength;
    }

    @Override
    public String getNamePattern() {
        return namePattern;
    }

    public NameValidation<NamedEntity> getNameValidation() {
        return new NameValidation<NamedEntity>() {
            @Override
            public int getNameMaximumLength() {
                return nameMaximumLength;
            }

            @Override
            public String getNamePattern() {
                return namePattern;
            }
        };
    }
}
