package com.benromberg.cordonbleu.main;

import com.benromberg.cordonbleu.main.config.TestModule;

import com.benromberg.cordonbleu.main.CordonBleuApplication;
import com.benromberg.cordonbleu.main.config.GuiceModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class CordonBleuTestApplication extends CordonBleuApplication {
    public CordonBleuTestApplication() {
        super(Modules.override(new GuiceModule()).with(new TestModule()));
    }

    @Override
    public Injector getInjector() {
        return super.getInjector();
    }

    @Override
    protected void scheduleTasks() {
    }
}
