package com.inditex.test.security;

import com.inditex.test.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.annotations.Tag;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Comprehensive security testing suite using OWASP ZAP for vulnerability scanning,
 * XSS detection, SQL injection testing, and security compliance validation
 */
public class SecurityTestSuite {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityTestSuite.class);
    
    private ClientApi zapClient;
    private String zapProxyUrl;
    private int zapPort;
    private boolean securityTestingEnabled;
    private final List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
    
    @PostConstruct
    @BeforeClass
    public void initialize() {
        securityTestingEnabled = ConfigManager.getBooleanProperty("security.testing.enabled", false);
        
        if (!securityTestingEnabled) {
            logger.info("Security testing is disabled");
            return;
        }
        
        zapPort = ConfigManager.getIntProperty("zap.proxy.port", 8090);
        zapProxyUrl = "http://localhost:" + zapPort;
        
        initializeZapClient();
        logger.info("Security testing suite initialized with ZAP proxy on port: {}", zapPort);
    }
    
    private void initializeZapClient() {
        try {
            zapClient = new ClientApi("localhost", zapPort);
            
            // Wait for ZAP to start (if needed)
            waitForZapToStart();
            
            // Configure ZAP settings
            configureZapSettings();
            
            logger.info("ZAP client initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize ZAP client", e);
            throw new RuntimeException("ZAP initialization failed", e);
        }
    }
    
    private void waitForZapToStart() {
        int maxAttempts = 30;
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                zapClient.core.version();
                logger.info("ZAP is ready after {} attempts", attempt + 1);
                return;
            } catch (Exception e) {
                attempt++;
                logger.debug("Waiting for ZAP to start, attempt {}/{}", attempt, maxAttempts);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for ZAP", ie);
                }
            }
        }
        
        throw new RuntimeException("ZAP failed to start after " + maxAttempts + " attempts");
    }
    
    private void configureZapSettings() throws ClientApiException {
        // Set ZAP to attack mode
        zapClient.core.setMode("standard");
        
        // Configure session management
        zapClient.core.newSession("", "true");
        
        // Set up custom rules for Inditex domains
        String[] inditexDomains = {
            "*.zara.com", "*.bershka.com", "*.pullandbear.com",
            "*.massimodutti.com", "*.stradivarius.com", "*.oysho.com"
        };
        
        for (String domain : inditexDomains) {
            zapClient.core.includeInContext("InditexContext", domain);
        }
        
        logger.info("ZAP configured for Inditex domains");
    }
    
    @Test
    @Tag("security")
    @Tag("vulnerability")
    public void performComprehensiveSecurityScan() {
        if (!securityTestingEnabled) {
            logger.info("Security scan skipped - security testing disabled");
            return;
        }
        
        String targetUrl = ConfigManager.getProperty("security.target.url", "https://www.zara.com");
        
        try {
            logger.info("Starting comprehensive security scan for: {}", targetUrl);
            
            // Step 1: Spider the target
            performSpiderScan(targetUrl);
            
            // Step 2: Passive scan
            performPassiveScan();
            
            // Step 3: Active scan
            performActiveScan(targetUrl);
            
            // Step 4: Generate and analyze results
            analyzeSecurityResults();
            
            // Step 5: Generate security report
            generateSecurityReport();
            
            logger.info("Comprehensive security scan completed");
            
        } catch (Exception e) {
            logger.error("Security scan failed", e);
            throw new RuntimeException("Security scan failed", e);
        }
    }
    
    private void performSpiderScan(String targetUrl) throws ClientApiException, InterruptedException {
        logger.info("Starting spider scan for: {}", targetUrl);
        
        // Start spider scan
        ApiResponse spiderResponse = zapClient.spider.scan(targetUrl, "", "", "", "");
        String scanId = ((Map<String, String>) spiderResponse).get("scan");
        
        // Wait for spider to complete
        int progress = 0;
        while (progress < 100) {
            Thread.sleep(5000);
            progress = Integer.parseInt(
                ((Map<String, String>) zapClient.spider.status(scanId)).get("status"));
            logger.debug("Spider scan progress: {}%", progress);
        }
        
        // Get spider results
        ApiResponse urls = zapClient.spider.results(scanId);
        int urlCount = ((List<?>) urls).size();
        
        logger.info("Spider scan completed. Found {} URLs", urlCount);
    }
    
    private void performPassiveScan() throws InterruptedException {
        logger.info("Starting passive scan");
        
        // Wait for passive scan to complete
        int recordsToScan;
        do {
            Thread.sleep(2000);
            recordsToScan = Integer.parseInt(
                ((Map<String, String>) zapClient.pscan.recordsToScan()).get("recordsToScan"));
            logger.debug("Passive scan records remaining: {}", recordsToScan);
        } while (recordsToScan > 0);
        
        logger.info("Passive scan completed");
    }
    
    private void performActiveScan(String targetUrl) throws ClientApiException, InterruptedException {
        logger.info("Starting active scan for: {}", targetUrl);
        
        // Start active scan
        ApiResponse activeScanResponse = zapClient.ascan.scan(targetUrl, "true", "false", "", "", "", "");
        String scanId = ((Map<String, String>) activeScanResponse).get("scan");
        
        // Wait for active scan to complete
        int progress = 0;
        while (progress < 100) {
            Thread.sleep(10000); // Active scan takes longer
            progress = Integer.parseInt(
                ((Map<String, String>) zapClient.ascan.status(scanId)).get("status"));
            logger.debug("Active scan progress: {}%", progress);
        }
        
        logger.info("Active scan completed");
    }
    
    private void analyzeSecurityResults() throws ClientApiException {
        logger.info("Analyzing security scan results");
        
        // Get all alerts
        ApiResponse alertsResponse = zapClient.core.alerts("", "", "");
        List<Map<String, String>> alerts = (List<Map<String, String>>) alertsResponse;
        
        Map<String, Integer> riskCounts = new HashMap<>();
        riskCounts.put("High", 0);
        riskCounts.put("Medium", 0);
        riskCounts.put("Low", 0);
        riskCounts.put("Informational", 0);
        
        for (Map<String, String> alert : alerts) {
            String risk = alert.get("risk");
            String name = alert.get("name");
            String description = alert.get("description");
            String url = alert.get("url");
            String solution = alert.get("solution");
            
            SecurityVulnerability vulnerability = new SecurityVulnerability(
                name, risk, description, url, solution);
            vulnerabilities.add(vulnerability);
            
            riskCounts.put(risk, riskCounts.get(risk) + 1);
        }
        
        logger.info("Security analysis completed. Vulnerabilities found - High: {}, Medium: {}, Low: {}, Info: {}",
            riskCounts.get("High"), riskCounts.get("Medium"), 
            riskCounts.get("Low"), riskCounts.get("Informational"));
        
        // Fail test if high-risk vulnerabilities found
        if (riskCounts.get("High") > 0) {
            throw new SecurityException("High-risk security vulnerabilities found: " + riskCounts.get("High"));
        }
    }
    
    private void generateSecurityReport() {
        try {
            String reportPath = "target/security-reports/security-report-" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".html";
            
            File reportFile = new File(reportPath);
            reportFile.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write(generateHtmlSecurityReport());
            }
            
            logger.info("Security report generated: {}", reportFile.getAbsolutePath());
            
        } catch (IOException e) {
            logger.error("Failed to generate security report", e);
        }
    }
    
    private String generateHtmlSecurityReport() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>Inditex Promotions Security Test Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f2f2f2; }\n");
        html.append(".high { color: red; font-weight: bold; }\n");
        html.append(".medium { color: orange; font-weight: bold; }\n");
        html.append(".low { color: blue; }\n");
        html.append(".info { color: gray; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<h1>Security Test Report</h1>\n");
        html.append("<p>Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</p>\n");
        
        // Summary
        html.append("<h2>Summary</h2>\n");
        html.append("<p>Total vulnerabilities found: ").append(vulnerabilities.size()).append("</p>\n");
        
        html.append("</body>\n</html>\n");
        
        return html.toString();
    }
    
    // Data classes
    public static class SecurityVulnerability {
        private final String name;
        private final String risk;
        private final String description;
        private final String url;
        private final String solution;
        
        public SecurityVulnerability(String name, String risk, String description, String url, String solution) {
            this.name = name;
            this.risk = risk;
            this.description = description;
            this.url = url;
            this.solution = solution;
        }
        
        public String getName() { return name; }
        public String getRisk() { return risk; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }
        public String getSolution() { return solution; }
    }
}
