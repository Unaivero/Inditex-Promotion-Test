package com.inditex.test.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
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
@PactTestFor(providerName = "inventory-service")
public class InventoryServiceContractTest {
    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceContractTest.class);
    
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer test-token");
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getProductInventoryInStock(PactDslWithProvider builder) {
        return builder
            .given("product is in stock")
            .uponReceiving("a request for product inventory")
            .path("/v1/inventory/check")
            .method("GET")
            .query("sku=ZARA001ES&location=ES")
            .headers(headers)
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                {
                    "sku": "ZARA001ES",
                    "location": "ES",
                    "availableQuantity": 50,
                    "reservedQuantity": 5,
                    "totalQuantity": 55,
                    "inStock": true,
                    "lowStock": false,
                    "lastUpdated": "2024-06-06T10:00:00Z",
                    "warehouse": {
                        "id": "ES-MAD-001",
                        "name": "Madrid Central Warehouse",
                        "location": "Madrid, Spain"
                    }
                }
                """)
            .toPact();
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact getProductInventoryOutOfStock(PactDslWithProvider builder) {
        return builder
            .given("product is out of stock")
            .uponReceiving("a request for out of stock product inventory")
            .path("/v1/inventory/check")
            .method("GET")
            .query("sku=ZARA999ES&location=ES")
            .headers(headers)
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                {
                    "sku": "ZARA999ES",
                    "location": "ES",
                    "availableQuantity": 0,
                    "reservedQuantity": 0,
                    "totalQuantity": 0,
                    "inStock": false,
                    "lowStock": false,
                    "lastUpdated": "2024-06-06T10:00:00Z",
                    "warehouse": {
                        "id": "ES-MAD-001",
                        "name": "Madrid Central Warehouse",
                        "location": "Madrid, Spain"
                    },
                    "expectedRestockDate": "2024-06-15T00:00:00Z"
                }
                """)
            .toPact();
    }

    @Pact(consumer = "promotions-ui-tests")
    public RequestResponsePact reserveInventorySuccess(PactDslWithProvider builder) {
        return builder
            .given("product has sufficient inventory")
            .uponReceiving("a request to reserve inventory")
            .path("/v1/inventory/reserve")
            .method("POST")
            .headers(headers)
            .body("""
                {
                    "sku": "ZARA001ES",
                    "location": "ES",
                    "quantity": 2,
                    "customerId": "customer-123",
                    "reservationTtl": 1800
                }
                """)
            .willRespondWith()
            .status(201)
            .headers(Map.of("Content-Type", "application/json"))
            .body("""
                {
                    "reservationId": "res-12345",
                    "sku": "ZARA001ES",
                    "location": "ES",
                    "quantity": 2,
                    "customerId": "customer-123",
                    "reservedAt": "2024-06-06T10:00:00Z",
                    "expiresAt": "2024-06-06T10:30:00Z",
                    "status": "RESERVED",
                    "remainingAvailable": 48
                }
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getProductInventoryInStock")
    void testGetProductInventoryInStock(MockServer mockServer) {
        logger.info("Testing inventory check for in-stock product");
        
        Response response = given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .queryParam("sku", "ZARA001ES")
            .queryParam("location", "ES")
        .when()
            .get("/v1/inventory/check")
        .then()
            .statusCode(200)
            .body("sku", equalTo("ZARA001ES"))
            .body("location", equalTo("ES"))
            .body("availableQuantity", equalTo(50))
            .body("inStock", equalTo(true))
            .body("lowStock", equalTo(false))
            .extract().response();

        // Additional assertions
        assertEquals(55, response.jsonPath().getInt("totalQuantity"));
        assertEquals("ES-MAD-001", response.jsonPath().getString("warehouse.id"));
        
        logger.info("In-stock inventory contract test passed");
    }

    @Test
    @PactTestFor(pactMethod = "getProductInventoryOutOfStock")
    void testGetProductInventoryOutOfStock(MockServer mockServer) {
        logger.info("Testing inventory check for out-of-stock product");
        
        Response response = given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .queryParam("sku", "ZARA999ES")
            .queryParam("location", "ES")
        .when()
            .get("/v1/inventory/check")
        .then()
            .statusCode(200)
            .body("sku", equalTo("ZARA999ES"))
            .body("availableQuantity", equalTo(0))
            .body("inStock", equalTo(false))
            .extract().response();

        // Check expected restock date is present
        assertNotNull(response.jsonPath().getString("expectedRestockDate"));
        
        logger.info("Out-of-stock inventory contract test passed");
    }

    @Test
    @PactTestFor(pactMethod = "reserveInventorySuccess")
    void testReserveInventorySuccess(MockServer mockServer) {
        logger.info("Testing inventory reservation");
        
        String requestBody = """
            {
                "sku": "ZARA001ES",
                "location": "ES",
                "quantity": 2,
                "customerId": "customer-123",
                "reservationTtl": 1800
            }
            """;

        Response response = given()
            .baseUri(mockServer.getUrl())
            .headers(headers)
            .body(requestBody)
        .when()
            .post("/v1/inventory/reserve")
        .then()
            .statusCode(201)
            .body("reservationId", startsWith("res-"))
            .body("sku", equalTo("ZARA001ES"))
            .body("quantity", equalTo(2))
            .body("status", equalTo("RESERVED"))
            .body("remainingAvailable", equalTo(48))
            .extract().response();

        // Verify reservation details
        assertNotNull(response.jsonPath().getString("reservedAt"));
        assertNotNull(response.jsonPath().getString("expiresAt"));
        
        logger.info("Inventory reservation contract test passed");
    }
}
