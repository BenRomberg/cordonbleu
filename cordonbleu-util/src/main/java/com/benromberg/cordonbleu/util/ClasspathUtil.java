package com.benromberg.cordonbleu.util;

import static com.benromberg.cordonbleu.util.ExceptionUtil.convertException;
import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class ClasspathUtil {
    public static InputStream readStreamFromClasspath(String path) {
        return ClasspathUtil.class.getClassLoader().getResourceAsStream(path);
    }

    public static String readFileFromClasspath(String path) {
        return readLinesFromClasspath(path).stream().collect(joining("\n"));
    }

    public static List<String> readLinesFromClasspath(String path) {
        InputStream inputStream = readStreamFromClasspath(path);
        return convertException(() -> IOUtils.readLines(inputStream));
    }
}
