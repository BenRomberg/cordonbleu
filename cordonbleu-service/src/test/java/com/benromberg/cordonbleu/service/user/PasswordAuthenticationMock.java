package com.benromberg.cordonbleu.service.user;

import com.benromberg.cordonbleu.service.user.PasswordAuthentication;

public class PasswordAuthenticationMock extends PasswordAuthentication {

    public PasswordAuthenticationMock() {
        super(null);
    }

    @Override
    public String encrypt(String password) {
        return password;
    }

    @Override
    public boolean verify(String providedPassword, String encryptedPassword) {
        return providedPassword.equals(encryptedPassword);
    }

}
