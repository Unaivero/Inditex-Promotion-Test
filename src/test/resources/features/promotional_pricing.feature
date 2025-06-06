Feature: Validate Promotional Pricing Across Brands and Regions
  As a user,
  I want to verify that promotional prices and discounts are correctly applied
  for different Inditex brands, countries, languages, and customer types
  across our comprehensive test data set of 500+ promotional scenarios.

  Background:
    Given I am on the homepage for brand "<brand>" and country "<country>" in "<language>" language
    And the test data manager is initialized with comprehensive promotion data

  @smoke @comprehensive
  Scenario: Verify promotional pricing with comprehensive test data
    Given I load the comprehensive promotional test data
    When I test promotional pricing for all brands and regions
    Then all promotional prices should be calculated correctly
    And discount percentages should be applied accurately
    And regional currency and tax rules should be respected
    And customer-type specific discounts should be honored

  @regression @data-driven
  Scenario Outline: Verify promotional pricing for comprehensive scenarios
    Given I navigate to a product with an active promotion
    And I am a "<customer_type>" customer from "<country>" using "<language>" language
    When I view the product details for "<product_name>" with SKU "<sku>"
    Then the promotional price should match expected price "<promotional_price_expected>"
    And the original price should be "<original_price>"
    And the discount type "<discount_type>" with value "<discount_value>" should be applied correctly
    And the promotion name "<promotion_name>" should be displayed
    And the campaign type "<campaign_type>" should be tracked

    Examples: Comprehensive Promotional Scenarios
      # This will be populated dynamically from comprehensive_promotions_data.csv
      # containing 500+ scenarios across all brands, countries, and customer types

  @seasonal @campaigns
  Scenario Outline: Verify seasonal campaign promotional pricing
    Given I navigate to a seasonal campaign product
    And I am a "<customer_type>" customer from "<country>"
    And the campaign is active between "<start_date>" and "<end_date>"
    When I view the product details for "<product_name>" with SKU "<sku>"
    Then the seasonal promotional price should match expected price "<promotional_price_expected>"
    And the campaign type should be "seasonal"
    And the target audience "<target_audience>" should be respected
    And exclusions "<exclusions>" should be applied if applicable

    Examples: Seasonal Campaign Scenarios
      # This will be populated dynamically from seasonal_campaigns_2024.csv
      # containing seasonal promotions across Spring, Summer, Back-to-School, 
      # Halloween, Black Friday, Christmas, New Year, Valentine's, and End-of-Season

  @bulk @multi-buy
  Scenario Outline: Verify bulk discount and multi-buy promotional pricing
    Given I navigate to a bulk discount product
    And I am a "<customer_type>" customer from "<country>"
    When I add the required quantity for bulk discount promotion
    And I view the product details for "<product_name>" with SKU "<sku>"
    Then the bulk promotional price should match expected price "<promotional_price_expected>"
    And the minimum quantity "<min_quantity>" requirement should be enforced
    And the maximum quantity "<max_quantity>" limit should be respected
    And the bundle description "<bundle_description>" should be displayed

    Examples: Bulk Discount Scenarios
      # This will be populated dynamically from bulk_discount_scenarios.csv
      # containing multi-buy, bundle deals, and volume discount scenarios

  Scenario Outline: Verify adding a promotional item to cart reflects correct pricing
    Given I add product SKU "<sku>" with an active promotion to the shopping cart
    And I am a "<customer_type>" customer
    When I proceed to the shopping cart page
    Then the item price in the cart should reflect the promotional discount for SKU "<sku>"
    And the cart total should be calculated correctly including the discount

    Examples: Cart Verification
      | brand   | country | language | customer_type | sku          | promotion_details         |
      | Zara    | GB      | en       | member        | ZARA004GB    | Loyalty Discount 25%      |
      | Bershka | IT      | it       | guest         | BSK003IT     | App Exclusive 10% Off     |
      | Zara    | US      | en       | member        | ZARA005US    | Black Friday 30%          |
