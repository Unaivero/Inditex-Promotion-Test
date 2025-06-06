package com.inditex.test.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates comprehensive edge case and negative test scenarios
 * for promotional testing including security, boundary, and error conditions.
 */
public class EdgeCaseDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(EdgeCaseDataGenerator.class);
    
    // Security test patterns
    private static final String[] XSS_PATTERNS = {
        "<script>alert('xss')</script>",
        "javascript:alert('xss')",
        "<img src=x onerror=alert('xss')>",
        "'\"><script>alert('xss')</script>",
        "<svg onload=alert('xss')>",
        "';alert('xss');//",
        "<iframe src=javascript:alert('xss')></iframe>"
    };
    
    private static final String[] SQL_INJECTION_PATTERNS = {
        "'; DROP TABLE promotions; --",
        "' OR '1'='1",
        "'; DELETE FROM users; --",
        "' UNION SELECT * FROM promotions --",
        "admin'--",
        "' OR 1=1 --",
        "'; INSERT INTO promotions VALUES ('hack'); --"
    };
    
    // Boundary value test cases
    private static final double[] BOUNDARY_PRICES = {0.01, 0.99, 1.00, 9.99, 10.00, 99.99, 100.00, 999.99, 1000.00, 9999.99};
    private static final int[] BOUNDARY_DISCOUNTS = {0, 1, 99, 100, 101, -1, -10, 999, 1000};
    
    // Invalid data patterns
    private static final String[] INVALID_CODES = {
        "", " ", "   ", "\t", "\n", "\r\n",
        "TOOLONGPROMOTIONCODE123456789012345678901234567890",
        "a", "AB", 
        "123456789012345678901234567890123456789012345678901234567890", // Too long
        "inv@lid", "inv#lid", "inv$lid", "inv%lid", "inv&lid",
        "null", "NULL", "undefined", "NaN", "Infinity"
    };
    
    private static final String[] INVALID_CUSTOMER_TYPES = {
        "", "invalid", "admin", "root", "system", "null", "undefined"
    };
    
    /**
     * Generates comprehensive edge case test scenarios
     */
    public List<EdgeCaseTestData> generateEdgeCases(int totalCases) {
        logger.info("Generating {} edge case test scenarios", totalCases);
        
        List<EdgeCaseTestData> edgeCases = new ArrayList<>();
        Random random = new Random(54321);
        
        // Distribute edge cases across different categories
        int securityCases = totalCases / 4;
        int boundaryCases = totalCases / 4;
        int invalidDataCases = totalCases / 4;
        int businessLogicCases = totalCases - securityCases - boundaryCases - invalidDataCases;
        
        edgeCases.addAll(generateSecurityEdgeCases(securityCases, random));
        edgeCases.addAll(generateBoundaryValueCases(boundaryCases, random));
        edgeCases.addAll(generateInvalidDataCases(invalidDataCases, random));
        edgeCases.addAll(generateBusinessLogicEdgeCases(businessLogicCases, random));
        
        // Shuffle to mix different types
        Collections.shuffle(edgeCases, random);
        
        logger.info("Generated {} total edge case scenarios", edgeCases.size());
        return edgeCases;
    }
    
    /**
     * Generates security-focused edge cases (XSS, SQL injection, etc.)
     */
    private List<EdgeCaseTestData> generateSecurityEdgeCases(int count, Random random) {
        List<EdgeCaseTestData> securityCases = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String testType = i % 2 == 0 ? "XSS_INJECTION" : "SQL_INJECTION";
            String[] patterns = testType.equals("XSS_INJECTION") ? XSS_PATTERNS : SQL_INJECTION_PATTERNS;
            String maliciousInput = patterns[random.nextInt(patterns.length)];
            
            securityCases.add(new EdgeCaseTestData(
                "SEC" + String.format("%03d", i),
                "Zara", "ES", "es", "guest",
                maliciousInput, // Use as promotion code
                "SECURITY_VIOLATION",
                "Invalid characters in promotion code",
                testType,
                "Security validation should reject malicious input",
                "HIGH"
            ));
        }
        
        return securityCases;
    }
    
    /**
     * Generates boundary value test cases
     */
    private List<EdgeCaseTestData> generateBoundaryValueCases(int count, Random random) {
        List<EdgeCaseTestData> boundaryCases = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String testId = "BND" + String.format("%03d", i);
            
            if (i % 3 == 0) {
                // Price boundary tests
                double boundaryPrice = BOUNDARY_PRICES[random.nextInt(BOUNDARY_PRICES.length)];
                boundaryCases.add(createPriceBoundaryCase(testId, boundaryPrice));
                
            } else if (i % 3 == 1) {
                // Discount boundary tests
                int boundaryDiscount = BOUNDARY_DISCOUNTS[random.nextInt(BOUNDARY_DISCOUNTS.length)];
                boundaryCases.add(createDiscountBoundaryCase(testId, boundaryDiscount));
                
            } else {
                // Date boundary tests
                boundaryCases.add(createDateBoundaryCase(testId, random));
            }
        }
        
        return boundaryCases;
    }
    
    /**
     * Generates invalid data format test cases
     */
    private List<EdgeCaseTestData> generateInvalidDataCases(int count, Random random) {
        List<EdgeCaseTestData> invalidCases = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String testId = "INV" + String.format("%03d", i);
            
            if (i % 4 == 0) {
                // Invalid promotion codes
                String invalidCode = INVALID_CODES[random.nextInt(INVALID_CODES.length)];
                invalidCases.add(createInvalidCodeCase(testId, invalidCode));
                
            } else if (i % 4 == 1) {
                // Invalid customer types
                String invalidCustomer = INVALID_CUSTOMER_TYPES[random.nextInt(INVALID_CUSTOMER_TYPES.length)];
                invalidCases.add(createInvalidCustomerCase(testId, invalidCustomer));
                
            } else if (i % 4 == 2) {
                // Invalid SKU formats
                invalidCases.add(createInvalidSKUCase(testId, random));
                
            } else {
                // Invalid currency/locale combinations
                invalidCases.add(createInvalidLocaleCase(testId, random));
            }
        }
        
        return invalidCases;
    }
    
    /**
     * Generates business logic edge cases
     */
    private List<EdgeCaseTestData> generateBusinessLogicEdgeCases(int count, Random random) {
        List<EdgeCaseTestData> businessCases = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String testId = "BIZ" + String.format("%03d", i);
            
            switch (i % 6) {
                case 0:
                    businessCases.add(createExpiredPromotionCase(testId));
                    break;
                case 1:
                    businessCases.add(createInactivePromotionCase(testId));
                    break;
                case 2:
                    businessCases.add(createExclusionConflictCase(testId));
                    break;
                case 3:
                    businessCases.add(createQuantityLimitCase(testId));
                    break;
                case 4:
                    businessCases.add(createMinimumOrderCase(testId));
                    break;
                default:
                    businessCases.add(createStackingConflictCase(testId));
                    break;
            }
        }
        
        return businessCases;
    }
    
    // Helper methods for creating specific edge case types
    private EdgeCaseTestData createPriceBoundaryCase(String testId, double price) {
        String expectedError = price <= 0 ? "INVALID_PRICE" : 
                              price > 10000 ? "PRICE_TOO_HIGH" : "VALID";
        
        return new EdgeCaseTestData(
            testId, "Zara", "ES", "es", "guest",
            "TESTCODE" + testId,
            expectedError,
            price <= 0 ? "Price must be greater than zero" : 
            price > 10000 ? "Price exceeds maximum allowed" : "Valid price",
            "PRICE_BOUNDARY",
            "Testing price boundary value: " + price,
            price <= 0 || price > 10000 ? "HIGH" : "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createDiscountBoundaryCase(String testId, int discount) {
        String expectedError = discount < 0 ? "NEGATIVE_DISCOUNT" :
                              discount > 100 ? "DISCOUNT_TOO_HIGH" : "VALID";
        
        return new EdgeCaseTestData(
            testId, "Bershka", "FR", "fr", "member",
            "DISCOUNT" + discount,
            expectedError,
            discount < 0 ? "Discount cannot be negative" :
            discount > 100 ? "Discount cannot exceed 100%" : "Valid discount",
            "DISCOUNT_BOUNDARY",
            "Testing discount boundary value: " + discount + "%",
            discount < 0 || discount > 100 ? "HIGH" : "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createDateBoundaryCase(String testId, Random random) {
        LocalDate[] testDates = {
            LocalDate.of(1900, 1, 1),     // Far past
            LocalDate.of(2000, 2, 29),    // Leap year
            LocalDate.now().minusDays(1), // Yesterday (expired)
            LocalDate.now(),              // Today
            LocalDate.now().plusDays(1),  // Tomorrow
            LocalDate.of(2100, 12, 31)    // Far future
        };
        
        LocalDate testDate = testDates[random.nextInt(testDates.length)];
        boolean isExpired = testDate.isBefore(LocalDate.now());
        
        return new EdgeCaseTestData(
            testId, "Stradivarius", "DE", "de", "guest",
            "DATE" + testDate.toString().replace("-", ""),
            isExpired ? "PROMOTION_EXPIRED" : "VALID",
            isExpired ? "Promotion has expired" : "Valid promotion date",
            "DATE_BOUNDARY",
            "Testing promotion date: " + testDate,
            isExpired ? "MEDIUM" : "LOW"
        );
    }
    
    private EdgeCaseTestData createInvalidCodeCase(String testId, String invalidCode) {
        String expectedError = invalidCode.trim().isEmpty() ? "EMPTY_CODE" :
                              invalidCode.length() > 20 ? "CODE_TOO_LONG" :
                              invalidCode.length() < 3 ? "CODE_TOO_SHORT" : "INVALID_FORMAT";
        
        return new EdgeCaseTestData(
            testId, "Pull&Bear", "GB", "en", "guest",
            invalidCode,
            expectedError,
            "Invalid promotion code format",
            "INVALID_CODE_FORMAT",
            "Testing invalid promotion code: '" + invalidCode + "'",
            "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createInvalidCustomerCase(String testId, String invalidCustomer) {
        return new EdgeCaseTestData(
            testId, "Massimo Dutti", "IT", "it", invalidCustomer,
            "VALIDCODE123",
            "INVALID_CUSTOMER_TYPE",
            "Invalid customer type",
            "INVALID_CUSTOMER",
            "Testing invalid customer type: '" + invalidCustomer + "'",
            "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createInvalidSKUCase(String testId, Random random) {
        String[] invalidSKUs = {"", "123", "TOOLONGSKU123456789012345678901234567890", "SKU#INVALID", "SKU WITH SPACES"};
        String invalidSKU = invalidSKUs[random.nextInt(invalidSKUs.length)];
        
        return new EdgeCaseTestData(
            testId, "Oysho", "PT", "pt", "member",
            "VALIDCODE123",
            "INVALID_SKU",
            "Invalid SKU format",
            "INVALID_SKU_FORMAT",
            "Testing invalid SKU: '" + invalidSKU + "'",
            "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createInvalidLocaleCase(String testId, Random random) {
        String[][] invalidCombos = {
            {"XX", "invalid"}, {"ES", "en"}, {"US", "fr"}, {"", ""}, {"123", "456"}
        };
        String[] combo = invalidCombos[random.nextInt(invalidCombos.length)];
        
        return new EdgeCaseTestData(
            testId, "Zara", combo[0], combo[1], "guest",
            "VALIDCODE123",
            "INVALID_LOCALE",
            "Invalid country/language combination",
            "INVALID_LOCALE",
            "Testing invalid locale: " + combo[0] + "/" + combo[1],
            "LOW"
        );
    }
    
    private EdgeCaseTestData createExpiredPromotionCase(String testId) {
        return new EdgeCaseTestData(
            testId, "Zara", "ES", "es", "member",
            "EXPIRED2023",
            "PROMOTION_EXPIRED",
            "This promotion has expired",
            "EXPIRED_PROMOTION",
            "Testing expired promotion behavior",
            "HIGH"
        );
    }
    
    private EdgeCaseTestData createInactivePromotionCase(String testId) {
        return new EdgeCaseTestData(
            testId, "Bershka", "FR", "fr", "guest",
            "INACTIVE2024",
            "PROMOTION_INACTIVE",
            "This promotion is not currently active",
            "INACTIVE_PROMOTION",
            "Testing inactive promotion behavior",
            "HIGH"
        );
    }
    
    private EdgeCaseTestData createExclusionConflictCase(String testId) {
        return new EdgeCaseTestData(
            testId, "Pull&Bear", "DE", "de", "vip",
            "EXCLUSIVE2024",
            "PROMOTION_CONFLICT",
            "This promotion cannot be combined with VIP discounts",
            "EXCLUSION_CONFLICT",
            "Testing promotion exclusion rules",
            "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createQuantityLimitCase(String testId) {
        return new EdgeCaseTestData(
            testId, "Stradivarius", "IT", "it", "guest",
            "LIMITEDQTY",
            "QUANTITY_EXCEEDED",
            "Maximum quantity for this promotion exceeded",
            "QUANTITY_LIMIT",
            "Testing quantity limitation enforcement",
            "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createMinimumOrderCase(String testId) {
        return new EdgeCaseTestData(
            testId, "Massimo Dutti", "GB", "en", "member",
            "MINORDER50",
            "MINIMUM_ORDER_NOT_MET",
            "Minimum order value not met for this promotion",
            "MINIMUM_ORDER",
            "Testing minimum order value requirements",
            "MEDIUM"
        );
    }
    
    private EdgeCaseTestData createStackingConflictCase(String testId) {
        return new EdgeCaseTestData(
            testId, "Oysho", "US", "en", "premium",
            "NOSTACKING",
            "STACKING_NOT_ALLOWED",
            "This promotion cannot be combined with other discounts",
            "STACKING_CONFLICT",
            "Testing promotion stacking restrictions",
            "MEDIUM"
        );
    }
    
    /**
     * Writes edge case test data to CSV file
     */
    public void writeEdgeCasesToCSV(List<EdgeCaseTestData> edgeCases, String filePath) throws IOException {
        logger.info("Writing {} edge case records to {}", edgeCases.size(), filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("test_id,brand,country,language,customer_type,promotion_code,expected_error,error_message,test_category,test_description,priority\n");
            
            // Write data
            for (EdgeCaseTestData data : edgeCases) {
                writer.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    data.testId, data.brand, data.country, data.language, data.customerType,
                    escapeCsvValue(data.promotionCode), data.expectedError, 
                    escapeCsvValue(data.errorMessage), data.testCategory,
                    escapeCsvValue(data.testDescription), data.priority
                ));
            }
        }
        
        logger.info("Successfully wrote edge case data to {}", filePath);
    }
    
    private String escapeCsvValue(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Edge case test data structure
     */
    public static class EdgeCaseTestData {
        public final String testId, brand, country, language, customerType;
        public final String promotionCode, expectedError, errorMessage, testCategory;
        public final String testDescription, priority;
        
        public EdgeCaseTestData(String testId, String brand, String country, String language,
                               String customerType, String promotionCode, String expectedError,
                               String errorMessage, String testCategory, String testDescription,
                               String priority) {
            this.testId = testId;
            this.brand = brand;
            this.country = country;
            this.language = language;
            this.customerType = customerType;
            this.promotionCode = promotionCode;
            this.expectedError = expectedError;
            this.errorMessage = errorMessage;
            this.testCategory = testCategory;
            this.testDescription = testDescription;
            this.priority = priority;
        }
    }
}