package com.inditex.test.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.inditex.test.api.PromotionalPricingApiClient;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "promotional-pricing-api")
public class PromotionalPricingContractTest {
    private static final Logger logger = LoggerFactory.getLogger(PromotionalPricingContractTest.class);
    
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getPromotionalPriceValidProduct(PactDslWithProvider builder) {
        return builder
            .given("product exists with active promotion")
            .uponReceiving("a request for promotional price")
            .path("/v1/promotions/pricing")
            .method("GET")
            .query("sku=ZARA001ES&brand=Zara&country=ES")
            .headers(headers)
            .willRespondWith()
            .status(200)
            .headers(headers)
            .body("""
                {
                    "sku": "ZARA001ES",
                    "brand": "Zara",
                    "country": "ES",
                    "productName": "Summer Dress",
                    "originalPrice": 49.95,
                    "promotionalPrice": 39.96,
                    "discountPercentage": 20,
                    "discountAmount": 9.99,
                    "promotionName": "Summer Sale 20% Off",
                    "promotionType": "PERCENTAGE",
                    "promotionStartDate": "2024-06-01",
                    "promotionEndDate": "2024-08-31",
                    "currency": "EUR",
                    "isActive": true,
                    "customerEligible": true,
                    "minimumOrderAmount": 0,
                    "maximumDiscountAmount": null,
                    "availableQuantity": 50,
                    "inStock": true
                }
                """)
            .toPact();
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getPromotionalPriceInvalidProduct(PactDslWithProvider builder) {
        return builder
            .given("product does not exist")
            .uponReceiving("a request for promotional price for invalid product")
            .path("/v1/promotions/pricing")
            .method("GET")
            .query("sku=INVALID001&brand=Zara&country=ES")
            .headers(headers)
            .willRespondWith()
            .status(404)
            .headers(headers)
            .body("""
                {
                    "error": "Product not found",
                    "errorCode": "PRODUCT_NOT_FOUND",
                    "message": "Product with SKU 'INVALID001' was not found",
                    "timestamp": "2024-06-06T10:00:00Z",
                    "path": "/v1/promotions/pricing"
                }
                """)
            .toPact();
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getPromotionalPriceExpiredPromotion(PactDslWithProvider builder) {
        return builder
            .given("product exists but promotion is expired")
            .uponReceiving("a request for promotional price for product with expired promotion")
            .path("/v1/promotions/pricing")
            .method("GET")
            .query("sku=ZARA002ES&brand=Zara&country=ES")
            .headers(headers)
            .willRespondWith()
            .status(200)
            .headers(headers)
            .body("""
                {
                    "sku": "ZARA002ES",
                    "brand": "Zara",
                    "country": "ES",
                    "productName": "Winter Coat",
                    "originalPrice": 89.95,
                    "promotionalPrice": 89.95,
                    "discountPercentage": 0,
                    "discountAmount": 0,
                    "promotionName": null,
                    "promotionType": null,
                    "promotionStartDate": null,
                    "promotionEndDate": null,
                    "currency": "EUR",
                    "isActive": false,
                    "customerEligible": false,
                    "minimumOrderAmount": 0,
                    "maximumDiscountAmount": null,
                    "availableQuantity": 25,
                    "inStock": true
                }
                """)
            .toPact();
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getPromotionalPriceMemberOnlyPromotion(PactDslWithProvider builder) {
        return builder
            .given("product exists with member-only promotion")
            .uponReceiving("a request for promotional price for member-only promotion")
            .path("/v1/promotions/pricing")
            .method("GET")
            .query("sku=ZARA003ES&brand=Zara&country=ES&customerType=member")
            .headers(headers)
            .willRespondWith()
            .status(200)
            .headers(headers)
            .body("""
                {
                    "sku": "ZARA003ES",
                    "brand": "Zara",
                    "country": "ES",
                    "productName": "Premium Shirt",
                    "originalPrice": 59.95,
                    "promotionalPrice": 47.96,
                    "discountPercentage": 20,
                    "discountAmount": 11.99,
                    "promotionName": "Member Exclusive 20% Off",
                    "promotionType": "PERCENTAGE",
                    "promotionStartDate": "2024-01-01",
                    "promotionEndDate": "2024-12-31",
                    "currency": "EUR",
                    "isActive": true,
                    "customerEligible": true,
                    "memberOnly": true,
                    "minimumOrderAmount": 0,
                    "maximumDiscountAmount": null,
                    "availableQuantity": 100,
                    "inStock": true
                }
                """)
            .toPact();
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getBulkPromotionalPrices(PactDslWithProvider builder) {
        return builder
            .given("multiple products exist with various promotions")
            .uponReceiving("a bulk request for promotional prices")
            .path("/v1/promotions/pricing/bulk")
            .method("POST")
            .headers(headers)
            .body("""
                {
                    "requests": [
                        {
                            "sku": "ZARA001ES",
                            "brand": "Zara",
                            "country": "ES"
                        },
                        {
                            "sku": "ZARA002ES",
                            "brand": "Zara",
                            "country": "ES"
                        }
                    ]
                }
                """)
            .willRespondWith()
            .status(200)
            .headers(headers)
            .body("""
                {
                    "results": [
                        {
                            "sku": "ZARA001ES",
                            "brand": "Zara",
                            "country": "ES",
                            "originalPrice": 49.95,
                            "promotionalPrice": 39.96,
                            "discountPercentage": 20,
                            "isActive": true
                        },
                        {
                            "sku": "ZARA002ES",
                            "brand": "Zara",
                            "country": "ES",
                            "originalPrice": 89.95,
                            "promotionalPrice": 89.95,
                            "discountPercentage": 0,
                            "isActive": false
                        }
                    ],
                    "totalRequests": 2,
                    "successfulRequests": 2,
                    "failedRequests": 0
                }
                """)
            .toPact();
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getPromotionalPriceUnauthorized(PactDslWithProvider builder) {
        return builder
            .given("request without valid authentication")
            .uponReceiving("a request for promotional price without authentication")
            .path("/v1/promotions/pricing")
            .method("GET")
            .query("sku=ZARA001ES&brand=Zara&country=ES")
            .headers(Map.of("Content-Type", "application/json", "Accept", "application/json"))
            .willRespondWith()
            .status(401)
            .headers(headers)
            .body("""
                {
                    "error": "Unauthorized",
                    "errorCode": "AUTHENTICATION_REQUIRED",
                    "message": "Valid authentication token is required",
                    "timestamp": "2024-06-06T10:00:00Z",
                    "path": "/v1/promotions/pricing"
                }
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getPromotionalPriceValidProduct")
    void testGetPromotionalPriceValidProduct(MockServer mockServer) {
        logger.info("Testing promotional price for valid product contract");
        
        Response response = given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .queryParam("sku", "ZARA001ES")
            .queryParam("brand", "Zara")
            .queryParam("country", "ES")
        .when()
            .get("/v1/promotions/pricing")
        .then()
            .statusCode(200)
            .body("sku", equalTo("ZARA001ES"))
            .body("brand", equalTo("Zara"))
            .body("country", equalTo("ES"))
            .body("originalPrice", equalTo(49.95f))
            .body("promotionalPrice", equalTo(39.96f))
            .body("discountPercentage", equalTo(20))
            .body("isActive", equalTo(true))
            .body("inStock", equalTo(true))
            .extract().response();

        // Additional assertions
        assertEquals("Summer Dress", response.jsonPath().getString("productName"));
        assertEquals("Summer Sale 20% Off", response.jsonPath().getString("promotionName"));
        assertEquals("PERCENTAGE", response.jsonPath().getString("promotionType"));
        assertTrue(response.jsonPath().getBoolean("customerEligible"));
        
        logger.info("Valid product contract test passed");
    }

    @Test
    @PactTestFor(pactMethod = "getPromotionalPriceInvalidProduct")
    void testGetPromotionalPriceInvalidProduct(MockServer mockServer) {
        logger.info("Testing promotional price for invalid product contract");
        
        given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .queryParam("sku", "INVALID001")
            .queryParam("brand", "Zara")
            .queryParam("country", "ES")
        .when()
            .get("/v1/promotions/pricing")
        .then()
            .statusCode(404)
            .body("error", equalTo("Product not found"))
            .body("errorCode", equalTo("PRODUCT_NOT_FOUND"))
            .body("message", containsString("INVALID001"));
        
        logger.info("Invalid product contract test passed");
    }

    @Test
    @PactTestFor(pactMethod = "getPromotionalPriceExpiredPromotion")
    void testGetPromotionalPriceExpiredPromotion(MockServer mockServer) {
        logger.info("Testing promotional price for expired promotion contract");
        
        Response response = given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .queryParam("sku", "ZARA002ES")
            .queryParam("brand", "Zara")
            .queryParam("country", "ES")
        .when()
            .get("/v1/promotions/pricing")
        .then()
            .statusCode(200)
            .body("sku", equalTo("ZARA002ES"))
            .body("originalPrice", equalTo(89.95f))
            .body("promotionalPrice", equalTo(89.95f))
            .body("discountPercentage", equalTo(0))
            .body("isActive", equalTo(false))
            .extract().response();

        // Verify no active promotion
        assertNull(response.jsonPath().getString("promotionName"));
        assertFalse(response.jsonPath().getBoolean("customerEligible"));
        
        logger.info("Expired promotion contract test passed");
    }

    @Test
    @PactTestFor(pactMethod = "getPromotionalPriceMemberOnlyPromotion")
    void testGetPromotionalPriceMemberOnlyPromotion(MockServer mockServer) {
        logger.info("Testing promotional price for member-only promotion contract");
        
        Response response = given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .queryParam("sku", "ZARA003ES")
            .queryParam("brand", "Zara")
            .queryParam("country", "ES")
            .queryParam("customerType", "member")
        .when()
            .get("/v1/promotions/pricing")
        .then()
            .statusCode(200)
            .body("sku", equalTo("ZARA003ES"))
            .body("originalPrice", equalTo(59.95f))
            .body("promotionalPrice", equalTo(47.96f))
            .body("discountPercentage", equalTo(20))
            .body("isActive", equalTo(true))
            .body("memberOnly", equalTo(true))
            .extract().response();

        assertEquals("Member Exclusive 20% Off", response.jsonPath().getString("promotionName"));
        assertTrue(response.jsonPath().getBoolean("customerEligible"));
        
        logger.info("Member-only promotion contract test passed");
    }

    @Test
    @PactTestFor(pactMethod = "getBulkPromotionalPrices")
    void testGetBulkPromotionalPrices(MockServer mockServer) {
        logger.info("Testing bulk promotional prices contract");
        
        String requestBody = """
            {
                "requests": [
                    {
                        "sku": "ZARA001ES",
                        "brand": "Zara",
                        "country": "ES"
                    },
                    {
                        "sku": "ZARA002ES",
                        "brand": "Zara",
                        "country": "ES"
                    }
                ]
            }
            """;

        Response response = given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .body(requestBody)
        .when()
            .post("/v1/promotions/pricing/bulk")
        .then()
            .statusCode(200)
            .body("totalRequests", equalTo(2))
            .body("successfulRequests", equalTo(2))
            .body("failedRequests", equalTo(0))
            .body("results", hasSize(2))
            .body("results[0].sku", equalTo("ZARA001ES"))
            .body("results[0].isActive", equalTo(true))
            .body("results[1].sku", equalTo("ZARA002ES"))
            .body("results[1].isActive", equalTo(false))
            .extract().response();

        // Verify first result has promotion
        assertEquals(39.96f, response.jsonPath().getFloat("results[0].promotionalPrice"));
        assertEquals(20, response.jsonPath().getInt("results[0].discountPercentage"));
        
        // Verify second result has no promotion
        assertEquals(89.95f, response.jsonPath().getFloat("results[1].promotionalPrice"));
        assertEquals(0, response.jsonPath().getInt("results[1].discountPercentage"));
        
        logger.info("Bulk promotional prices contract test passed");
    }

    @Test
    @PactTestFor(pactMethod = "getPromotionalPriceUnauthorized")
    void testGetPromotionalPriceUnauthorized(MockServer mockServer) {
        logger.info("Testing promotional price unauthorized access contract");
        
        given()
            .baseUri(mockServer.getUrl())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .queryParam("sku", "ZARA001ES")
            .queryParam("brand", "Zara")
            .queryParam("country", "ES")
        .when()
            .get("/v1/promotions/pricing")
        .then()
            .statusCode(401)
            .body("error", equalTo("Unauthorized"))
            .body("errorCode", equalTo("AUTHENTICATION_REQUIRED"))
            .body("message", containsString("authentication token"));
        
        logger.info("Unauthorized access contract test passed");
    }
}
