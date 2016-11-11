package com.benromberg.cordonbleu.main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

public class RequestBuilder {
    private WebTarget target;
    private final List<Function<Builder, Builder>> builderInterceptors = new ArrayList<>();

    public RequestBuilder(WebTarget target) {
        this.target = target;
    }

    public RequestBuilder param(String name, Object... values) {
        target = target.queryParam(name, values);
        return this;
    }

    public RequestBuilder header(String name, Object value) {
        builderInterceptors.add(builder -> builder.header(name, value));
        return this;
    }

    public Response post(String path, Object entity) {
        return buildRequest(path).post(Entity.json(entity));
    }

    public Response get(String path) {
        return buildRequest(path).get();
    }

    private Builder buildRequest(String path) {
        Builder requestBuilder = target.path(path).request();
        for (Function<Builder, Builder> interceptor : builderInterceptors) {
            requestBuilder = interceptor.apply(requestBuilder);
        }
        return requestBuilder;
    }
}