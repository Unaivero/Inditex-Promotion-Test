package com.inditex.test.utils;

import com.inditex.test.exceptions.TestDataException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CsvDataReader {
    private static final Logger logger = LoggerFactory.getLogger(CsvDataReader.class);
    private static final ConcurrentHashMap<String, List<Map<String, String>>> dataCache = new ConcurrentHashMap<>();

    public static List<Map<String, String>> getTestData(String csvFileName) {
        return dataCache.computeIfAbsent(csvFileName, CsvDataReader::loadTestDataFromFile);
    }
    
    /**
     * Gets test data from the generated data directory
     */
    public static List<Map<String, String>> getGeneratedTestData(String csvFileName) {
        String generatedFileName = "generated/" + csvFileName;
        return dataCache.computeIfAbsent(generatedFileName, CsvDataReader::loadTestDataFromFile);
    }
    
    /**
     * Gets comprehensive promotion test data (500+ records)
     */
    public static List<Map<String, String>> getComprehensivePromotionData() {
        return getGeneratedTestData("comprehensive_promotions_data.csv");
    }
    
    /**
     * Gets seasonal campaign test data
     */
    public static List<Map<String, String>> getSeasonalCampaignData() {
        return getTestData("seasonal_campaigns_2024.csv");
    }
    
    /**
     * Gets edge case test data
     */
    public static List<Map<String, String>> getEdgeCaseData() {
        return getGeneratedTestData("edge_cases_comprehensive.csv");
    }
    
    /**
     * Gets performance test data (1000+ records)
     */
    public static List<Map<String, String>> getPerformanceTestData() {
        return getGeneratedTestData("performance_test_data_sample.csv");
    }
    
    /**
     * Gets filtered test data by campaign type
     */
    public static List<Map<String, String>> getTestDataByType(String campaignType) {
        List<Map<String, String>> allData = getComprehensivePromotionData();
        return allData.stream()
                .filter(record -> campaignType.equals(record.get("campaign_type")))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Gets filtered test data by customer type
     */
    public static List<Map<String, String>> getTestDataByCustomerType(String customerType) {
        List<Map<String, String>> allData = getComprehensivePromotionData();
        return allData.stream()
                .filter(record -> customerType.equals(record.get("customer_type")))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Gets filtered test data by brand
     */
    public static List<Map<String, String>> getTestDataByBrand(String brand) {
        List<Map<String, String>> allData = getComprehensivePromotionData();
        return allData.stream()
                .filter(record -> brand.equals(record.get("brand")))
                .collect(java.util.stream.Collectors.toList());
    }
    
    private static List<Map<String, String>> loadTestDataFromFile(String csvFileName) {
        logger.info("Loading test data from CSV file: {}", csvFileName);
        
        List<Map<String, String>> testDataList = new ArrayList<>();
        String filePath = "/testdata/" + csvFileName;
        
        try (InputStream inputStream = CsvDataReader.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                String errorMsg = "Cannot find CSV file: " + filePath + ". Ensure it's in src/test/resources/testdata";
                logger.error(errorMsg);
                throw new TestDataException(errorMsg);
            }
            
            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 CSVParser csvParser = new CSVParser(reader, getCSVFormat())) {
                
                logger.debug("Parsing CSV file: {}", csvFileName);
                
                for (CSVRecord csvRecord : csvParser) {
                    Map<String, String> recordMap = csvRecord.toMap();
                    validateRecord(recordMap, csvRecord.getRecordNumber());
                    testDataList.add(recordMap);
                }
                
                logger.info("Successfully loaded {} records from {}", testDataList.size(), csvFileName);
            }
            
        } catch (IOException e) {
            String errorMsg = "Failed to read CSV file: " + csvFileName;
            logger.error(errorMsg, e);
            throw new TestDataException(errorMsg, e);
        }
        
        return testDataList;
    }
    
    private static CSVFormat getCSVFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();
    }
    
    private static void validateRecord(Map<String, String> record, long recordNumber) {
        if (record.isEmpty()) {
            throw new TestDataException("Empty record found at line: " + recordNumber);
        }
        
        // Validate required fields for promotion data
        String[] requiredFields = {"sku", "brand", "country", "language"};
        for (String field : requiredFields) {
            String value = record.get(field);
            if (value == null || value.trim().isEmpty()) {
                throw new TestDataException(
                    String.format("Missing required field '%s' at record %d", field, recordNumber));
            }
        }
        
        logger.debug("Validated record {} with SKU: {}", recordNumber, record.get("sku"));
    }
    
    public static void clearCache() {
        logger.info("Clearing test data cache");
        dataCache.clear();
    }
    
    public static int getCacheSize() {
        return dataCache.size();
    }

    // Example usage (optional - can be removed or used for testing this utility)
    /*
    public static void main(String[] args) {
        try {
            List<Map<String, String>> data = getTestData("promotions_data.csv");
            for (Map<String, String> row : data) {
                System.out.println(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
