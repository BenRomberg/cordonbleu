package com.benromberg.cordonbleu.main.resource.configuration;

import javax.inject.Singleton;

import com.benromberg.cordonbleu.main.config.WebsiteVersionFeature;
import com.fasterxml.jackson.annotation.JsonProperty;

@Singleton
public class Configuration {
    @JsonProperty
    public String getWebsiteVersion() {
        return WebsiteVersionFeature.WEBSITE_VERSION;
    }
}
