package com.benromberg.cordonbleu.main;

import com.benromberg.cordonbleu.util.ClasspathUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class CatchAllServlet extends HttpServlet {
    private static final String FAVICON_FILENAME = "favicon.ico";
    private static final String FAVICON_REQUEST_PATH = "/" + FAVICON_FILENAME;
    private static final String FAVICON_CLASSPATH = "static/img/" + FAVICON_FILENAME;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (FAVICON_REQUEST_PATH.equals(request.getRequestURI())) {
            respondFromClasspath(FAVICON_CLASSPATH, response, "image/ico");
            return;
        }
        respondFromClasspath("index.html", response, "text/html");
    }

    private void respondFromClasspath(String classpath, HttpServletResponse response, String contentType)
            throws IOException {
        response.setContentType(contentType);
        IOUtils.copy(ClasspathUtil.readStreamFromClasspath(classpath), response.getOutputStream());
    }
}
