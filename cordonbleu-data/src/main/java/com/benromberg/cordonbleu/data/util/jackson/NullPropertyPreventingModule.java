package com.benromberg.cordonbleu.data.util.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class NullPropertyPreventingModule extends SimpleModule {
    public NullPropertyPreventingModule(String basePackage) {
        setValueInstantiators(new PreventDefaultConstructorValueInstantiators(basePackage));
    }
}
