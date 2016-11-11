package com.benromberg.cordonbleu.main.permission;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;
import com.benromberg.cordonbleu.util.ClasspathUtil;

import com.benromberg.cordonbleu.main.config.CordonBleuConfiguration;
import com.google.inject.AbstractModule;

public class UnitTestModule extends AbstractModule {
    @Override
    protected void configure() {
        CordonBleuConfiguration configuration = convertException(() -> JsonMapper.getInstance().readValue(
                ClasspathUtil.readFileFromClasspath("config-unittest.json"), CordonBleuConfiguration.class));
        bind(CordonBleuConfiguration.class).toInstance(configuration);
    }
}
