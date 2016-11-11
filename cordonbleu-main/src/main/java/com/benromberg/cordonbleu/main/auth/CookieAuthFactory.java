package com.benromberg.cordonbleu.main.auth;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benromberg.cordonbleu.main.permission.UserWithPermissions;

public class CookieAuthFactory extends AuthFactory<String, UserWithPermissions> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CookieAuthFactory.class);

    @Context
    private HttpServletRequest request;

    private final String cookieName;
    private final boolean required;

    private CookieAuthFactory(boolean required, Authenticator<String, UserWithPermissions> authenticator,
            String cookieName) {
        super(authenticator);
        this.required = required;
        this.cookieName = cookieName;
    }

    public CookieAuthFactory(Authenticator<String, UserWithPermissions> authenticator, String cookieName) {
        this(false, authenticator, cookieName);
    }

    @Override
    public UserWithPermissions provide() {
        if (request == null || request.getCookies() == null) {
            return failedAuthentication();
        }
        Optional<Cookie> cookie = Stream.of(request.getCookies())
                .filter(cookieItem -> cookieItem.getName().equals(cookieName)).findFirst();
        if (!cookie.isPresent()) {
            return failedAuthentication();
        }
        try {
            return authenticator().authenticate(cookie.get().getValue()).or(() -> failedAuthentication());
        } catch (AuthenticationException e) {
            LOGGER.error("Error authenticating credentials", e);
            throw new InternalServerErrorException();
        }
    }

    private UserWithPermissions failedAuthentication() {
        if (required) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return UserWithPermissions.forAnonymousUser();
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public AuthFactory<String, UserWithPermissions> clone(boolean required) {
        return new CookieAuthFactory(required, authenticator(), cookieName);
    }

    @Override
    public Class<UserWithPermissions> getGeneratedClass() {
        return UserWithPermissions.class;
    }

}