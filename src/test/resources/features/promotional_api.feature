Feature: Promotional Pricing API Testing
  As a QA Engineer
  I want to validate promotional pricing APIs
  So that backend services correctly calculate and return promotional prices

  Background:
    Given the promotional pricing API is available
    And I have valid API authentication

  @api @smoke
  Scenario Outline: Get promotional price for valid product
    When I request promotional price for product SKU "<sku>" in brand "<brand>" and country "<country>"
    Then the API should return status code 200
    And the response should contain promotional price "<expected_price>"
    And the response should include original price "<original_price>"
    And the response should contain discount information

    Examples:
      | sku       | brand   | country | expected_price | original_price |
      | ZARA001ES | Zara    | ES      | 39.95         | 49.95          |
      | BRSK001ES | Bershka | ES      | 17.95         | 19.95          |
      | ZARA001US | Zara    | US      | 45.99         | 59.99          |

  @api @negative
  Scenario Outline: API error handling for invalid requests
    When I request promotional price for product SKU "<sku>" in brand "<brand>" and country "<country>"
    Then the API should return status code <expected_status>
    And the response should contain error message "<expected_error>"

    Examples:
      | sku        | brand   | country | expected_status | expected_error              |
      | INVALID    | Zara    | ES      | 404            | Product not found           |
      | ZARA001ES  | Invalid | ES      | 400            | Invalid brand               |
      | ZARA001ES  | Zara    | XX      | 400            | Invalid country code        |
      |            | Zara    | ES      | 400            | SKU is required             |

  @api @validation
  Scenario: Promotional price API response schema validation
    When I request promotional price for product SKU "ZARA001ES" in brand "Zara" and country "ES"
    Then the API response should match the promotional price schema
    And all required fields should be present
    And price values should be in correct format

  @api @performance
  Scenario: Promotional price API performance
    When I request promotional price for 10 different products simultaneously
    Then all API responses should be received within 2 seconds
    And all responses should have status code 200

  @api @authentication
  Scenario Outline: API authentication scenarios
    Given I use authentication method "<auth_method>" with credentials "<credentials>"
    When I request promotional price for product SKU "ZARA001ES"
    Then the API should return status code <expected_status>

    Examples:
      | auth_method | credentials | expected_status |
      | valid_token | valid       | 200            |
      | expired     | expired     | 401            |
      | invalid     | invalid     | 401            |
      | none        | none        | 401            |

  @api @business_rules
  Scenario: Customer type specific pricing
    Given I am requesting prices for customer type "<customer_type>"
    When I request promotional price for product SKU "ZARA001ES"
    Then the promotional price should reflect "<customer_type>" pricing rules
    And member discounts should be applied only for "member" customer type

    Examples:
      | customer_type |
      | guest        |
      | member       |
      | vip          |

  @api @regional
  Scenario: Regional promotion validation
    When I request promotional price for product SKU "ZARA001ES" in country "<country>"
    Then the promotional price should be in "<currency>"
    And tax calculations should follow "<country>" regulations
    And promotional rules should match "<country>" specific offers

    Examples:
      | country | currency |
      | ES      | EUR      |
      | US      | USD      |
      | GB      | GBP      |
      | FR      | EUR      |

  @api @concurrent
  Scenario: Concurrent promotional price requests
    Given multiple users are requesting promotional prices simultaneously
    When 50 concurrent requests are made for the same product SKU "ZARA001ES"
    Then all requests should complete successfully
    And the promotional price should be consistent across all responses
    And no race conditions should occur

  @api @caching
  Scenario: Promotional price caching behavior
    Given promotional prices are cached for performance
    When I request promotional price for product SKU "ZARA001ES"
    Then the first request should hit the database
    When I request the same promotional price again within cache TTL
    Then the second request should be served from cache
    And response time should be significantly faster

  @api @inventory
  Scenario: Promotional price with inventory check
    When I request promotional price for product SKU "ZARA001ES" with inventory check
    Then the API should return promotional price only if item is in stock
    And out of stock items should return appropriate status
    And stock quantity should be included in response

  @api @bulk
  Scenario: Bulk promotional price requests
    When I request promotional prices for multiple products in a single API call
    Then the API should return prices for all valid products
    And invalid products should be clearly identified in response
    And response should maintain product order from request