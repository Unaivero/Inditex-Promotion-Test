Feature: Comprehensive Promotional Testing with Large-Scale Test Data
  As a QA engineer,
  I want to validate promotional functionality across 500+ test scenarios
  to ensure robust promotional pricing across all Inditex brands, regions, and customer segments.

  Background:
    Given the test data manager is initialized with comprehensive test data
    And all test data sources are validated and available

  @smoke @quick-validation
  Scenario: Quick validation of comprehensive test data availability
    Given I initialize the test data manager
    When I check the test data statistics
    Then the comprehensive promotion data should contain at least 100 records
    And the seasonal campaign data should contain at least 50 records
    And the edge case data should contain at least 50 records
    And the bulk discount data should contain at least 50 records
    And all data files should be valid and accessible

  @regression @comprehensive @data-driven
  Scenario: Execute comprehensive promotional testing across all brands
    Given I load the comprehensive promotional test data
    When I execute promotional testing for all test scenarios
    Then all promotional calculations should be accurate
    And no promotional pricing errors should be detected
    And all customer type discounts should be applied correctly
    And all regional pricing variations should be validated
    And all currency conversions should be accurate

  @performance @load-testing
  Scenario: Performance testing with large data set
    Given I load the performance test data with 1000+ records
    When I execute concurrent promotional price calculations
    Then all promotional prices should be calculated within 2 seconds
    And the system should handle concurrent users efficiently
    And no performance degradation should be observed
    And memory usage should remain within acceptable limits

  @seasonal @campaigns @comprehensive
  Scenario: Validate all seasonal campaign scenarios
    Given I load the seasonal campaigns test data
    When I validate all seasonal promotional scenarios
    Then all seasonal discounts should be applied correctly
    And campaign date ranges should be respected
    And target audience restrictions should be enforced
    And seasonal exclusions should be applied appropriately
    And campaign-specific pricing rules should work correctly

  @bulk @multi-buy @comprehensive
  Scenario: Validate all bulk discount scenarios
    Given I load the bulk discount test data
    When I validate all bulk promotional scenarios
    Then all multi-buy discounts should be calculated correctly
    And quantity thresholds should be enforced
    And bundle pricing should be accurate
    And volume discounts should apply appropriately
    And bulk promotion descriptions should be displayed correctly

  @security @comprehensive @edge-cases
  Scenario: Validate security and edge case scenarios
    Given I load the edge case test data
    When I execute all security and edge case tests
    Then XSS injection attempts should be blocked
    And SQL injection attempts should be prevented
    And invalid input should be handled gracefully
    And boundary values should be validated correctly
    And error messages should be appropriate and secure

  @accessibility @comprehensive
  Scenario: Validate promotional accessibility across all scenarios
    Given I load test data for accessibility testing
    When I validate promotional pricing accessibility
    Then all promotional information should be screen reader accessible
    And promotional prices should have appropriate ARIA labels
    And discount information should be keyboard navigable
    And promotional content should meet WCAG 2.1 AA standards
    And high contrast mode should display promotions correctly

  @mobile @comprehensive
  Scenario: Validate promotional pricing on mobile devices
    Given I load test data for mobile testing
    When I validate promotional pricing on mobile devices
    Then promotional prices should display correctly on all mobile devices
    And touch interactions should work properly for promotional content
    And mobile-specific promotional features should function correctly
    And responsive design should adapt promotional displays appropriately
    And mobile performance should meet acceptable standards

  @internationalization @global
  Scenario: Validate promotional pricing across all international markets
    Given I load test data for all supported countries and languages
    When I validate promotional pricing for international markets
    Then promotional prices should be displayed in correct local currencies
    And promotional text should be properly localized
    And regional tax calculations should be accurate
    And country-specific promotional rules should be applied
    And cultural considerations should be respected in promotional displays

  @cross-brand @validation
  Scenario: Validate promotional consistency across all Inditex brands
    Given I load test data for all Inditex brands
    When I validate promotional functionality across brands
    Then promotional calculation logic should be consistent across brands
    And brand-specific promotional rules should be applied correctly
    And cross-brand promotions should work appropriately
    And brand exclusions should be enforced
    And brand-specific customer segments should be respected

  @data-integrity @validation
  Scenario: Validate test data integrity and completeness
    Given I access all test data sources
    When I validate the integrity of test data
    Then all required fields should be present and valid
    And all test data should follow the expected format
    And no duplicate test scenarios should exist
    And all referenced promotional codes should be valid
    And all price calculations in test data should be mathematically correct

  @monitoring @reporting
  Scenario: Generate comprehensive test execution reports
    Given I have executed all comprehensive promotional tests
    When I generate test execution reports
    Then test coverage should be at least 95% for promotional functionality
    And all test results should be properly documented
    And any failures should be categorized and prioritized
    And performance metrics should be captured and reported
    And test data usage statistics should be generated