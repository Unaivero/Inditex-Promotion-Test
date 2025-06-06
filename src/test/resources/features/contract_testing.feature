Feature: API Contract Validation
  As a QA Engineer
  I want to validate API contracts between services
  So that services can communicate reliably and breaking changes are detected early

  Background:
    Given the contract testing framework is initialized
    And the API mock servers are available

  @contract @api
  Scenario: Validate promotional pricing API contract
    Given I have a valid consumer contract for promotional pricing API
    When I execute the promotional pricing contract tests
    Then all consumer contract tests should pass
    And the pact file should be generated successfully
    And the contract should specify the expected request format
    And the contract should specify the expected response format

  @contract @api
  Scenario: Validate inventory service API contract
    Given I have a valid consumer contract for inventory service API
    When I execute the inventory service contract tests
    Then all consumer contract tests should pass
    And the pact file should be generated successfully
    And inventory availability should be correctly specified
    And reservation functionality should be properly contracted

  @contract @negative
  Scenario: Validate API contract with invalid requests
    Given I have consumer contracts that include error scenarios
    When I test invalid product SKU requests
    Then the contract should specify 404 error responses
    And error messages should follow the agreed format
    And error codes should be consistent across services

  @contract @negative
  Scenario: Validate API contract with authentication failures
    Given I have consumer contracts for authentication scenarios
    When I test requests without valid authentication
    Then the contract should specify 401 error responses
    And authentication error messages should be standardized

  @contract @provider
  Scenario: Validate provider fulfills consumer contracts
    Given I have generated consumer contracts
    And the provider service is running
    When I execute provider verification tests
    Then the provider should satisfy all consumer expectations
    And all contracted API endpoints should be available
    And response formats should match consumer expectations

  @contract @integration
  Scenario: End-to-end contract validation workflow
    Given I have both consumer and provider contract tests
    When I run the complete contract testing suite
    Then consumer contracts should be generated
    And provider verification should pass
    And contract compatibility should be confirmed
    And breaking changes should be detected if any

  @contract @versioning
  Scenario: Validate API contract versioning
    Given I have contracts for different API versions
    When I test backward compatibility
    Then older contract versions should still be supported
    And new contract features should not break existing consumers
    And version-specific behaviors should be properly handled

  @contract @performance
  Scenario: Validate contract performance requirements
    Given I have contracts with performance expectations
    When I execute contract tests with timing validation
    Then API responses should meet contracted response times
    And bulk operations should perform within specified limits
    And concurrent request handling should meet expectations
