package com.inditex.test.performance;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadTestConfig {
    private String testName;
    private int numThreads;
    private int rampUpTime;
    private int loopCount;
    private List<HTTPRequestConfig> httpRequests;
    private Map<String, String> userVariables;

    public LoadTestConfig() {
        this.httpRequests = new ArrayList<>();
        this.userVariables = new HashMap<>();
    }

    public LoadTestConfig(String testName, int numThreads, int rampUpTime, int loopCount) {
        this.testName = testName;
        this.numThreads = numThreads;
        this.rampUpTime = rampUpTime;
        this.loopCount = loopCount;
        this.httpRequests = new ArrayList<>();
        this.userVariables = new HashMap<>();
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getRampUpTime() {
        return rampUpTime;
    }

    public void setRampUpTime(int rampUpTime) {
        this.rampUpTime = rampUpTime;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public List<HTTPRequestConfig> getHttpRequests() {
        return httpRequests;
    }

    public void setHttpRequests(List<HTTPRequestConfig> httpRequests) {
        this.httpRequests = httpRequests;
    }

    public void addHttpRequest(HTTPRequestConfig request) {
        this.httpRequests.add(request);
    }

    public Map<String, String> getUserVariables() {
        return userVariables;
    }

    public void setUserVariables(Map<String, String> userVariables) {
        this.userVariables = userVariables;
    }

    public void addUserVariable(String name, String value) {
        this.userVariables.put(name, value);
    }

    @Override
    public String toString() {
        return String.format("LoadTestConfig{testName='%s', numThreads=%d, rampUpTime=%d, loopCount=%d, requests=%d}",
                testName, numThreads, rampUpTime, loopCount, httpRequests.size());
    }
}
