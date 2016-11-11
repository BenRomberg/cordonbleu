package com.benromberg.cordonbleu.main.exception;

import com.benromberg.cordonbleu.data.validation.ValidationFailedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationFailedExceptionMapper implements ExceptionMapper<ValidationFailedException> {
    @Override
    public Response toResponse(ValidationFailedException exception) {
        return Response.status(Status.BAD_REQUEST).build();
    }
}
