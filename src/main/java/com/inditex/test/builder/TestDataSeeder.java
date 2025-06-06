package com.inditex.test.builder;

import com.inditex.test.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Test data seeding and management utility.
 * Handles generation, validation, and deployment of test data across environments.
 */
public class TestDataSeeder {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataSeeder.class);
    
    private static final String TEST_DATA_DIR = "src/test/resources/testdata";
    private static final String GENERATED_DATA_DIR = TEST_DATA_DIR + "/generated";
    private static final String BACKUP_DATA_DIR = TEST_DATA_DIR + "/backup";
    
    /**
     * Seeds comprehensive test data for all environments
     */
    public static void seedAllEnvironments() {
        logger.info("Starting comprehensive test data seeding");
        
        try {
            // Create necessary directories
            createDirectories();
            
            // Backup existing data
            backupExistingData();
            
            // Generate and seed different data sets
            seedRegularPromotionData();
            seedSeasonalCampaignData();
            seedBulkDiscountData();
            seedEdgeCaseData();
            seedPerformanceTestData();
            
            logger.info("Successfully completed test data seeding");
            
        } catch (Exception e) {
            logger.error("Failed to seed test data", e);
            throw new RuntimeException("Test data seeding failed", e);
        }
    }
    
    /**
     * Seeds regular promotion test data (500 records)
     */
    private static void seedRegularPromotionData() throws IOException {
        logger.info("Generating regular promotion test data");
        
        List<TestDataGenerator.PromotionTestData> regularData = 
            TestDataGenerator.generateComprehensiveTestData(500);
        
        String filePath = GENERATED_DATA_DIR + "/regular_promotions_data.csv";
        TestDataGenerator.writeToCSV(regularData, filePath);
        
        logger.info("Generated {} regular promotion records in {}", regularData.size(), filePath);
    }
    
    /**
     * Seeds seasonal campaign test data (200 records)
     */
    private static void seedSeasonalCampaignData() throws IOException {
        logger.info("Generating seasonal campaign test data");
        
        List<TestDataGenerator.PromotionTestData> seasonalData = 
            TestDataGenerator.generateComprehensiveTestData(200);
        
        String filePath = GENERATED_DATA_DIR + "/seasonal_campaigns_data.csv";
        TestDataGenerator.writeToCSV(seasonalData, filePath);
        
        logger.info("Generated {} seasonal campaign records in {}", seasonalData.size(), filePath);
    }
    
    /**
     * Seeds bulk discount scenarios (100 records)
     */
    private static void seedBulkDiscountData() throws IOException {
        logger.info("Generating bulk discount test data");
        
        List<TestDataGenerator.PromotionTestData> bulkData = 
            TestDataGenerator.generateBulkDiscountScenarios(100);
        
        String filePath = GENERATED_DATA_DIR + "/bulk_discount_data.csv";
        TestDataGenerator.writeToCSV(bulkData, filePath);
        
        logger.info("Generated {} bulk discount records in {}", bulkData.size(), filePath);
    }
    
    /**
     * Seeds edge case and negative test data
     */
    private static void seedEdgeCaseData() throws IOException {
        logger.info("Generating edge case test data");
        
        EdgeCaseDataGenerator edgeGenerator = new EdgeCaseDataGenerator();
        List<EdgeCaseDataGenerator.EdgeCaseTestData> edgeCases = edgeGenerator.generateEdgeCases(150);
        
        String filePath = GENERATED_DATA_DIR + "/edge_cases_data.csv";
        edgeGenerator.writeEdgeCasesToCSV(edgeCases, filePath);
        
        logger.info("Generated {} edge case records in {}", edgeCases.size(), filePath);
    }
    
    /**
     * Seeds performance test data (1000+ records for load testing)
     */
    private static void seedPerformanceTestData() throws IOException {
        logger.info("Generating performance test data");
        
        List<TestDataGenerator.PromotionTestData> performanceData = 
            TestDataGenerator.generateComprehensiveTestData(1000);
        
        String filePath = GENERATED_DATA_DIR + "/performance_test_data.csv";
        TestDataGenerator.writeToCSV(performanceData, filePath);
        
        logger.info("Generated {} performance test records in {}", performanceData.size(), filePath);
    }
    
    /**
     * Creates necessary directory structure
     */
    private static void createDirectories() throws IOException {
        Path testDataPath = Paths.get(TEST_DATA_DIR);
        Path generatedPath = Paths.get(GENERATED_DATA_DIR);
        Path backupPath = Paths.get(BACKUP_DATA_DIR);
        
        Files.createDirectories(testDataPath);
        Files.createDirectories(generatedPath);
        Files.createDirectories(backupPath);
        
        logger.debug("Created directory structure for test data");
    }
    
    /**
     * Backs up existing test data before regenerating
     */
    private static void backupExistingData() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path testDataPath = Paths.get(TEST_DATA_DIR);
        
        if (Files.exists(testDataPath)) {
            Files.walk(testDataPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".csv"))
                .forEach(source -> {
                    try {
                        String fileName = source.getFileName().toString();
                        String backupFileName = timestamp + "_" + fileName;
                        Path backup = Paths.get(BACKUP_DATA_DIR, backupFileName);
                        Files.copy(source, backup);
                        logger.debug("Backed up {} to {}", fileName, backup);
                    } catch (IOException e) {
                        logger.warn("Failed to backup {}: {}", source, e.getMessage());
                    }
                });
        }
    }
    
    /**
     * Validates generated test data integrity
     */
    public static boolean validateTestData() {
        logger.info("Validating generated test data");
        
        try {
            Path generatedPath = Paths.get(GENERATED_DATA_DIR);
            
            if (!Files.exists(generatedPath)) {
                logger.error("Generated data directory does not exist: {}", generatedPath);
                return false;
            }
            
            // Check required files exist
            String[] requiredFiles = {
                "regular_promotions_data.csv",
                "seasonal_campaigns_data.csv", 
                "bulk_discount_data.csv",
                "edge_cases_data.csv",
                "performance_test_data.csv"
            };
            
            for (String file : requiredFiles) {
                Path filePath = generatedPath.resolve(file);
                if (!Files.exists(filePath)) {
                    logger.error("Required test data file missing: {}", file);
                    return false;
                }
                
                long lineCount = Files.lines(filePath).count();
                if (lineCount < 2) { // Header + at least one data row
                    logger.error("Test data file {} appears to be empty or invalid", file);
                    return false;
                }
                
                logger.debug("Validated {}: {} lines", file, lineCount);
            }
            
            logger.info("All test data files validated successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Test data validation failed", e);
            return false;
        }
    }
    
    /**
     * Cleans up old backup files (keeps last 10)
     */
    public static void cleanupOldBackups() {
        try {
            Path backupPath = Paths.get(BACKUP_DATA_DIR);
            
            if (!Files.exists(backupPath)) {
                return;
            }
            
            Files.list(backupPath)
                .filter(Files::isRegularFile)
                .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                .skip(10) // Keep last 10 backups
                .forEach(file -> {
                    try {
                        Files.delete(file);
                        logger.debug("Deleted old backup: {}", file.getFileName());
                    } catch (IOException e) {
                        logger.warn("Failed to delete old backup {}: {}", file, e.getMessage());
                    }
                });
                
        } catch (Exception e) {
            logger.warn("Failed to cleanup old backups", e);
        }
    }
    
    /**
     * Gets test data statistics
     */
    public static void printTestDataStatistics() {
        logger.info("=== Test Data Statistics ===");
        
        try {
            Path generatedPath = Paths.get(GENERATED_DATA_DIR);
            
            if (!Files.exists(generatedPath)) {
                logger.info("No generated test data found");
                return;
            }
            
            Files.list(generatedPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".csv"))
                .forEach(file -> {
                    try {
                        long lineCount = Files.lines(file).count() - 1; // Exclude header
                        long fileSize = Files.size(file);
                        
                        logger.info("{}: {} records, {} bytes", 
                                   file.getFileName(), lineCount, fileSize);
                                   
                    } catch (IOException e) {
                        logger.warn("Failed to read statistics for {}: {}", file, e.getMessage());
                    }
                });
                
        } catch (Exception e) {
            logger.error("Failed to generate test data statistics", e);
        }
        
        logger.info("============================");
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        logger.info("Starting test data seeding process");
        
        try {
            // Seed all test data
            seedAllEnvironments();
            
            // Validate generated data
            if (validateTestData()) {
                logger.info("Test data seeding completed successfully");
                printTestDataStatistics();
            } else {
                logger.error("Test data validation failed");
                System.exit(1);
            }
            
            // Cleanup old backups
            cleanupOldBackups();
            
        } catch (Exception e) {
            logger.error("Test data seeding process failed", e);
            System.exit(1);
        }
    }
}