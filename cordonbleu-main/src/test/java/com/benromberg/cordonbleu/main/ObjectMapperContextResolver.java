package com.benromberg.cordonbleu.main;

import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;

import javax.ws.rs.ext.ContextResolver;

import com.benromberg.cordonbleu.main.util.jackson.CustomModule;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    private ObjectMapper mapper = CustomModule.enhanceMapper(JsonMapper.getInstance());

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}