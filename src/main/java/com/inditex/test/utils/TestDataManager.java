package com.inditex.test.utils;

import com.inditex.test.builder.TestDataSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Central test data management utility for the InditexPromotionsTest framework.
 * Handles test data lifecycle, validation, and provides convenient access methods.
 */
public class TestDataManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataManager.class);
    
    // Data sources configuration
    private static final String LEGACY_DATA_PATH = "src/test/resources/testdata/promotions_data.csv";
    private static final String COMPREHENSIVE_DATA_PATH = "src/test/resources/testdata/generated/comprehensive_promotions_data.csv";
    private static final String SEASONAL_DATA_PATH = "src/test/resources/testdata/seasonal_campaigns_2024.csv";
    private static final String EDGE_CASES_PATH = "src/test/resources/testdata/generated/edge_cases_comprehensive.csv";
    private static final String PERFORMANCE_DATA_PATH = "src/test/resources/testdata/generated/performance_test_data_sample.csv";
    private static final String BULK_DISCOUNT_PATH = "src/test/resources/testdata/generated/bulk_discount_scenarios.csv";
    
    // Test data statistics
    private static TestDataStatistics statistics;
    
    /**
     * Initializes the test data manager and ensures all data sources are available
     */
    public static void initialize() {
        logger.info("Initializing TestDataManager");
        
        try {
            // Check if comprehensive data exists, if not generate it
            if (!isComprehensiveDataAvailable()) {
                logger.info("Comprehensive test data not found, generating new data...");
                generateComprehensiveTestData();
            }
            
            // Update statistics
            updateStatistics();
            
            // Validate data integrity
            validateDataIntegrity();
            
            logger.info("TestDataManager initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize TestDataManager", e);
            throw new RuntimeException("TestDataManager initialization failed", e);
        }
    }
    
    /**
     * Gets comprehensive promotional test data (500+ records)
     */
    public static List<Map<String, String>> getComprehensivePromotionData() {
        ensureInitialized();
        return CsvDataReader.getComprehensivePromotionData();
    }
    
    /**
     * Gets seasonal campaign test data with date ranges
     */
    public static List<Map<String, String>> getSeasonalCampaignData() {
        ensureInitialized();
        return CsvDataReader.getSeasonalCampaignData();
    }
    
    /**
     * Gets edge case and negative test scenarios
     */
    public static List<Map<String, String>> getEdgeCaseData() {
        ensureInitialized();
        return CsvDataReader.getEdgeCaseData();
    }
    
    /**
     * Gets performance test data for load testing (1000+ records)
     */
    public static List<Map<String, String>> getPerformanceTestData() {
        ensureInitialized();
        return CsvDataReader.getPerformanceTestData();
    }
    
    /**
     * Gets bulk discount and multi-buy scenarios
     */
    public static List<Map<String, String>> getBulkDiscountData() {
        ensureInitialized();
        return CsvDataReader.getGeneratedTestData("bulk_discount_scenarios.csv");
    }
    
    /**
     * Gets filtered test data for specific test scenarios
     */
    public static List<Map<String, String>> getTestDataForScenario(TestScenario scenario) {
        ensureInitialized();
        
        switch (scenario) {
            case SMOKE_TEST:
                return getSmokTestData();
            case REGRESSION:
                return getComprehensivePromotionData();
            case SEASONAL_CAMPAIGNS:
                return getSeasonalCampaignData();
            case EDGE_CASES:
                return getEdgeCaseData();
            case PERFORMANCE:
                return getPerformanceTestData();
            case BULK_DISCOUNTS:
                return getBulkDiscountData();
            case SECURITY:
                return getSecurityTestData();
            case ACCESSIBILITY:
                return getAccessibilityTestData();
            case MOBILE:
                return getMobileTestData();
            default:
                logger.warn("Unknown test scenario: {}, returning comprehensive data", scenario);
                return getComprehensivePromotionData();
        }
    }
    
    /**
     * Gets test data filtered by customer type
     */
    public static List<Map<String, String>> getDataByCustomerType(CustomerType customerType) {
        ensureInitialized();
        return CsvDataReader.getTestDataByCustomerType(customerType.getValue());
    }
    
    /**
     * Gets test data filtered by brand
     */
    public static List<Map<String, String>> getDataByBrand(InditexBrand brand) {
        ensureInitialized();
        return CsvDataReader.getTestDataByBrand(brand.getValue());
    }
    
    /**
     * Gets test data statistics
     */
    public static TestDataStatistics getStatistics() {
        ensureInitialized();
        return statistics;
    }
    
    /**
     * Refreshes all test data by regenerating from sources
     */
    public static void refreshTestData() {
        logger.info("Refreshing all test data");
        
        try {
            // Clear existing cache
            CsvDataReader.clearCache();
            
            // Regenerate comprehensive data
            generateComprehensiveTestData();
            
            // Update statistics
            updateStatistics();
            
            // Validate integrity
            validateDataIntegrity();
            
            logger.info("Test data refresh completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to refresh test data", e);
            throw new RuntimeException("Test data refresh failed", e);
        }
    }
    
    // Private helper methods
    
    private static void ensureInitialized() {
        if (statistics == null) {
            initialize();
        }
    }
    
    private static boolean isComprehensiveDataAvailable() {
        Path dataPath = Paths.get(COMPREHENSIVE_DATA_PATH);
        if (!Files.exists(dataPath)) {
            return false;
        }
        
        try {
            long lineCount = Files.lines(dataPath).count();
            return lineCount > 100; // Should have at least 100+ records
        } catch (IOException e) {
            logger.warn("Failed to check comprehensive data availability", e);
            return false;
        }
    }
    
    private static void generateComprehensiveTestData() {
        logger.info("Generating comprehensive test data");
        TestDataSeeder.seedAllEnvironments();
    }
    
    private static void updateStatistics() {
        logger.debug("Updating test data statistics");
        
        try {
            int comprehensiveCount = getFileRecordCount(COMPREHENSIVE_DATA_PATH);
            int seasonalCount = getFileRecordCount(SEASONAL_DATA_PATH);
            int edgeCaseCount = getFileRecordCount(EDGE_CASES_PATH);
            int performanceCount = getFileRecordCount(PERFORMANCE_DATA_PATH);
            int bulkDiscountCount = getFileRecordCount(BULK_DISCOUNT_PATH);
            
            statistics = new TestDataStatistics(
                comprehensiveCount, seasonalCount, edgeCaseCount,
                performanceCount, bulkDiscountCount,
                LocalDateTime.now()
            );
            
            logger.debug("Updated statistics: {}", statistics);
            
        } catch (Exception e) {
            logger.warn("Failed to update statistics", e);
            statistics = new TestDataStatistics(0, 0, 0, 0, 0, LocalDateTime.now());
        }
    }
    
    private static int getFileRecordCount(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return (int) (Files.lines(path).count() - 1); // Exclude header
            }
        } catch (IOException e) {
            logger.warn("Failed to count records in {}", filePath, e);
        }
        return 0;
    }
    
    private static void validateDataIntegrity() {
        logger.debug("Validating test data integrity");
        
        boolean isValid = TestDataSeeder.validateTestData();
        if (!isValid) {
            throw new RuntimeException("Test data integrity validation failed");
        }
        
        logger.debug("Test data integrity validation passed");
    }
    
    private static List<Map<String, String>> getSmokTestData() {
        // Return a subset of data for smoke testing (e.g., first 20 records)
        List<Map<String, String>> allData = getComprehensivePromotionData();
        return allData.subList(0, Math.min(20, allData.size()));
    }
    
    private static List<Map<String, String>> getSecurityTestData() {
        return getEdgeCaseData().stream()
                .filter(record -> "XSS_INJECTION".equals(record.get("test_category")) ||
                                "SQL_INJECTION".equals(record.get("test_category")))
                .collect(java.util.stream.Collectors.toList());
    }
    
    private static List<Map<String, String>> getAccessibilityTestData() {
        return getEdgeCaseData().stream()
                .filter(record -> "ACCESSIBILITY_TEST".equals(record.get("test_category")))
                .collect(java.util.stream.Collectors.toList());
    }
    
    private static List<Map<String, String>> getMobileTestData() {
        return getEdgeCaseData().stream()
                .filter(record -> "MOBILE_TEST".equals(record.get("test_category")))
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Enums and data classes
    
    public enum TestScenario {
        SMOKE_TEST, REGRESSION, SEASONAL_CAMPAIGNS, EDGE_CASES,
        PERFORMANCE, BULK_DISCOUNTS, SECURITY, ACCESSIBILITY, MOBILE
    }
    
    public enum CustomerType {
        GUEST("guest"), MEMBER("member"), VIP("vip"), PREMIUM("premium"),
        STUDENT("student"), EMPLOYEE("employee"), CORPORATE("corporate");
        
        private final String value;
        
        CustomerType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum InditexBrand {
        ZARA("Zara"), BERSHKA("Bershka"), PULL_AND_BEAR("Pull&Bear"),
        MASSIMO_DUTTI("Massimo Dutti"), STRADIVARIUS("Stradivarius"),
        OYSHO("Oysho"), ZARA_HOME("Zara Home"), LEFTIES("Lefties");
        
        private final String value;
        
        InditexBrand(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public static class TestDataStatistics {
        public final int comprehensiveRecords;
        public final int seasonalRecords;
        public final int edgeCaseRecords;
        public final int performanceRecords;
        public final int bulkDiscountRecords;
        public final int totalRecords;
        public final LocalDateTime lastUpdated;
        
        public TestDataStatistics(int comprehensive, int seasonal, int edgeCase,
                                int performance, int bulkDiscount, LocalDateTime lastUpdated) {
            this.comprehensiveRecords = comprehensive;
            this.seasonalRecords = seasonal;
            this.edgeCaseRecords = edgeCase;
            this.performanceRecords = performance;
            this.bulkDiscountRecords = bulkDiscount;
            this.totalRecords = comprehensive + seasonal + edgeCase + performance + bulkDiscount;
            this.lastUpdated = lastUpdated;
        }
        
        @Override
        public String toString() {
            return String.format(
                "TestDataStatistics{total=%d, comprehensive=%d, seasonal=%d, edgeCase=%d, performance=%d, bulkDiscount=%d, lastUpdated=%s}",
                totalRecords, comprehensiveRecords, seasonalRecords, edgeCaseRecords,
                performanceRecords, bulkDiscountRecords, lastUpdated.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }
}