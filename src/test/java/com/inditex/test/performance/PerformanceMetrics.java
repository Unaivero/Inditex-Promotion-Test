package com.inditex.test.performance;

import java.util.Map;
import java.util.HashMap;

public class PerformanceMetrics {
    private long totalSamples;
    private long successfulSamples;
    private long failedSamples;
    private double errorRate;
    private double averageResponseTime;
    private double minResponseTime;
    private double maxResponseTime;
    private double p50ResponseTime;
    private double p90ResponseTime;
    private double p95ResponseTime;
    private double p99ResponseTime;
    private double throughput; // requests per second
    private long totalBytes;
    private double averageBytes;
    private Map<String, Integer> httpResponseCodes;
    private Map<String, Double> requestMetrics; // per request type metrics

    public PerformanceMetrics() {
        this.httpResponseCodes = new HashMap<>();
        this.requestMetrics = new HashMap<>();
        this.minResponseTime = Double.MAX_VALUE;
        this.maxResponseTime = 0.0;
    }

    // Getters and setters
    public long getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(long totalSamples) {
        this.totalSamples = totalSamples;
    }

    public long getSuccessfulSamples() {
        return successfulSamples;
    }

    public void setSuccessfulSamples(long successfulSamples) {
        this.successfulSamples = successfulSamples;
    }

    public long getFailedSamples() {
        return failedSamples;
    }

    public void setFailedSamples(long failedSamples) {
        this.failedSamples = failedSamples;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public void calculateErrorRate() {
        if (totalSamples > 0) {
            this.errorRate = (double) failedSamples / totalSamples;
        } else {
            this.errorRate = 0.0;
        }
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public double getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(double minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public double getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(double maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public double getP50ResponseTime() {
        return p50ResponseTime;
    }

    public void setP50ResponseTime(double p50ResponseTime) {
        this.p50ResponseTime = p50ResponseTime;
    }

    public double getP90ResponseTime() {
        return p90ResponseTime;
    }

    public void setP90ResponseTime(double p90ResponseTime) {
        this.p90ResponseTime = p90ResponseTime;
    }

    public double getP95ResponseTime() {
        return p95ResponseTime;
    }

    public void setP95ResponseTime(double p95ResponseTime) {
        this.p95ResponseTime = p95ResponseTime;
    }

    public double getP99ResponseTime() {
        return p99ResponseTime;
    }

    public void setP99ResponseTime(double p99ResponseTime) {
        this.p99ResponseTime = p99ResponseTime;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public double getAverageBytes() {
        return averageBytes;
    }

    public void setAverageBytes(double averageBytes) {
        this.averageBytes = averageBytes;
    }

    public void calculateAverageBytes() {
        if (totalSamples > 0) {
            this.averageBytes = (double) totalBytes / totalSamples;
        } else {
            this.averageBytes = 0.0;
        }
    }

    public Map<String, Integer> getHttpResponseCodes() {
        return httpResponseCodes;
    }

    public void setHttpResponseCodes(Map<String, Integer> httpResponseCodes) {
        this.httpResponseCodes = httpResponseCodes;
    }

    public void addHttpResponseCode(String code) {
        httpResponseCodes.merge(code, 1, Integer::sum);
    }

    public Map<String, Double> getRequestMetrics() {
        return requestMetrics;
    }

    public void setRequestMetrics(Map<String, Double> requestMetrics) {
        this.requestMetrics = requestMetrics;
    }

    public void addRequestMetric(String requestName, double responseTime) {
        requestMetrics.put(requestName, responseTime);
    }

    // Helper methods for common calculations
    public double getSuccessRate() {
        return 1.0 - errorRate;
    }

    public boolean isHealthy() {
        return errorRate < 0.05 && // Less than 5% error rate
               averageResponseTime < 5000 && // Less than 5 seconds average response time
               throughput > 1.0; // At least 1 request per second
    }

    public String getPerformanceGrade() {
        if (errorRate > 0.1) return "F"; // More than 10% errors
        if (averageResponseTime > 10000) return "F"; // More than 10 seconds average
        if (averageResponseTime > 5000) return "D"; // More than 5 seconds
        if (averageResponseTime > 3000) return "C"; // More than 3 seconds
        if (averageResponseTime > 1000) return "B"; // More than 1 second
        return "A"; // Less than 1 second average response time
    }

    @Override
    public String toString() {
        return String.format("PerformanceMetrics{" +
                        "totalSamples=%d, " +
                        "errorRate=%.2f%%, " +
                        "avgResponseTime=%.2fms, " +
                        "throughput=%.2f req/s, " +
                        "grade=%s" +
                        "}",
                totalSamples,
                errorRate * 100,
                averageResponseTime,
                throughput,
                getPerformanceGrade());
    }

    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Performance Metrics Report ===\n");
        report.append(String.format("Total Samples: %d\n", totalSamples));
        report.append(String.format("Successful: %d (%.2f%%)\n", successfulSamples, getSuccessRate() * 100));
        report.append(String.format("Failed: %d (%.2f%%)\n", failedSamples, errorRate * 100));
        report.append(String.format("Throughput: %.2f req/s\n", throughput));
        report.append("\n--- Response Times ---\n");
        report.append(String.format("Average: %.2fms\n", averageResponseTime));
        report.append(String.format("Min: %.2fms\n", minResponseTime));
        report.append(String.format("Max: %.2fms\n", maxResponseTime));
        report.append(String.format("50th Percentile: %.2fms\n", p50ResponseTime));
        report.append(String.format("90th Percentile: %.2fms\n", p90ResponseTime));
        report.append(String.format("95th Percentile: %.2fms\n", p95ResponseTime));
        report.append(String.format("99th Percentile: %.2fms\n", p99ResponseTime));
        
        if (!httpResponseCodes.isEmpty()) {
            report.append("\n--- HTTP Response Codes ---\n");
            httpResponseCodes.forEach((code, count) -> 
                report.append(String.format("%s: %d\n", code, count)));
        }
        
        report.append(String.format("\nPerformance Grade: %s\n", getPerformanceGrade()));
        return report.toString();
    }
}
