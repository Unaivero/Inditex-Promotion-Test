package com.inditex.test.performance;

public class PerformanceTestResult {
    private String testName;
    private long startTime;
    private long endTime;
    private long duration;
    private String resultsFile;
    private PerformanceMetrics metrics;
    private boolean passed;
    private String failureReason;

    public PerformanceTestResult() {
        this.passed = true;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getResultsFile() {
        return resultsFile;
    }

    public void setResultsFile(String resultsFile) {
        this.resultsFile = resultsFile;
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(PerformanceMetrics metrics) {
        this.metrics = metrics;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        this.passed = false;
    }

    public void validateThresholds(StressTestConfig.PerformanceThresholds thresholds) {
        if (metrics == null) {
            setFailureReason("No performance metrics available for validation");
            return;
        }

        StringBuilder failures = new StringBuilder();

        // Check response time threshold
        if (metrics.getAverageResponseTime() > thresholds.getMaxResponseTime()) {
            failures.append(String.format("Average response time (%.2fms) exceeds threshold (%dms). ",
                    metrics.getAverageResponseTime(), thresholds.getMaxResponseTime()));
        }

        // Check error rate threshold
        if (metrics.getErrorRate() > thresholds.getMaxErrorRate()) {
            failures.append(String.format("Error rate (%.2f%%) exceeds threshold (%.2f%%). ",
                    metrics.getErrorRate() * 100, thresholds.getMaxErrorRate() * 100));
        }

        // Check throughput threshold
        if (metrics.getThroughput() < thresholds.getMinThroughput()) {
            failures.append(String.format("Throughput (%.2f req/s) below threshold (%.2f req/s). ",
                    metrics.getThroughput(), thresholds.getMinThroughput()));
        }

        if (failures.length() > 0) {
            setFailureReason(failures.toString().trim());
        }
    }

    @Override
    public String toString() {
        return String.format("PerformanceTestResult{testName='%s', duration=%dms, passed=%s, metrics=%s}",
                testName, duration, passed, metrics != null ? metrics.toString() : "null");
    }
}
