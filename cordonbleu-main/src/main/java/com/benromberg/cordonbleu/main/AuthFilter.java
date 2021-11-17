package com.benromberg.cordonbleu.main;

import com.benromberg.cordonbleu.main.config.CordonBleuConfiguration;
import com.benromberg.cordonbleu.main.config.Credentials;

import java.util.Base64;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class AuthFilter implements ContainerRequestFilter {
    private final Optional<Credentials> credentials;

    @Inject
    public AuthFilter(CordonBleuConfiguration configuration) {
        credentials = configuration.getGlobalCredentials();
    }

    @Override
    public void filter(ContainerRequestContext containerRequest) throws WebApplicationException {
        if (!credentials.isPresent()) {
            return;
        }
        String auth = containerRequest.getHeaderString("authorization");
        Response errorResponse = Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic").build();
        if (auth == null) {
            throw new WebApplicationException(errorResponse);
        }
        String[] lap = decode(auth);
        if (lap == null || lap.length != 2) {
            throw new WebApplicationException(errorResponse);
        }
        if (!(lap[0].equals(credentials.get().getUsername()) && lap[1].equals(credentials.get().getPassword()))) {
            throw new WebApplicationException(errorResponse);
        }
    }

    public static String[] decode(String auth) {
        auth = auth.replaceFirst("[B|b]asic ", "");
        byte[] decodedBytes = Base64.getDecoder().decode(auth);
        return new String(decodedBytes).split(":", 2);
    }
}