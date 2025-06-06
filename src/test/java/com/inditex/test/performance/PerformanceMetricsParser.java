package com.inditex.test.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerformanceMetricsParser {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsParser.class);

    public PerformanceMetrics parseJTLFile(String filePath) throws Exception {
        logger.info("Parsing JTL file: {}", filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("JTL file not found: " + filePath);
        }

        PerformanceMetrics metrics = new PerformanceMetrics();
        
        if (file.length() == 0) {
            logger.warn("JTL file is empty: {}", filePath);
            return metrics;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            NodeList samples = document.getElementsByTagName("httpSample");
            if (samples.getLength() == 0) {
                // Try alternative tag name
                samples = document.getElementsByTagName("sample");
            }

            if (samples.getLength() == 0) {
                logger.warn("No samples found in JTL file: {}", filePath);
                return metrics;
            }

            List<Double> responseTimes = new ArrayList<>();
            long totalSamples = samples.getLength();
            long successfulSamples = 0;
            long failedSamples = 0;
            long totalBytes = 0;
            double totalResponseTime = 0;
            double minResponseTime = Double.MAX_VALUE;
            double maxResponseTime = 0;
            long testStartTime = Long.MAX_VALUE;
            long testEndTime = 0;

            for (int i = 0; i < samples.getLength(); i++) {
                Element sample = (Element) samples.item(i);
                
                // Parse sample attributes
                long timestamp = Long.parseLong(sample.getAttribute("ts"));
                double responseTime = Double.parseDouble(sample.getAttribute("t"));
                boolean success = Boolean.parseBoolean(sample.getAttribute("s"));
                String responseCode = sample.getAttribute("rc");
                long bytes = sample.hasAttribute("by") ? Long.parseLong(sample.getAttribute("by")) : 0;
                
                // Update metrics
                responseTimes.add(responseTime);
                totalResponseTime += responseTime;
                totalBytes += bytes;
                
                if (success) {
                    successfulSamples++;
                } else {
                    failedSamples++;
                }
                
                // Track response codes
                if (!responseCode.isEmpty()) {
                    metrics.addHttpResponseCode(responseCode);
                }
                
                // Update min/max response times
                if (responseTime < minResponseTime) {
                    minResponseTime = responseTime;
                }
                if (responseTime > maxResponseTime) {
                    maxResponseTime = responseTime;
                }
                
                // Track test duration
                if (timestamp < testStartTime) {
                    testStartTime = timestamp;
                }
                if (timestamp > testEndTime) {
                    testEndTime = timestamp;
                }
            }

            // Set basic metrics
            metrics.setTotalSamples(totalSamples);
            metrics.setSuccessfulSamples(successfulSamples);
            metrics.setFailedSamples(failedSamples);
            metrics.calculateErrorRate();
            
            // Set response time metrics
            metrics.setAverageResponseTime(totalResponseTime / totalSamples);
            metrics.setMinResponseTime(minResponseTime == Double.MAX_VALUE ? 0 : minResponseTime);
            metrics.setMaxResponseTime(maxResponseTime);
            
            // Calculate percentiles
            Collections.sort(responseTimes);
            metrics.setP50ResponseTime(calculatePercentile(responseTimes, 50));
            metrics.setP90ResponseTime(calculatePercentile(responseTimes, 90));
            metrics.setP95ResponseTime(calculatePercentile(responseTimes, 95));
            metrics.setP99ResponseTime(calculatePercentile(responseTimes, 99));
            
            // Calculate throughput (requests per second)
            long testDuration = testEndTime - testStartTime;
            if (testDuration > 0) {
                double throughput = (double) totalSamples / (testDuration / 1000.0);
                metrics.setThroughput(throughput);
            }
            
            // Set bytes metrics
            metrics.setTotalBytes(totalBytes);
            metrics.calculateAverageBytes();
            
            logger.info("Parsed {} samples from JTL file", totalSamples);
            
        } catch (Exception e) {
            logger.error("Failed to parse JTL file: {}", filePath, e);
            throw new RuntimeException("JTL parsing failed", e);
        }

        return metrics;
    }

    private double calculatePercentile(List<Double> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0.0;
        }
        
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }
        
        if (percentile == 0) {
            return sortedValues.get(0);
        }
        
        if (percentile == 100) {
            return sortedValues.get(sortedValues.size() - 1);
        }
        
        double index = (percentile / 100.0) * (sortedValues.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }
        
        double lowerValue = sortedValues.get(lowerIndex);
        double upperValue = sortedValues.get(upperIndex);
        double weight = index - lowerIndex;
        
        return lowerValue + (weight * (upperValue - lowerValue));
    }

    public PerformanceMetrics parseCSVFile(String filePath) throws Exception {
        logger.info("Parsing CSV JTL file: {}", filePath);
        
        // Implementation for CSV format JTL files
        // This would parse comma-separated values format
        // For now, we'll delegate to XML parser or implement CSV parsing if needed
        
        throw new UnsupportedOperationException("CSV JTL parsing not yet implemented. Use XML format.");
    }
}
