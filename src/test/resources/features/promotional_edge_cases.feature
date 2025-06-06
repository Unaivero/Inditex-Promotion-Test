Feature: Promotional Edge Cases and Negative Testing
  As a QA Engineer
  I want to validate that the promotional system handles edge cases correctly
  So that users receive appropriate error messages and the system remains stable

  Background:
    Given I am on the homepage for brand "Zara" and country "ES" in "Spanish" language

  @negative @edge_cases
  Scenario Outline: Invalid promotion code handling
    Given I navigate to a product with SKU "<sku>"
    When I try to apply promotion code "<invalid_code>"
    Then I should see error message "<expected_error>"
    And the original price should remain unchanged

    Examples:
      | sku       | invalid_code | expected_error                    |
      | ZARA001ES | INVALID123   | Invalid promotion code            |
      | ZARA001ES | EXPIRED2022  | Promotion code has expired        |
      | ZARA001ES | WRONGBRAND   | Promotion not valid for this item |
      | ZARA001ES |              | Please enter a promotion code     |
      | ZARA001ES | <script>     | Invalid characters in code        |

  @negative @boundary
  Scenario Outline: Boundary value testing for discount percentages
    Given I navigate to a product with SKU "<sku>"
    When I apply a promotion with discount percentage "<discount_percentage>"
    Then the system should handle the discount "<expected_behavior>"

    Examples:
      | sku       | discount_percentage | expected_behavior |
      | ZARA001ES | 0                  | No discount applied |
      | ZARA001ES | 100                | Full discount applied |
      | ZARA001ES | 101                | Error: Invalid discount |
      | ZARA001ES | -10                | Error: Invalid discount |
      | ZARA001ES | 99.99              | Maximum valid discount |

  @negative @inventory
  Scenario: Out of stock promotional item
    Given I navigate to a product with SKU "ZARA999ES" that is out of stock
    When I try to add the promotional item to cart
    Then I should see message "This item is currently out of stock"
    And the add to cart button should be disabled

  @negative @quantity_limits
  Scenario: Exceeding maximum promotional quantity
    Given I navigate to a product with SKU "ZARA001ES" with promotion limit of 2 items
    When I try to add 3 items to cart
    Then I should see message "Maximum 2 items allowed for this promotion"
    And only 2 items should be added to cart

  @negative @expired_promotion
  Scenario: Accessing expired promotion
    Given I navigate to a product with SKU "ZARA001ES"
    And the promotion has expired on "2023-12-31"
    When I view the product details page
    Then the promotional price should not be displayed
    And only the original price should be visible

  @negative @customer_segment
  Scenario: Member-only promotion accessed by guest
    Given I am a "guest" customer
    And I navigate to a product with SKU "ZARA001ES" with member-only promotion
    When I view the product details page
    Then I should see message "Sign in to access member exclusive prices"
    And the promotional price should not be displayed

  @negative @minimum_order
  Scenario: Promotion with minimum order requirement not met
    Given I navigate to a product with SKU "ZARA001ES" with minimum order of 50 EUR
    When I add the item worth 30 EUR to cart
    And I proceed to checkout
    Then I should see message "Minimum order of 50 EUR required for this promotion"
    And the promotional discount should not be applied

  @negative @concurrent_promotions
  Scenario: Multiple conflicting promotions on same item
    Given I navigate to a product with SKU "ZARA001ES"
    When I try to apply multiple promotion codes "SUMMER20" and "STUDENT10"
    Then I should see message "Only one promotion can be applied per item"
    And the higher discount promotion should be automatically selected

  @negative @payment_failure
  Scenario: Payment failure during promotional checkout
    Given I have promotional items in cart worth 100 EUR with 20% discount
    When the payment process fails during checkout
    Then the promotional prices should be preserved in cart
    And I should be able to retry payment with same discounts

  @negative @session_timeout
  Scenario: Session timeout with promotional items in cart
    Given I have promotional items in cart
    When my session expires after 30 minutes of inactivity
    And I return to the cart page
    Then I should see message "Your session has expired"
    And I should be prompted to re-validate promotional eligibility