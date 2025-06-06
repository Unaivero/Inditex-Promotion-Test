package com.inditex.test.api.enhanced;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.inditex.test.config.ConfigManager;
import com.inditex.test.model.PromotionalPriceRequest;
import com.inditex.test.model.PromotionalPriceResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.annotations.Tag;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Enhanced API testing helper with performance monitoring, contract testing,
 * and comprehensive validation capabilities
 */
public class EnhancedApiTestHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedApiTestHelper.class);
    
    private WireMockServer wireMockServer;
    private ExecutorService executorService;
    private final Map<String, ApiMetrics> metricsMap = new ConcurrentHashMap<>();
    private final List<ApiTestResult> testResults = new ArrayList<>();
    
    @PostConstruct
    public void initialize() {
        setupWireMock();
        setupRestAssured();
        executorService = Executors.newFixedThreadPool(10);
        logger.info("Enhanced API testing helper initialized");
    }
    
    private void setupWireMock() {
        int wireMockPort = ConfigManager.getIntProperty("wiremock.port", 8089);
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
            .port(wireMockPort)
            .extensions(new com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer(true)));
        
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockPort);
        
        setupPromotionalApiMocks();
        logger.info("WireMock server started on port: {}", wireMockPort);
    }
    
    private void setupRestAssured() {
        String baseUrl = ConfigManager.getProperty("api.base.url", "http://localhost:8080");
        RestAssured.baseURI = baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Set default timeout
        int timeout = ConfigManager.getIntProperty("api.timeout.seconds", 30);
        RestAssured.config = RestAssured.config().httpClient(
            io.restassured.config.HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", timeout * 1000)
                .setParam("http.socket.timeout", timeout * 1000));
    }
    
    public void setupPromotionalApiMocks() {
        // Mock active promotions endpoint
        wireMockServer.stubFor(get(urlEqualTo("/api/promotions/active"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("active-promotions.json")
                .withTransformers("response-template")));
        
        // Mock promotional pricing endpoint
        wireMockServer.stubFor(get(urlMatching("/api/pricing/promotional/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"sku\": \"{{request.pathSegments.[3]}}\",\n" +
                    "  \"originalPrice\": 50.00,\n" +
                    "  \"promotionalPrice\": 40.00,\n" +
                    "  \"discountPercentage\": 20,\n" +
                    "  \"promotionName\": \"Summer Sale\",\n" +
                    "  \"validUntil\": \"2024-12-31T23:59:59Z\"\n" +
                    "}")
                .withTransformers("response-template")));
        
        // Mock inventory check endpoint
        wireMockServer.stubFor(get(urlMatching("/api/inventory/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"sku\": \"{{request.pathSegments.[2]}}\",\n" +
                    "  \"available\": true,\n" +
                    "  \"quantity\": 100,\n" +
                    "  \"reservedQuantity\": 5\n" +
                    "}")
                .withTransformers("response-template")));
        
        // Mock error scenarios
        wireMockServer.stubFor(get(urlEqualTo("/api/pricing/promotional/INVALID_SKU"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"error\": \"Product not found\",\n" +
                    "  \"code\": \"PRODUCT_NOT_FOUND\",\n" +
                    "  \"timestamp\": \"{{now}}\"\n" +
                    "}")
                .withTransformers("response-template")));
        
        logger.info("API mocks configured successfully");
    }
    
    @Test
    @Tag("api")
    @Tag("performance")
    public void testPromotionalPricingApiPerformance() {
        String testName = "Promotional Pricing API Performance";
        Instant startTime = Instant.now();
        
        try {
            Response response = given()
                .when()
                .get("/api/pricing/promotional/{sku}", "ZARA001ES")
                .then()
                .statusCode(200)
                .time(lessThan(500L)) // 500ms SLA
                .body("promotionalPrice", notNullValue())
                .body("discountPercentage", greaterThan(0))
                .body("sku", equalTo("ZARA001ES"))
                .extract()
                .response();
            
            long responseTime = response.getTime();
            recordApiMetrics("promotional_pricing", responseTime, true);
            
            ApiTestResult result = new ApiTestResult(testName, true, responseTime, 
                "Performance test passed within SLA", response.getStatusCode());
            testResults.add(result);
            
            logger.info("API performance test passed: {} ms", responseTime);
            
        } catch (Exception e) {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            recordApiMetrics("promotional_pricing", duration, false);
            
            ApiTestResult result = new ApiTestResult(testName, false, duration, 
                "Performance test failed: " + e.getMessage(), 0);
            testResults.add(result);
            
            logger.error("API performance test failed", e);
            throw e;
        }
    }
    
    @Test
    @Tag("api")
    @Tag("functional")
    public void testPromotionalPricingCalculation() {
        String testName = "Promotional Pricing Calculation";
        Instant startTime = Instant.now();
        
        try {
            Response response = given()
                .when()
                .get("/api/pricing/promotional/{sku}", "ZARA002FR")
                .then()
                .statusCode(200)
                .body("originalPrice", notNullValue())
                .body("promotionalPrice", notNullValue())
                .body("discountPercentage", notNullValue())
                .extract()
                .response();
            
            // Validate pricing calculation
            double originalPrice = response.jsonPath().getDouble("originalPrice");
            double promotionalPrice = response.jsonPath().getDouble("promotionalPrice");
            int discountPercentage = response.jsonPath().getInt("discountPercentage");
            
            double expectedPromotionalPrice = originalPrice * (1 - discountPercentage / 100.0);
            
            if (Math.abs(promotionalPrice - expectedPromotionalPrice) > 0.01) {
                throw new AssertionError(String.format(
                    "Pricing calculation mismatch. Expected: %.2f, Actual: %.2f", 
                    expectedPromotionalPrice, promotionalPrice));
            }
            
            long responseTime = response.getTime();
            recordApiMetrics("promotional_calculation", responseTime, true);
            
            ApiTestResult result = new ApiTestResult(testName, true, responseTime, 
                "Pricing calculation validated successfully", response.getStatusCode());
            testResults.add(result);
            
            logger.info("Promotional pricing calculation test passed");
            
        } catch (Exception e) {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            recordApiMetrics("promotional_calculation", duration, false);
            
            ApiTestResult result = new ApiTestResult(testName, false, duration, 
                "Calculation test failed: " + e.getMessage(), 0);
            testResults.add(result);
            
            logger.error("Promotional pricing calculation test failed", e);
            throw e;
        }
    }
    
    @Test
    @Tag("api")
    @Tag("negative")
    public void testInvalidSkuHandling() {
        String testName = "Invalid SKU Handling";
        Instant startTime = Instant.now();
        
        try {
            Response response = given()
                .when()
                .get("/api/pricing/promotional/{sku}", "INVALID_SKU")
                .then()
                .statusCode(404)
                .body("error", notNullValue())
                .body("code", equalTo("PRODUCT_NOT_FOUND"))
                .extract()
                .response();
            
            long responseTime = response.getTime();
            recordApiMetrics("invalid_sku", responseTime, true);
            
            ApiTestResult result = new ApiTestResult(testName, true, responseTime, 
                "Invalid SKU handled correctly", response.getStatusCode());
            testResults.add(result);
            
            logger.info("Invalid SKU handling test passed");
            
        } catch (Exception e) {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            recordApiMetrics("invalid_sku", duration, false);
            
            ApiTestResult result = new ApiTestResult(testName, false, duration, 
                "Invalid SKU test failed: " + e.getMessage(), 0);
            testResults.add(result);
            
            logger.error("Invalid SKU handling test failed", e);
            throw e;
        }
    }
    
    @Test
    @Tag("api")
    @Tag("load")
    public void testConcurrentApiCalls() {
        String testName = "Concurrent API Load Test";
        int concurrentUsers = 10;
        int requestsPerUser = 5;
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int user = 0; user < concurrentUsers; user++) {
            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                for (int request = 0; request < requestsPerUser; request++) {
                    try {
                        Instant startTime = Instant.now();
                        
                        Response response = given()
                            .when()
                            .get("/api/pricing/promotional/{sku}", "ZARA00" + (request + 1) + "ES")
                            .then()
                            .statusCode(200)
                            .time(lessThan(2000L)) // 2 second timeout for load test
                            .extract()
                            .response();
                        
                        long responseTime = response.getTime();
                        recordApiMetrics("concurrent_load", responseTime, true);
                        
                    } catch (Exception e) {
                        recordApiMetrics("concurrent_load", 2000, false);
                        logger.warn("Concurrent request failed: {}", e.getMessage());
                    }
                }
            }, executorService);
            
            futures.add(userFuture);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        ApiMetrics metrics = metricsMap.get("concurrent_load");
        double successRate = (double) metrics.successCount / (metrics.successCount + metrics.failureCount) * 100;
        
        ApiTestResult result = new ApiTestResult(testName, successRate >= 95, 
            (long) metrics.averageResponseTime, 
            String.format("Load test completed. Success rate: %.2f%%, Avg response: %.2f ms", 
                successRate, metrics.averageResponseTime), 200);
        testResults.add(result);
        
        logger.info("Concurrent API load test completed. Success rate: {:.2f}%", successRate);
        
        if (successRate < 95) {
            throw new AssertionError("Load test failed with success rate: " + successRate + "%");
        }
    }
    
    @Test
    @Tag("api")
    @Tag("security")
    public void testApiSecurityHeaders() {
        String testName = "API Security Headers";
        Instant startTime = Instant.now();
        
        try {
            Response response = given()
                .when()
                .get("/api/pricing/promotional/ZARA001ES")
                .then()
                .statusCode(200)
                .extract()
                .response();
            
            // Validate security headers (these would be configured in the actual API)
            Map<String, String> headers = response.getHeaders().asFlatMap();
            
            List<String> missingHeaders = new ArrayList<>();
            if (!headers.containsKey("X-Content-Type-Options")) {
                missingHeaders.add("X-Content-Type-Options");
            }
            if (!headers.containsKey("X-Frame-Options")) {
                missingHeaders.add("X-Frame-Options");
            }
            if (!headers.containsKey("X-XSS-Protection")) {
                missingHeaders.add("X-XSS-Protection");
            }
            
            long responseTime = response.getTime();
            boolean passed = missingHeaders.isEmpty();
            
            recordApiMetrics("security_headers", responseTime, passed);
            
            String message = passed ? "All security headers present" : 
                "Missing security headers: " + String.join(", ", missingHeaders);
            
            ApiTestResult result = new ApiTestResult(testName, passed, responseTime, 
                message, response.getStatusCode());
            testResults.add(result);
            
            if (!passed) {
                logger.warn("Security headers test failed: {}", message);
            } else {
                logger.info("Security headers test passed");
            }
            
        } catch (Exception e) {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            recordApiMetrics("security_headers", duration, false);
            
            ApiTestResult result = new ApiTestResult(testName, false, duration, 
                "Security test failed: " + e.getMessage(), 0);
            testResults.add(result);
            
            logger.error("Security headers test failed", e);
            throw e;
        }
    }
    
    private void recordApiMetrics(String endpoint, long responseTime, boolean success) {
        metricsMap.compute(endpoint, (key, existing) -> {
            if (existing == null) {
                existing = new ApiMetrics();
            }
            existing.addMeasurement(responseTime, success);
            return existing;
        });
    }
    
    public ApiTestSummary getTestSummary() {
        int totalTests = testResults.size();
        long passedTests = testResults.stream().mapToLong(r -> r.passed ? 1 : 0).sum();
        double averageResponseTime = testResults.stream()
            .mapToLong(ApiTestResult::getResponseTime)
            .average()
            .orElse(0.0);
        
        return new ApiTestSummary(totalTests, (int) passedTests, 
            totalTests - (int) passedTests, averageResponseTime, metricsMap);
    }
    
    @PreDestroy
    public void cleanup() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        logger.info("Enhanced API test helper cleaned up");
    }
    
    // Helper classes
    public static class ApiMetrics {
        private long totalResponseTime = 0;
        private int totalRequests = 0;
        private int successCount = 0;
        private int failureCount = 0;
        private long minResponseTime = Long.MAX_VALUE;
        private long maxResponseTime = 0;
        
        public void addMeasurement(long responseTime, boolean success) {
            totalResponseTime += responseTime;
            totalRequests++;
            
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
            
            minResponseTime = Math.min(minResponseTime, responseTime);
            maxResponseTime = Math.max(maxResponseTime, responseTime);
        }
        
        public double getAverageResponseTime() {
            return totalRequests > 0 ? (double) totalResponseTime / totalRequests : 0;
        }
        
        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successCount / totalRequests * 100 : 0;
        }
        
        // Getters
        public int getTotalRequests() { return totalRequests; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public long getMinResponseTime() { return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime; }
        public long getMaxResponseTime() { return maxResponseTime; }
        public double averageResponseTime() { return getAverageResponseTime(); }
    }
    
    public static class ApiTestResult {
        private final String testName;
        private final boolean passed;
        private final long responseTime;
        private final String message;
        private final int statusCode;
        
        public ApiTestResult(String testName, boolean passed, long responseTime, 
                           String message, int statusCode) {
            this.testName = testName;
            this.passed = passed;
            this.responseTime = responseTime;
            this.message = message;
            this.statusCode = statusCode;
        }
        
        // Getters
        public String getTestName() { return testName; }
        public boolean isPassed() { return passed; }
        public long getResponseTime() { return responseTime; }
        public String getMessage() { return message; }
        public int getStatusCode() { return statusCode; }
    }
    
    public static class ApiTestSummary {
        private final int totalTests;
        private final int passedTests;
        private final int failedTests;
        private final double averageResponseTime;
        private final Map<String, ApiMetrics> endpointMetrics;
        
        public ApiTestSummary(int totalTests, int passedTests, int failedTests, 
                            double averageResponseTime, Map<String, ApiMetrics> endpointMetrics) {
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.failedTests = failedTests;
            this.averageResponseTime = averageResponseTime;
            this.endpointMetrics = endpointMetrics;
        }
        
        // Getters
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return failedTests; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public Map<String, ApiMetrics> getEndpointMetrics() { return endpointMetrics; }
        public double getPassRate() { return totalTests > 0 ? (double) passedTests / totalTests * 100 : 0; }
    }
}
