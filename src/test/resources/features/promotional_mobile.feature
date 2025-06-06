Feature: Mobile and Responsive Promotional Testing
  As a QA Engineer
  I want to validate promotional pricing on mobile devices and responsive layouts
  So that mobile users have the same promotional experience as desktop users

  Background:
    Given I am testing on a mobile device

  @mobile @responsive
  Scenario Outline: Mobile promotional price display
    Given I am using device "<device>" with screen size "<screen_size>"
    And I am on the homepage for brand "Zara" and country "ES" in "Spanish" language
    When I navigate to a product with SKU "<sku>" on mobile
    Then the promotional price should be clearly visible on mobile
    And the original price should be displayed with strikethrough on mobile
    And the discount badge should be prominently shown on mobile
    And all price elements should be touch-friendly

    Examples:
      | device         | screen_size | sku       |
      | iPhone 12      | 390x844     | ZARA001ES |
      | Samsung Galaxy | 360x740     | ZARA001ES |
      | iPad Mini      | 768x1024    | ZARA001ES |
      | iPhone SE      | 375x667     | ZARA001ES |

  @mobile @touch
  Scenario: Mobile touch interactions for promotional items
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am on a product page with SKU "ZARA001ES" with active promotion
    When I tap on the promotional price element
    Then the promotion details should expand
    When I tap on "Add to Cart" button
    Then the item should be added to cart with promotional price
    And a mobile-optimized confirmation should appear

  @mobile @cart
  Scenario: Mobile cart experience with promotional items
    Given I am using device "iPhone 12" with screen size "390x844"
    And I have promotional items in my mobile cart
    When I view the cart on mobile
    Then promotional prices should be clearly visible
    And the total savings should be highlighted
    And swipe gestures should work for cart items
    And the checkout button should be easily accessible

  @responsive @breakpoints
  Scenario Outline: Responsive design breakpoints for promotional content
    Given I am testing responsive design at breakpoint "<breakpoint>"
    And I am on a product page with active promotion
    Then promotional content should adapt to "<breakpoint>" layout
    And all promotional elements should remain functional
    And text should be readable without horizontal scrolling
    And buttons should be appropriately sized for the viewport

    Examples:
      | breakpoint |
      | 320px      |
      | 768px      |
      | 1024px     |
      | 1200px     |
      | 1920px     |

  @mobile @orientation
  Scenario: Mobile orientation changes with promotional content
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am on a product page with active promotion
    When I rotate the device to landscape orientation
    Then promotional elements should adjust to landscape layout
    And all promotional information should remain visible
    When I rotate back to portrait orientation
    Then promotional elements should return to portrait layout
    And no data should be lost during orientation changes

  @mobile @performance
  Scenario: Mobile performance with promotional images and content
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am on a page with multiple promotional products
    When I scroll through the promotional product list
    Then images should load progressively
    And scrolling should be smooth without lag
    And promotional badges should load within 2 seconds
    And the page should be interactive within 3 seconds

  @mobile @navigation
  Scenario: Mobile navigation with promotional filters
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am on the product listing page
    When I open the mobile filter menu
    And I select "Items on Sale" filter
    Then only promotional items should be displayed
    And the filter should be applied without page reload
    And promotional badges should be visible on filtered items

  @mobile @accessibility
  Scenario: Mobile accessibility for promotional content
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am using screen reader on mobile
    When I navigate to a promotional product
    Then promotional price should be announced correctly
    And discount information should be accessible via screen reader
    And all promotional buttons should have proper aria labels
    And focus indicators should be visible for promotional elements

  @mobile @forms
  Scenario: Mobile promotional code entry
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am on the cart page with items
    When I tap on "Enter promotional code" field
    Then the mobile keyboard should appear
    And the input field should be properly focused
    When I enter promotional code "SUMMER20"
    Then the code should be applied without page refresh
    And promotional discount should be immediately visible

  @mobile @gestures
  Scenario: Mobile gesture support for promotional content
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am on a product page with promotion gallery
    When I swipe left on promotional images
    Then the image carousel should advance
    When I pinch to zoom on promotional image
    Then the image should zoom appropriately
    When I double-tap on promotional details
    Then the details should expand or contract

  @mobile @offline
  Scenario: Mobile offline behavior with promotional data
    Given I am using device "iPhone 12" with screen size "390x844"
    And I have previously loaded promotional products
    When I go offline
    And I view previously loaded promotional products
    Then cached promotional information should still be visible
    And appropriate offline indicators should be shown
    When I try to apply promotional codes offline
    Then proper offline error messages should appear

  @mobile @notifications
  Scenario: Mobile push notifications for promotions
    Given I am using device "iPhone 12" with screen size "390x844"
    And I have enabled promotional notifications
    When a limited-time promotion becomes available
    Then I should receive a mobile push notification
    And tapping the notification should open the promotional product
    And the promotional price should be immediately visible

  @tablet @landscape
  Scenario: Tablet landscape experience for promotions
    Given I am using device "iPad Pro" with screen size "1024x1366"
    And I am in landscape orientation
    When I browse promotional products
    Then promotional content should utilize the wider screen
    And multiple promotional products should be visible per row
    And promotional details should have adequate spacing
    And touch targets should be optimized for tablet use

  @mobile @comparison
  Scenario: Mobile promotional product comparison
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am comparing 2 promotional products
    When I view the mobile comparison interface
    Then promotional prices should be clearly compared
    And original prices should be shown for both products
    And discount percentages should be easily comparable
    And the interface should fit properly on mobile screen

  @mobile @sharing
  Scenario: Mobile social sharing of promotional products
    Given I am using device "iPhone 12" with screen size "390x844"
    And I am viewing a promotional product
    When I tap the mobile share button
    Then native mobile sharing options should appear
    And shared content should include promotional information
    And shared links should work correctly on mobile devices
    And promotional pricing should be preserved in shared content