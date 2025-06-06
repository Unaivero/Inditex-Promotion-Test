package com.inditex.test.api;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.model.PromotionalPriceRequest;
import com.inditex.test.model.PromotionalPriceResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

public class PromotionalPricingApiClient {
    private static final Logger logger = LoggerFactory.getLogger(PromotionalPricingApiClient.class);
    
    private final String baseUrl;
    private final String apiKey;
    private final int timeout;
    
    public PromotionalPricingApiClient() {
        this.baseUrl = ConfigManager.getProperty("api.base.url", "https://api.inditex.com");
        this.apiKey = ConfigManager.getEncryptedProperty("api.key");
        this.timeout = ConfigManager.getIntProperty("api.timeout.seconds", 30);
        
        RestAssured.baseURI = baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        logger.info("Initialized API client with base URL: {}", baseUrl);
    }
    
    public Response getPromotionalPrice(String sku, String brand, String country) {
        return getPromotionalPrice(sku, brand, country, null, null);
    }
    
    public Response getPromotionalPrice(String sku, String brand, String country, String customerType, String language) {
        logger.info("Requesting promotional price for SKU: {}, Brand: {}, Country: {}", sku, brand, country);
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .queryParam("sku", sku)
            .queryParam("brand", brand)
            .queryParam("country", country);
        
        if (customerType != null) {
            request.queryParam("customerType", customerType);
        }
        
        if (language != null) {
            request.queryParam("language", language);
        }
        
        Response response = request
            .when()
            .timeout(timeout, TimeUnit.SECONDS)
            .get("/v1/promotions/pricing");
        
        logger.info("API Response - Status: {}, Time: {}ms", 
                   response.getStatusCode(), response.getTime());
        
        return response;
    }
    
    public Response getPromotionalPriceWithAuth(String sku, String brand, String country, String authToken) {
        logger.info("Requesting promotional price with custom auth for SKU: {}", sku);
        
        RequestSpecification request = given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .queryParam("sku", sku)
            .queryParam("brand", brand)
            .queryParam("country", country);
        
        if (authToken != null && !authToken.equals("none")) {
            request.header("Authorization", "Bearer " + authToken);
        }
        
        return request
            .when()
            .timeout(timeout, TimeUnit.SECONDS)
            .get("/v1/promotions/pricing");
    }
    
    public Response getBulkPromotionalPrices(List<PromotionalPriceRequest> requests) {
        logger.info("Requesting bulk promotional prices for {} products", requests.size());
        
        return given()
            .header("Authorization", "Bearer " + apiKey)
            .contentType(ContentType.JSON)
            .body(Map.of("requests", requests))
            .when()
            .timeout(timeout, TimeUnit.SECONDS)
            .post("/v1/promotions/pricing/bulk");
    }
    
    public Response getPromotionalPriceWithInventory(String sku, String brand, String country) {
        logger.info("Requesting promotional price with inventory for SKU: {}", sku);
        
        return given()
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .queryParam("sku", sku)
            .queryParam("brand", brand)
            .queryParam("country", country)
            .queryParam("includeInventory", true)
            .when()
            .timeout(timeout, TimeUnit.SECONDS)
            .get("/v1/promotions/pricing");
    }
    
    public Response[] getConcurrentPromotionalPrices(String sku, String brand, String country, int numberOfRequests) {
        logger.info("Making {} concurrent requests for SKU: {}", numberOfRequests, sku);
        
        Response[] responses = new Response[numberOfRequests];
        
        // Create concurrent requests
        Thread[] threads = new Thread[numberOfRequests];
        
        for (int i = 0; i < numberOfRequests; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                responses[index] = getPromotionalPrice(sku, brand, country);
            });
        }
        
        // Start all threads
        long startTime = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("Thread interrupted during concurrent API calls", e);
                Thread.currentThread().interrupt();
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Completed {} concurrent requests in {}ms", numberOfRequests, (endTime - startTime));
        
        return responses;
    }
    
    public Response validateApiSchema(String sku, String brand, String country) {
        logger.info("Validating API schema for promotional price response");
        
        Response response = getPromotionalPrice(sku, brand, country);
        
        if (response.getStatusCode() == 200) {
            response.then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/promotional-price-response-schema.json"));
        }
        
        return response;
    }
    
    private io.restassured.matcher.RestAssuredMatchers matchesJsonSchemaInClasspath(String schemaPath) {
        return io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath);
    }
    
    public boolean isApiHealthy() {
        try {
            Response response = given()
                .when()
                .timeout(5, TimeUnit.SECONDS)
                .get("/health");
            
            boolean isHealthy = response.getStatusCode() == 200;
            logger.info("API health check: {}", isHealthy ? "HEALTHY" : "UNHEALTHY");
            return isHealthy;
            
        } catch (Exception e) {
            logger.error("API health check failed", e);
            return false;
        }
    }
    
    public void clearCache() {
        logger.info("Clearing API cache");
        
        try {
            given()
                .header("Authorization", "Bearer " + apiKey)
                .when()
                .delete("/v1/promotions/cache")
                .then()
                .statusCode(204);
            
            logger.info("API cache cleared successfully");
        } catch (Exception e) {
            logger.warn("Failed to clear API cache", e);
        }
    }
}