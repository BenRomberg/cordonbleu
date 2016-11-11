package com.benromberg.cordonbleu.main.auth;

import com.benromberg.cordonbleu.service.user.UserService;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import javax.inject.Inject;

import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.google.common.base.Optional;

public class UserAuthenticator implements Authenticator<String, UserWithPermissions> {
    private final UserService userService;

    @Inject
    public UserAuthenticator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<UserWithPermissions> authenticate(String sessionToken) throws AuthenticationException {
        return Optional
                .fromNullable(userService.verifySession(sessionToken).map(UserWithPermissions::new).orElse(null));
    }
}
