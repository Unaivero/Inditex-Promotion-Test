package com.inditex.test.performance;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.utils.TestDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Comprehensive performance testing manager for the InditexPromotionsTest framework.
 * Handles execution, monitoring, and reporting of various performance test scenarios.
 */
public class PerformanceTestManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestManager.class);
    
    private static final String JMETER_HOME = ConfigManager.getProperty("jmeter.home", "/opt/jmeter");
    private static final String JMETER_PLANS_DIR = "src/test/resources/jmeter";
    private static final String RESULTS_DIR = "target/performance-results";
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Map<String, PerformanceTestResult> testResults = new ConcurrentHashMap<>();
    
    /**
     * Executes all performance test scenarios
     */
    public void executeAllPerformanceTests() {
        logger.info("Starting comprehensive performance testing suite");
        
        try {
            // Initialize test environment
            initializeTestEnvironment();
            
            // Generate JMeter test plans
            generateTestPlans();
            
            // Execute different types of performance tests
            List<Future<PerformanceTestResult>> futures = new ArrayList<>();
            
            futures.add(executorService.submit(this::executeLoadTest));
            futures.add(executorService.submit(this::executeStressTest));
            futures.add(executorService.submit(this::executeEnduranceTest));
            futures.add(executorService.submit(this::executeSpikeTest));
            
            // Wait for all tests to complete
            for (Future<PerformanceTestResult> future : futures) {
                try {
                    PerformanceTestResult result = future.get(30, TimeUnit.MINUTES);
                    testResults.put(result.testName, result);
                } catch (Exception e) {
                    logger.error("Performance test execution failed", e);
                }
            }
            
            // Generate comprehensive report
            generatePerformanceReport();
            
            logger.info("Performance testing suite completed");
            
        } catch (Exception e) {
            logger.error("Performance testing suite failed", e);
            throw new RuntimeException("Performance testing failed", e);
        } finally {
            executorService.shutdown();
        }
    }
    
    /**
     * Executes load testing with 1000+ concurrent users
     */
    private PerformanceTestResult executeLoadTest() {
        logger.info("Executing load test with 1000+ concurrent users");
        
        try {
            String testPlan = "promotional_load_test.jmx";
            String resultFile = "load_test_results.jtl";
            
            PerformanceTestResult result = executeJMeterTest(testPlan, resultFile, "Load Test");
            
            // Validate load test thresholds
            validateLoadTestResults(result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Load test execution failed", e);
            return new PerformanceTestResult("Load Test", false, e.getMessage(), null);
        }
    }
    
    /**
     * Executes stress testing to find failure points
     */
    private PerformanceTestResult executeStressTest() {
        logger.info("Executing stress test to find failure points");
        
        try {
            String testPlan = "promotional_stress_test.jmx";
            String resultFile = "stress_test_results.jtl";
            
            PerformanceTestResult result = executeJMeterTest(testPlan, resultFile, "Stress Test");
            
            // Analyze stress test results for failure points
            analyzeStressTestResults(result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Stress test execution failed", e);
            return new PerformanceTestResult("Stress Test", false, e.getMessage(), null);
        }
    }
    
    /**
     * Executes endurance testing for extended periods
     */
    private PerformanceTestResult executeEnduranceTest() {
        logger.info("Executing endurance test for extended periods");
        
        try {
            String testPlan = "promotional_endurance_test.jmx";
            String resultFile = "endurance_test_results.jtl";
            
            PerformanceTestResult result = executeJMeterTest(testPlan, resultFile, "Endurance Test");
            
            // Check for memory leaks and performance degradation
            analyzeEnduranceTestResults(result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Endurance test execution failed", e);
            return new PerformanceTestResult("Endurance Test", false, e.getMessage(), null);
        }
    }
    
    /**
     * Executes spike testing for traffic bursts
     */
    private PerformanceTestResult executeSpikeTest() {
        logger.info("Executing spike test for traffic bursts");
        
        try {
            String testPlan = "promotional_spike_test.jmx";
            String resultFile = "spike_test_results.jtl";
            
            PerformanceTestResult result = executeJMeterTest(testPlan, resultFile, "Spike Test");
            
            // Analyze spike recovery and system stability
            analyzeSpikeTestResults(result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Spike test execution failed", e);
            return new PerformanceTestResult("Spike Test", false, e.getMessage(), null);
        }
    }
    
    /**
     * Executes JMeter test plan and returns results
     */
    private PerformanceTestResult executeJMeterTest(String testPlan, String resultFile, String testName) throws Exception {
        logger.info("Executing JMeter test: {}", testPlan);
        
        File testPlanFile = new File(JMETER_PLANS_DIR, testPlan);
        File resultFileObj = new File(RESULTS_DIR, resultFile);
        
        if (!testPlanFile.exists()) {
            throw new RuntimeException("Test plan not found: " + testPlanFile.getAbsolutePath());
        }
        
        // Ensure results directory exists
        resultFileObj.getParentFile().mkdirs();
        
        // Build JMeter command
        List<String> command = Arrays.asList(
            JMETER_HOME + "/bin/jmeter",
            "-n", // non-GUI mode
            "-t", testPlanFile.getAbsolutePath(), // test plan
            "-l", resultFileObj.getAbsolutePath(), // results log
            "-j", new File(RESULTS_DIR, testName.toLowerCase().replace(" ", "_") + ".log").getAbsolutePath(), // JMeter log
            "-Jjmeter.reportgenerator.overall_granularity=60000", // report granularity
            "-e", // generate report
            "-o", new File(RESULTS_DIR, testName.toLowerCase().replace(" ", "_") + "_report").getAbsolutePath() // report output
        );
        
        long startTime = System.currentTimeMillis();
        
        // Execute JMeter
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Capture output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug("JMeter: {}", line);
            }
        }
        
        // Wait for completion
        boolean finished = process.waitFor(30, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("JMeter test timed out after 30 minutes");
        }
        
        long duration = System.currentTimeMillis() - startTime;
        int exitCode = process.exitValue();
        
        if (exitCode != 0) {
            throw new RuntimeException("JMeter test failed with exit code: " + exitCode + "\nOutput: " + output.toString());
        }
        
        // Parse results
        PerformanceMetrics metrics = parseJMeterResults(resultFileObj);
        
        logger.info("JMeter test completed: {} in {} ms", testPlan, duration);
        
        return new PerformanceTestResult(testName, true, "Test completed successfully", metrics);
    }
    
    /**
     * Parses JMeter results file and extracts performance metrics
     */
    private PerformanceMetrics parseJMeterResults(File resultFile) throws IOException {
        logger.debug("Parsing JMeter results from: {}", resultFile.getAbsolutePath());
        
        int totalSamples = 0;
        int errorSamples = 0;
        long totalResponseTime = 0;
        long minResponseTime = Long.MAX_VALUE;
        long maxResponseTime = Long.MIN_VALUE;
        List<Long> responseTimes = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(resultFile)) {
            // Skip header line if present
            if (scanner.hasNextLine()) {
                String header = scanner.nextLine();
                if (!header.startsWith("timestamp")) {
                    // First line is data, process it
                    processJMeterResultLine(header, responseTimes);
                    totalSamples++;
                }
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        long responseTime = Long.parseLong(parts[1]); // elapsed time
                        boolean success = "true".equals(parts[7]); // success flag
                        
                        totalSamples++;
                        totalResponseTime += responseTime;
                        responseTimes.add(responseTime);
                        
                        if (!success) {
                            errorSamples++;
                        }
                        
                        minResponseTime = Math.min(minResponseTime, responseTime);
                        maxResponseTime = Math.max(maxResponseTime, responseTime);
                        
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid response time in results: {}", line);
                    }
                }
            }
        }
        
        if (totalSamples == 0) {
            logger.warn("No valid samples found in results file");
            return new PerformanceMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        
        // Calculate metrics
        double averageResponseTime = (double) totalResponseTime / totalSamples;
        double errorPercentage = (double) errorSamples / totalSamples * 100;
        
        // Calculate percentiles
        Collections.sort(responseTimes);
        long p90ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.9));
        long p95ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.99));
        
        return new PerformanceMetrics(
            totalSamples, errorSamples, errorPercentage, averageResponseTime,
            minResponseTime, maxResponseTime, p90ResponseTime, p95ResponseTime, p99ResponseTime
        );
    }
    
    private void processJMeterResultLine(String line, List<Long> responseTimes) {
        // Process first line if it contains data
        String[] parts = line.split(",");
        if (parts.length >= 3) {
            try {
                long responseTime = Long.parseLong(parts[1]);
                responseTimes.add(responseTime);
            } catch (NumberFormatException e) {
                // This was actually a header line, ignore
            }
        }
    }
    
    /**
     * Validates load test results against performance thresholds
     */
    private void validateLoadTestResults(PerformanceTestResult result) {
        if (result.metrics == null) return;
        
        PerformanceMetrics metrics = result.metrics;
        
        // Define performance thresholds
        double maxErrorPercentage = 1.0; // 1% max error rate
        double maxAverageResponseTime = 2000; // 2 seconds max average response time
        long maxP95ResponseTime = 5000; // 5 seconds max 95th percentile
        
        List<String> violations = new ArrayList<>();
        
        if (metrics.errorPercentage > maxErrorPercentage) {
            violations.add(String.format("Error rate %.2f%% exceeds threshold %.2f%%", 
                          metrics.errorPercentage, maxErrorPercentage));
        }
        
        if (metrics.averageResponseTime > maxAverageResponseTime) {
            violations.add(String.format("Average response time %.2fms exceeds threshold %.2fms", 
                          metrics.averageResponseTime, maxAverageResponseTime));
        }
        
        if (metrics.p95ResponseTime > maxP95ResponseTime) {
            violations.add(String.format("95th percentile response time %dms exceeds threshold %dms", 
                          metrics.p95ResponseTime, maxP95ResponseTime));
        }
        
        if (!violations.isEmpty()) {
            logger.warn("Load test performance thresholds violated:");
            violations.forEach(logger::warn);
            result.addWarnings(violations);
        } else {
            logger.info("Load test passed all performance thresholds");
        }
    }
    
    /**
     * Analyzes stress test results to identify failure points
     */
    private void analyzeStressTestResults(PerformanceTestResult result) {
        if (result.metrics == null) return;
        
        PerformanceMetrics metrics = result.metrics;
        
        // Analyze stress test patterns
        List<String> findings = new ArrayList<>();
        
        if (metrics.errorPercentage > 10) {
            findings.add("High error rate (" + String.format("%.2f", metrics.errorPercentage) + 
                        "%) indicates system stress point reached");
        }
        
        if (metrics.maxResponseTime > 30000) {
            findings.add("Maximum response time (" + metrics.maxResponseTime + 
                        "ms) indicates potential timeout issues");
        }
        
        if (metrics.p99ResponseTime > 20000) {
            findings.add("99th percentile response time (" + metrics.p99ResponseTime + 
                        "ms) shows tail latency problems under stress");
        }
        
        logger.info("Stress test analysis findings:");
        findings.forEach(logger::info);
        result.addFindings(findings);
    }
    
    /**
     * Analyzes endurance test results for memory leaks and degradation
     */
    private void analyzeEnduranceTestResults(PerformanceTestResult result) {
        if (result.metrics == null) return;
        
        // For endurance tests, we'd typically analyze trends over time
        // This would require time-series data from JMeter results
        
        List<String> findings = new ArrayList<>();
        
        if (result.metrics.errorPercentage > 2) {
            findings.add("Endurance test shows elevated error rate, possible resource exhaustion");
        }
        
        if (result.metrics.averageResponseTime > 3000) {
            findings.add("Response times during endurance test suggest performance degradation");
        }
        
        logger.info("Endurance test analysis findings:");
        findings.forEach(logger::info);
        result.addFindings(findings);
    }
    
    /**
     * Analyzes spike test results for recovery and stability
     */
    private void analyzeSpikeTestResults(PerformanceTestResult result) {
        if (result.metrics == null) return;
        
        List<String> findings = new ArrayList<>();
        
        if (result.metrics.errorPercentage > 5) {
            findings.add("Spike test shows poor error handling during traffic bursts");
        }
        
        if (result.metrics.maxResponseTime > 15000) {
            findings.add("Spike test shows slow recovery from traffic bursts");
        }
        
        logger.info("Spike test analysis findings:");
        findings.forEach(logger::info);
        result.addFindings(findings);
    }
    
    /**
     * Initializes the test environment
     */
    private void initializeTestEnvironment() throws IOException {
        logger.info("Initializing performance test environment");
        
        // Create results directory
        File resultsDir = new File(RESULTS_DIR);
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }
        
        // Validate JMeter installation
        validateJMeterInstallation();
        
        // Initialize test data
        TestDataManager.initialize();
        
        logger.info("Performance test environment initialized");
    }
    
    /**
     * Validates JMeter installation
     */
    private void validateJMeterInstallation() {
        File jmeterBin = new File(JMETER_HOME + "/bin/jmeter");
        if (!jmeterBin.exists()) {
            logger.warn("JMeter not found at {}. Performance tests will be skipped.", JMETER_HOME);
            logger.info("To run performance tests, install JMeter and set jmeter.home property");
            throw new RuntimeException("JMeter installation not found");
        }
        
        logger.info("JMeter installation validated at: {}", JMETER_HOME);
    }
    
    /**
     * Generates JMeter test plans
     */
    private void generateTestPlans() {
        logger.info("Generating JMeter test plans");
        JMeterTestPlanGenerator.generateAllTestPlans();
        logger.info("JMeter test plans generated successfully");
    }
    
    /**
     * Generates comprehensive performance report
     */
    private void generatePerformanceReport() throws IOException {
        logger.info("Generating comprehensive performance report");
        
        File reportFile = new File(RESULTS_DIR, "performance_summary_report.html");
        
        try (java.io.FileWriter writer = new java.io.FileWriter(reportFile)) {
            writer.write(generateHtmlReport());
        }
        
        logger.info("Performance report generated: {}", reportFile.getAbsolutePath());
    }
    
    /**
     * Generates HTML performance report
     */
    private String generateHtmlReport() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>Inditex Promotions Performance Test Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f2f2f2; }\n");
        html.append(".success { color: green; }\n");
        html.append(".failure { color: red; }\n");
        html.append(".warning { color: orange; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<h1>Inditex Promotions Performance Test Report</h1>\n");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</p>\n");
        
        // Executive Summary
        html.append("<h2>Executive Summary</h2>\n");
        html.append("<p>This report summarizes the performance testing results for the Inditex Promotions system.</p>\n");
        
        // Test Results Summary
        html.append("<h2>Test Results Summary</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>Test Name</th><th>Status</th><th>Total Samples</th><th>Error Rate</th><th>Avg Response Time</th><th>95th Percentile</th></tr>\n");
        
        for (PerformanceTestResult result : testResults.values()) {
            html.append("<tr>\n");
            html.append("<td>").append(result.testName).append("</td>\n");
            html.append("<td class=\"").append(result.success ? "success" : "failure").append("\">")
                .append(result.success ? "PASS" : "FAIL").append("</td>\n");
            
            if (result.metrics != null) {
                html.append("<td>").append(result.metrics.totalSamples).append("</td>\n");
                html.append("<td>").append(String.format("%.2f%%", result.metrics.errorPercentage)).append("</td>\n");
                html.append("<td>").append(String.format("%.2f ms", result.metrics.averageResponseTime)).append("</td>\n");
                html.append("<td>").append(result.metrics.p95ResponseTime).append(" ms</td>\n");
            } else {
                html.append("<td colspan=\"4\">No metrics available</td>\n");
            }
            
            html.append("</tr>\n");
        }
        
        html.append("</table>\n");
        
        // Detailed Results
        html.append("<h2>Detailed Results</h2>\n");
        
        for (PerformanceTestResult result : testResults.values()) {
            html.append("<h3>").append(result.testName).append("</h3>\n");
            html.append("<p><strong>Status:</strong> ").append(result.success ? "PASS" : "FAIL").append("</p>\n");
            html.append("<p><strong>Message:</strong> ").append(result.message).append("</p>\n");
            
            if (result.metrics != null) {
                html.append("<table>\n");
                html.append("<tr><th>Metric</th><th>Value</th></tr>\n");
                html.append("<tr><td>Total Samples</td><td>").append(result.metrics.totalSamples).append("</td></tr>\n");
                html.append("<tr><td>Error Samples</td><td>").append(result.metrics.errorSamples).append("</td></tr>\n");
                html.append("<tr><td>Error Percentage</td><td>").append(String.format("%.2f%%", result.metrics.errorPercentage)).append("</td></tr>\n");
                html.append("<tr><td>Average Response Time</td><td>").append(String.format("%.2f ms", result.metrics.averageResponseTime)).append("</td></tr>\n");
                html.append("<tr><td>Min Response Time</td><td>").append(result.metrics.minResponseTime).append(" ms</td></tr>\n");
                html.append("<tr><td>Max Response Time</td><td>").append(result.metrics.maxResponseTime).append(" ms</td></tr>\n");
                html.append("<tr><td>90th Percentile</td><td>").append(result.metrics.p90ResponseTime).append(" ms</td></tr>\n");
                html.append("<tr><td>95th Percentile</td><td>").append(result.metrics.p95ResponseTime).append(" ms</td></tr>\n");
                html.append("<tr><td>99th Percentile</td><td>").append(result.metrics.p99ResponseTime).append(" ms</td></tr>\n");
                html.append("</table>\n");
            }
            
            if (result.warnings != null && !result.warnings.isEmpty()) {
                html.append("<h4>Warnings</h4>\n<ul>\n");
                for (String warning : result.warnings) {
                    html.append("<li class=\"warning\">").append(warning).append("</li>\n");
                }
                html.append("</ul>\n");
            }
            
            if (result.findings != null && !result.findings.isEmpty()) {
                html.append("<h4>Analysis Findings</h4>\n<ul>\n");
                for (String finding : result.findings) {
                    html.append("<li>").append(finding).append("</li>\n");
                }
                html.append("</ul>\n");
            }
        }
        
        html.append("</body>\n</html>\n");
        
        return html.toString();
    }
    
    // Data classes
    
    public static class PerformanceTestResult {
        final String testName;
        final boolean success;
        final String message;
        final PerformanceMetrics metrics;
        List<String> warnings;
        List<String> findings;
        
        public PerformanceTestResult(String testName, boolean success, String message, PerformanceMetrics metrics) {
            this.testName = testName;
            this.success = success;
            this.message = message;
            this.metrics = metrics;
        }
        
        public void addWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
        
        public void addFindings(List<String> findings) {
            this.findings = findings;
        }
    }
    
    public static class PerformanceMetrics {
        final int totalSamples;
        final int errorSamples;
        final double errorPercentage;
        final double averageResponseTime;
        final long minResponseTime;
        final long maxResponseTime;
        final long p90ResponseTime;
        final long p95ResponseTime;
        final long p99ResponseTime;
        
        public PerformanceMetrics(int totalSamples, int errorSamples, double errorPercentage,
                                double averageResponseTime, long minResponseTime, long maxResponseTime,
                                long p90ResponseTime, long p95ResponseTime, long p99ResponseTime) {
            this.totalSamples = totalSamples;
            this.errorSamples = errorSamples;
            this.errorPercentage = errorPercentage;
            this.averageResponseTime = averageResponseTime;
            this.minResponseTime = minResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.p90ResponseTime = p90ResponseTime;
            this.p95ResponseTime = p95ResponseTime;
            this.p99ResponseTime = p99ResponseTime;
        }
    }
}