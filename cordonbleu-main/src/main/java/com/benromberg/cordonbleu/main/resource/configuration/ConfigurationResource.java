package com.benromberg.cordonbleu.main.resource.configuration;

import com.benromberg.cordonbleu.data.util.jackson.JsonMapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.benromberg.cordonbleu.main.config.WebsiteVersionNotRequired;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;

@Path("/configuration.js")
public class ConfigurationResource {
    private final Configuration configuration;

    @Inject
    public ConfigurationResource(Configuration configuration) {
        this.configuration = configuration;
    }

    @GET
    @Timed
    @WebsiteVersionNotRequired
    public String getConfiguration() throws JsonProcessingException {
        return "window.configuration = " + JsonMapper.getInstance().writeValueAsString(configuration);
    }
}
