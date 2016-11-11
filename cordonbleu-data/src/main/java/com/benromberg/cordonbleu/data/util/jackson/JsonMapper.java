package com.benromberg.cordonbleu.data.util.jackson;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {
    private static final String PACKAGE_SEPARATOR = ".";
    private static final ObjectMapper MAPPER_INSTANCE = wrapInstance(new ObjectMapper());

    public static ObjectMapper getInstance() {
        return MAPPER_INSTANCE;
    }

    public static ObjectMapper wrapInstance(ObjectMapper mapper) {
        mapper.findAndRegisterModules();
        mapper.registerModule(new NullPropertyPreventingModule(ownPackagePrefix()));
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        return mapper;
    }

    private static String ownPackagePrefix() {
        String ownPackageName = JsonMapper.class.getPackage().getName();
        String[] packageParts = ownPackageName.split(Pattern.quote(PACKAGE_SEPARATOR));
        return packageParts[0] + PACKAGE_SEPARATOR + packageParts[1];
    }

}