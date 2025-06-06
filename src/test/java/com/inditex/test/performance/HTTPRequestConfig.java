package com.inditex.test.performance;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequestConfig {
    private String name;
    private String domain;
    private int port;
    private String path;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private String bodyData;

    public HTTPRequestConfig() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.port = 80; // default HTTP port
        this.method = "GET"; // default method
    }

    public HTTPRequestConfig(String name, String domain, String path, String method) {
        this();
        this.name = name;
        this.domain = domain;
        this.path = path;
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public String getBodyData() {
        return bodyData;
    }

    public void setBodyData(String bodyData) {
        this.bodyData = bodyData;
    }

    // Helper methods for common configurations
    public static HTTPRequestConfig createGetRequest(String name, String domain, String path) {
        return new HTTPRequestConfig(name, domain, path, "GET");
    }

    public static HTTPRequestConfig createPostRequest(String name, String domain, String path, String bodyData) {
        HTTPRequestConfig config = new HTTPRequestConfig(name, domain, path, "POST");
        config.setBodyData(bodyData);
        config.addHeader("Content-Type", "application/json");
        return config;
    }

    public static HTTPRequestConfig createApiRequest(String name, String domain, String path, String method, String authToken) {
        HTTPRequestConfig config = new HTTPRequestConfig(name, domain, path, method);
        config.addHeader("Authorization", "Bearer " + authToken);
        config.addHeader("Content-Type", "application/json");
        config.addHeader("Accept", "application/json");
        return config;
    }

    @Override
    public String toString() {
        return String.format("HTTPRequestConfig{name='%s', method='%s', domain='%s', path='%s'}",
                name, method, domain, path);
    }
}
