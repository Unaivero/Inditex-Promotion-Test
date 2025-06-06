package com.inditex.test.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StressTestConfig {
    private String testName;
    private int initialUsers;
    private int maxUsers;
    private int rampUpTime;
    private int duration; // in seconds
    private int thinkTime; // in milliseconds
    private List<HTTPRequestConfig> httpRequests;
    private Map<String, String> userVariables;
    private PerformanceThresholds thresholds;

    public StressTestConfig() {
        this.httpRequests = new ArrayList<>();
        this.userVariables = new HashMap<>();
        this.thresholds = new PerformanceThresholds();
    }

    public StressTestConfig(String testName, int initialUsers, int maxUsers, int rampUpTime, int duration) {
        this();
        this.testName = testName;
        this.initialUsers = initialUsers;
        this.maxUsers = maxUsers;
        this.rampUpTime = rampUpTime;
        this.duration = duration;
        this.thinkTime = 1000; // default 1 second think time
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public int getInitialUsers() {
        return initialUsers;
    }

    public void setInitialUsers(int initialUsers) {
        this.initialUsers = initialUsers;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public int getRampUpTime() {
        return rampUpTime;
    }

    public void setRampUpTime(int rampUpTime) {
        this.rampUpTime = rampUpTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getThinkTime() {
        return thinkTime;
    }

    public void setThinkTime(int thinkTime) {
        this.thinkTime = thinkTime;
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

    public PerformanceThresholds getThresholds() {
        return thresholds;
    }

    public void setThresholds(PerformanceThresholds thresholds) {
        this.thresholds = thresholds;
    }

    @Override
    public String toString() {
        return String.format("StressTestConfig{testName='%s', initialUsers=%d, maxUsers=%d, rampUpTime=%d, duration=%d}",
                testName, initialUsers, maxUsers, rampUpTime, duration);
    }

    public static class PerformanceThresholds {
        private long maxResponseTime = 5000; // 5 seconds
        private double maxErrorRate = 0.05; // 5%
        private double minThroughput = 10.0; // requests per second
        private long maxMemoryUsage = 1000; // MB
        private double maxCpuUsage = 80.0; // percentage

        public long getMaxResponseTime() {
            return maxResponseTime;
        }

        public void setMaxResponseTime(long maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
        }

        public double getMaxErrorRate() {
            return maxErrorRate;
        }

        public void setMaxErrorRate(double maxErrorRate) {
            this.maxErrorRate = maxErrorRate;
        }

        public double getMinThroughput() {
            return minThroughput;
        }

        public void setMinThroughput(double minThroughput) {
            this.minThroughput = minThroughput;
        }

        public long getMaxMemoryUsage() {
            return maxMemoryUsage;
        }

        public void setMaxMemoryUsage(long maxMemoryUsage) {
            this.maxMemoryUsage = maxMemoryUsage;
        }

        public double getMaxCpuUsage() {
            return maxCpuUsage;
        }

        public void setMaxCpuUsage(double maxCpuUsage) {
            this.maxCpuUsage = maxCpuUsage;
        }
    }
}
