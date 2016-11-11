package com.benromberg.cordonbleu.service.user;

import javax.inject.Inject;

import com.lambdaworks.crypto.SCryptUtil;

public class PasswordAuthentication {
    private static final int CPU_COST = 16384;
    private static final int MEMORY_COST = 8;
    private static final int PARALLELIZATION = 4;

    private PasswordValidation validation;

    @Inject
    public PasswordAuthentication(PasswordValidation validation) {
        this.validation = validation;
    }

    public String encrypt(String password) {
        validation.validatePassword(password);
        return SCryptUtil.scrypt(password, CPU_COST, MEMORY_COST, PARALLELIZATION);
    }

    public boolean verify(String providedPassword, String encryptedPassword) {
        return SCryptUtil.check(providedPassword, encryptedPassword);
    }

}
