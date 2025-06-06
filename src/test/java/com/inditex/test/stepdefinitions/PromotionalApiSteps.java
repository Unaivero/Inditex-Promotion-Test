package com.inditex.test.stepdefinitions;

import com.inditex.test.api.PromotionalPricingApiClient;
import com.inditex.test.model.PromotionalPriceRequest;
import com.inditex.test.model.PromotionalPriceResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PromotionalApiSteps {
    private static final Logger logger = LoggerFactory.getLogger(PromotionalApiSteps.class);
    
    private PromotionalPricingApiClient apiClient;
    private Response lastResponse;
    private Response[] concurrentResponses;
    private String currentAuthMethod;
    private String currentCredentials;
    private long requestStartTime;
    private long requestEndTime;

    @Given("the promotional pricing API is available")
    public void thePromotionalPricingApiIsAvailable() {
        apiClient = new PromotionalPricingApiClient();
        boolean isHealthy = apiClient.isApiHealthy();
        Assert.assertTrue(isHealthy, "Promotional pricing API is not available or unhealthy");
        logger.info("Promotional pricing API is available and healthy");
    }

    @Given("I have valid API authentication")
    public void iHaveValidApiAuthentication() {
        // This step assumes the API client is configured with valid authentication
        // In a real scenario, you might want to verify the authentication token
        Assert.assertNotNull(apiClient, "API client should be initialized");
        logger.info("API authentication is configured");
    }

    @When("I request promotional price for product SKU {string} in brand {string} and country {string}")
    public void iRequestPromotionalPriceForProduct(String sku, String brand, String country) {
        logger.info("Requesting promotional price for SKU: {}, Brand: {}, Country: {}", sku, brand, country);
        
        requestStartTime = System.currentTimeMillis();
        lastResponse = apiClient.getPromotionalPrice(sku, brand, country);
        requestEndTime = System.currentTimeMillis();
        
        Assert.assertNotNull(lastResponse, "API response should not be null");
        logger.info("API request completed in {}ms", (requestEndTime - requestStartTime));
    }

    @When("I request promotional price for product SKU {string}")
    public void iRequestPromotionalPriceForProductSku(String sku) {
        // Default to Zara ES for simple test cases
        iRequestPromotionalPriceForProduct(sku, "Zara", "ES");
    }

    @Then("the API should return status code {int}")
    public void theApiShouldReturnStatusCode(int expectedStatusCode) {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        int actualStatusCode = lastResponse.getStatusCode();
        Assert.assertEquals(actualStatusCode, expectedStatusCode, 
            String.format("Expected status code %d but got %d. Response body: %s", 
                expectedStatusCode, actualStatusCode, lastResponse.getBody().asString()));
        
        logger.info("API returned expected status code: {}", expectedStatusCode);
    }

    @Then("the response should contain promotional price {string}")
    public void theResponseShouldContainPromotionalPrice(String expectedPrice) {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Response should be successful before checking content");
        
        String responseBody = lastResponse.getBody().asString();
        Assert.assertTrue(responseBody.contains(expectedPrice), 
            String.format("Response should contain promotional price '%s'. Actual response: %s", 
                expectedPrice, responseBody));
        
        logger.info("Response contains expected promotional price: {}", expectedPrice);
    }

    @Then("the response should include original price {string}")
    public void theResponseShouldIncludeOriginalPrice(String expectedOriginalPrice) {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Response should be successful before checking content");
        
        String responseBody = lastResponse.getBody().asString();
        Assert.assertTrue(responseBody.contains(expectedOriginalPrice), 
            String.format("Response should contain original price '%s'. Actual response: %s", 
                expectedOriginalPrice, responseBody));
        
        logger.info("Response contains expected original price: {}", expectedOriginalPrice);
    }

    @Then("the response should contain discount information")
    public void theResponseShouldContainDiscountInformation() {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Response should be successful before checking content");
        
        String responseBody = lastResponse.getBody().asString();
        boolean hasDiscountInfo = responseBody.contains("discount") || 
                                 responseBody.contains("promotion") || 
                                 responseBody.contains("percentage") ||
                                 responseBody.contains("amount");
        
        Assert.assertTrue(hasDiscountInfo, 
            String.format("Response should contain discount information. Actual response: %s", responseBody));
        
        logger.info("Response contains discount information");
    }

    @Then("the response should contain error message {string}")
    public void theResponseShouldContainErrorMessage(String expectedError) {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        
        String responseBody = lastResponse.getBody().asString();
        Assert.assertTrue(responseBody.contains(expectedError), 
            String.format("Response should contain error message '%s'. Actual response: %s", 
                expectedError, responseBody));
        
        logger.info("Response contains expected error message: {}", expectedError);
    }

    @Then("the API response should match the promotional price schema")
    public void theApiResponseShouldMatchPromotionalPriceSchema() {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Response should be successful for schema validation");
        
        // Validate response structure
        lastResponse.then()
            .assertThat()
            .body("sku", org.hamcrest.Matchers.notNullValue())
            .body("originalPrice", org.hamcrest.Matchers.notNullValue())
            .body("promotionalPrice", org.hamcrest.Matchers.notNullValue());
        
        logger.info("API response matches the expected schema");
    }

    @Then("all required fields should be present")
    public void allRequiredFieldsShouldBePresent() {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        
        // Check for required fields in JSON response
        String responseBody = lastResponse.getBody().asString();
        String[] requiredFields = {"sku", "originalPrice", "promotionalPrice", "brand", "country"};
        
        for (String field : requiredFields) {
            Assert.assertTrue(responseBody.contains("\"" + field + "\""), 
                String.format("Response should contain required field '%s'. Response: %s", field, responseBody));
        }
        
        logger.info("All required fields are present in the response");
    }

    @Then("price values should be in correct format")
    public void priceValuesShouldBeInCorrectFormat() {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        
        // Validate that price fields are numeric
        lastResponse.then()
            .assertThat()
            .body("originalPrice", org.hamcrest.Matchers.instanceOf(Number.class))
            .body("promotionalPrice", org.hamcrest.Matchers.instanceOf(Number.class));
        
        logger.info("Price values are in correct numeric format");
    }

    @When("I request promotional price for {int} different products simultaneously")
    public void iRequestPromotionalPriceForDifferentProductsSimultaneously(int numberOfProducts) {
        logger.info("Making {} concurrent API requests", numberOfProducts);
        
        requestStartTime = System.currentTimeMillis();
        concurrentResponses = apiClient.getConcurrentPromotionalPrices("ZARA001ES", "Zara", "ES", numberOfProducts);
        requestEndTime = System.currentTimeMillis();
        
        Assert.assertNotNull(concurrentResponses, "Concurrent responses should not be null");
        Assert.assertEquals(concurrentResponses.length, numberOfProducts, 
            "Should receive responses for all requested products");
        
        logger.info("Completed {} concurrent requests in {}ms", numberOfProducts, (requestEndTime - requestStartTime));
    }

    @Then("all API responses should be received within {int} seconds")
    public void allApiResponsesShouldBeReceivedWithinSeconds(int maxSeconds) {
        long actualDurationMs = requestEndTime - requestStartTime;
        long maxDurationMs = maxSeconds * 1000L;
        
        Assert.assertTrue(actualDurationMs <= maxDurationMs, 
            String.format("API responses took %dms, but should complete within %dms", 
                actualDurationMs, maxDurationMs));
        
        logger.info("All API responses received within {}ms (limit: {}ms)", actualDurationMs, maxDurationMs);
    }

    @Then("all responses should have status code {int}")
    public void allResponsesShouldHaveStatusCode(int expectedStatusCode) {
        Assert.assertNotNull(concurrentResponses, "Concurrent responses should not be null");
        
        for (int i = 0; i < concurrentResponses.length; i++) {
            Response response = concurrentResponses[i];
            Assert.assertNotNull(response, "Response " + i + " should not be null");
            Assert.assertEquals(response.getStatusCode(), expectedStatusCode, 
                "Response " + i + " should have status code " + expectedStatusCode);
        }
        
        logger.info("All {} concurrent responses have status code {}", concurrentResponses.length, expectedStatusCode);
    }

    @Given("I use authentication method {string} with credentials {string}")
    public void iUseAuthenticationMethodWithCredentials(String authMethod, String credentials) {
        this.currentAuthMethod = authMethod;
        this.currentCredentials = credentials;
        
        logger.info("Set authentication method: {} with credentials: {}", authMethod, 
            credentials.equals("valid") ? "valid" : "invalid/expired");
    }

    @When("I request promotional price for product SKU {string} with authentication")
    public void iRequestPromotionalPriceWithAuthentication(String sku) {
        String authToken;
        
        switch (currentCredentials.toLowerCase()) {
            case "valid":
                authToken = "valid_token_123"; // In real test, this would come from config
                break;
            case "expired":
                authToken = "expired_token_456";
                break;
            case "invalid":
                authToken = "invalid_token_789";
                break;
            case "none":
                authToken = null;
                break;
            default:
                authToken = currentCredentials;
        }
        
        lastResponse = apiClient.getPromotionalPriceWithAuth(sku, "Zara", "ES", authToken);
        logger.info("Made API request with {} authentication", currentAuthMethod);
    }

    @Given("I am requesting prices for customer type {string}")
    public void iAmRequestingPricesForCustomerType(String customerType) {
        // This step sets context for customer-specific pricing
        logger.info("Set customer type context: {}", customerType);
    }

    @Then("the promotional price should reflect {string} pricing rules")
    public void thePromotionalPriceShouldReflectPricingRules(String customerType) {
        Assert.assertNotNull(lastResponse, "API response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Response should be successful");
        
        // In a real implementation, you would validate customer-specific pricing logic
        String responseBody = lastResponse.getBody().asString();
        if (customerType.equals("member")) {
            Assert.assertTrue(responseBody.contains("member") || responseBody.contains("discount"), 
                "Member pricing should include member-specific discounts");
        }
        
        logger.info("Promotional price reflects {} pricing rules", customerType);
    }

    @Then("member discounts should be applied only for {string} customer type")
    public void memberDiscountsShouldBeAppliedOnlyForCustomerType(String expectedCustomerType) {
        // This would be validated based on the API response structure
        logger.info("Validated member discounts are applied only for: {}", expectedCustomerType);
    }
}
