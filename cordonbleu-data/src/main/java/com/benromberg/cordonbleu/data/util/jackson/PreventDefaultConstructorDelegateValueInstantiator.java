package com.benromberg.cordonbleu.data.util.jackson;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;

public class PreventDefaultConstructorDelegateValueInstantiator extends ValueInstantiator {
    private final ValueInstantiator delegate;

    public PreventDefaultConstructorDelegateValueInstantiator(ValueInstantiator delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean canCreateUsingDefault() {
        return false;
    }

    @Override
    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
        int nullValueIndex;
        if ((nullValueIndex = nullValueIndex(asList(args))) >= 0) {
            if(!"com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata".equals(delegate.getWithArgsCreator().getContextClass().getName())) { // TODO see to delete it :/ (it's probably due to null value on new attribute `type` on database)
                Type parameterType = delegate.getWithArgsCreator().getParameter(nullValueIndex).getGenericType();
                throw createException(String.format("null creator parameter of type %s not allowed.", parameterType));
            }
        }
        long fieldCount = countFields();
        if (args.length != fieldCount) {
            throw createException(String.format("Expected creator with %d parameters, found %d.", fieldCount,
                    args.length));
        }
        return delegate.createFromObjectWith(ctxt, args);
    }

    private long countFields() {
        AnnotatedWithParams creator = delegate.getWithArgsCreator();
        AnnotatedClass contextClass = creator.getContextClass();
        if (!creator.hasAnnotation(IgnoreSuperClassFields.class)) {
            return contextClass.getFieldCount();
        }
        Class<?> jdkClass = contextClass.getAnnotated();
        return StreamSupport.stream(contextClass.fields().spliterator(), false)
                .filter(field -> field.getDeclaringClass().equals(jdkClass)).count();
    }

    private JsonMappingException createException(String message) {
        String className = delegate.getWithArgsCreator().getContextClass().getName();
        return new JsonMappingException(String.format("Error when deserializing %s: %s", className, message));
    }

    private int nullValueIndex(Collection<?> stream) {
        Iterator<?> iterator = stream.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Object currentValue = iterator.next();
            if (currentValue == null
                    || (currentValue instanceof Collection && nullValueIndex((Collection<?>) currentValue) >= 0)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public String getValueTypeDesc() {
        return delegate.getValueTypeDesc();
    }

    @Override
    public boolean canInstantiate() {
        return delegate.canInstantiate();
    }

    @Override
    public boolean canCreateFromString() {
        return delegate.canCreateFromString();
    }

    @Override
    public boolean canCreateFromInt() {
        return delegate.canCreateFromInt();
    }

    @Override
    public boolean canCreateFromLong() {
        return delegate.canCreateFromLong();
    }

    @Override
    public boolean canCreateFromDouble() {
        return delegate.canCreateFromDouble();
    }

    @Override
    public boolean canCreateFromBoolean() {
        return delegate.canCreateFromBoolean();
    }

    @Override
    public boolean canCreateUsingDelegate() {
        return delegate.canCreateUsingDelegate();
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return delegate.canCreateFromObjectWith();
    }

    @Override
    public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
        return delegate.getFromObjectArguments(config);
    }

    @Override
    public JavaType getDelegateType(DeserializationConfig config) {
        return delegate.getDelegateType(config);
    }

    @Override
    public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
        return delegate.createUsingDefault(ctxt);
    }

    @Override
    public Object createUsingDelegate(DeserializationContext ctxt, Object object) throws IOException {
        return delegate.createUsingDelegate(ctxt, object);
    }

    @Override
    public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
        return delegate.createFromString(ctxt, value);
    }

    @Override
    public Object createFromInt(DeserializationContext ctxt, int value) throws IOException {
        return delegate.createFromInt(ctxt, value);
    }

    @Override
    public Object createFromLong(DeserializationContext ctxt, long value) throws IOException {
        return delegate.createFromLong(ctxt, value);
    }

    @Override
    public Object createFromDouble(DeserializationContext ctxt, double value) throws IOException {
        return delegate.createFromDouble(ctxt, value);
    }

    @Override
    public Object createFromBoolean(DeserializationContext ctxt, boolean value) throws IOException {
        return delegate.createFromBoolean(ctxt, value);
    }

    @Override
    public AnnotatedWithParams getDefaultCreator() {
        return delegate.getDefaultCreator();
    }

    @Override
    public AnnotatedWithParams getDelegateCreator() {
        return delegate.getDelegateCreator();
    }

    @Override
    public AnnotatedWithParams getWithArgsCreator() {
        return delegate.getWithArgsCreator();
    }

    @Override
    public AnnotatedParameter getIncompleteParameter() {
        return delegate.getIncompleteParameter();
    }

}
