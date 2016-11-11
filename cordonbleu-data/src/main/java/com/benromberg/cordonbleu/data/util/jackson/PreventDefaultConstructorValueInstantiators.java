package com.benromberg.cordonbleu.data.util.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;

public class PreventDefaultConstructorValueInstantiators extends SimpleValueInstantiators {
    private String basePackage;

    public PreventDefaultConstructorValueInstantiators(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDescription,
            ValueInstantiator defaultInstantiator) {
        ValueInstantiator instantiator = super.findValueInstantiator(config, beanDescription, defaultInstantiator);
        if (!beanDescription.getBeanClass().getPackage().getName().startsWith(basePackage)) {
            return instantiator;
        }
        return new PreventDefaultConstructorDelegateValueInstantiator(instantiator);
    }
}