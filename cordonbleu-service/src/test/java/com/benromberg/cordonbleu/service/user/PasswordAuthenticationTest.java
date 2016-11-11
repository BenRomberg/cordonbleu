package com.benromberg.cordonbleu.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.validation.ValidationFailedException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.benromberg.cordonbleu.service.user.PasswordAuthentication;

public class PasswordAuthenticationTest {
    private static final int PASSWORD_MINIMUM_LENGTH = 6;
    public static final PasswordAuthentication PASSWORD_AUTHENTICATION_TEST_INSTANCE = new PasswordAuthentication(
            () -> PASSWORD_MINIMUM_LENGTH);
    private static final String TEST_PASSWORD = "test password";
    private PasswordAuthentication authentication;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        authentication = PASSWORD_AUTHENTICATION_TEST_INSTANCE;
    }

    @Test
    public void shortPassword_ThrowsValidationException() throws Exception {
        expectedException.expect(ValidationFailedException.class);
        authentication.encrypt("short");
    }

    @Test
    public void encryptedPassword_CanBeVerified() throws Exception {
        String encryptedPassword = authentication.encrypt(TEST_PASSWORD);
        assertThat(authentication.verify(TEST_PASSWORD, encryptedPassword)).isTrue();
    }

    @Test
    public void encryptedPassword_IsNotVerified_OnWrongPassword() throws Exception {
        String encryptedPassword = authentication.encrypt(TEST_PASSWORD);
        assertThat(authentication.verify("other password", encryptedPassword)).isFalse();
    }
}
