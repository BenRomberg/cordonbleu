package com.benromberg.cordonbleu.main.config;

import static com.benromberg.cordonbleu.util.ClasspathUtil.readLinesFromClasspath;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import com.benromberg.cordonbleu.service.coderepository.ChecksumUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class WebsiteVersionFeature implements DynamicFeature {
    public static final String WEBSITE_VERSION = createWebsiteHash();
    public static final String WEBSITE_VERSION_HEADER = "X-WebsiteVersion";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteVersionFeature.class);
    private static final String WEBSITE_CHECKSUMS_FILE = "website-checksums.csv";

    private static String createWebsiteHash() {
        List<String> websiteChecksums;
        try {
            websiteChecksums = readLinesFromClasspath(WEBSITE_CHECKSUMS_FILE);
        } catch (Exception e) {
            websiteChecksums = emptyList();
        }
        Collections.sort(websiteChecksums);
        return ChecksumUtil.stringToMd5HexChecksum(websiteChecksums.stream().collect(joining()));
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(WebsiteVersionNotRequired.class) == null
                && resourceInfo.getResourceClass().getAnnotation(WebsiteVersionNotRequired.class) == null) {
            context.register(WebsiteVersionFilter.class);
        }
    }

    public static class WebsiteVersionFilter implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            String websiteVersionHeader = requestContext.getHeaderString(WEBSITE_VERSION_HEADER);
            if (websiteVersionHeader == null) {
                LOGGER.warn("No version header found on request to {}.", requestContext.getUriInfo().getRequestUri());
                return;
            }
            if (!websiteVersionHeader.equals(WEBSITE_VERSION)) {
                String errorMessage = String.format("Website version mismatch, expected %s, actual %s.",
                        WEBSITE_VERSION, websiteVersionHeader);
                throw new WebApplicationException(errorMessage, Response.Status.PRECONDITION_FAILED);
            }
        }
    }
}
