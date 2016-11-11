package com.benromberg.cordonbleu.main.exception;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EntityExistsExceptionMapper implements ExceptionMapper<EntityExistsException> {
    @Override
    public Response toResponse(EntityExistsException exception) {
        return Response.status(Status.CONFLICT).build();
    }
}