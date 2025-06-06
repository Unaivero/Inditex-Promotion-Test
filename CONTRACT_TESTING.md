# Contract Testing with Pact

## Overview

This project implements consumer-driven contract testing using Pact to ensure reliable API communication between services.

## What is Contract Testing?

Contract testing verifies that services can communicate with each other by testing the interactions between consumer and provider services. It ensures:

- APIs meet consumer expectations
- Breaking changes are detected early
- Service integration reliability
- Documentation of service interactions

## Implementation

### Consumer Tests

Consumer tests define expectations for how the consumer will interact with the provider:

- `PromotionalPricingContractTest.java` - Tests for promotional pricing API
- `InventoryServiceContractTest.java` - Tests for inventory service API

### Provider Tests

Provider tests verify that the actual provider service meets the consumer's expectations:

- `PromotionalPricingProviderContractTest.java` - Verifies promotional pricing API

### Contract Scenarios Covered

1. **Happy Path Scenarios**
   - Valid product promotional pricing requests
   - Successful inventory checks
   - Successful inventory reservations

2. **Error Scenarios**
   - Invalid product SKUs (404 responses)
   - Authentication failures (401 responses)
   - Out of stock conditions

3. **Business Logic Scenarios**
   - Member-only promotions
   - Expired promotions
   - Bulk operations

## Running Contract Tests

### Consumer Tests
```bash
# Run all consumer contract tests
mvn test -Dtest="*ContractTest"

# Run specific consumer test
mvn test -Dtest="PromotionalPricingContractTest"
```

### Provider Tests
```bash
# Run provider verification
mvn test -Dtest="*ProviderContractTest"
```

### Via TestNG
```bash
# Run contract test suite
mvn test -Dgroups="contract"
```

### Via Cucumber
```bash
# Run contract testing features
mvn test -Dcucumber.filter.tags="@contract"
```

## Contract Files

Generated contract files (Pact files) are stored in:
- `target/pacts/` - Generated consumer contracts
- `src/test/resources/pacts/` - Reference contract files

## Pact Broker Integration

The project is configured to work with a Pact Broker for:
- Storing and versioning contracts
- Sharing contracts between teams
- Tracking contract compatibility

Configuration in `pact.properties`:
```properties
pact.broker.url=http://localhost:9292
pact.consumer.name=promotions-ui-tests
pact.provider.name=promotional-pricing-api
```

## Contract Verification Workflow

1. **Consumer Side**:
   - Write consumer tests defining expectations
   - Run tests to generate Pact files
   - Publish contracts to Pact Broker

2. **Provider Side**:
   - Download consumer contracts
   - Run provider verification tests
   - Publish verification results

3. **CI/CD Integration**:
   - Run consumer tests on consumer code changes
   - Run provider verification on provider code changes
   - Block deployments if contracts are broken

## Best Practices

1. **Contract Design**:
   - Focus on data structure, not values
   - Include both success and error scenarios
   - Keep contracts minimal and focused

2. **Test Maintenance**:
   - Update contracts when APIs change
   - Use provider states for test setup
   - Verify contracts regularly

3. **Team Collaboration**:
   - Share contracts early and often
   - Communicate breaking changes
   - Use semantic versioning

## Troubleshooting

### Common Issues

1. **Mock Server Port Conflicts**:
   - Pact uses dynamic ports by default
   - Configure specific ports if needed

2. **Contract Mismatches**:
   - Check data types and structure
   - Verify required vs optional fields
   - Ensure consistent error formats

3. **Provider State Setup**:
   - Implement proper state setup methods
   - Clean up test data between tests
   - Use isolated test databases

### Debugging

- Enable detailed logging: `pact.verification.publishResults=true`
- Check generated Pact files for correctness
- Use Pact Broker UI for contract visualization
- Review provider verification logs

## Integration with Existing Tests

Contract tests complement but don't replace:
- Unit tests for business logic
- Integration tests for end-to-end flows
- UI tests for user interactions

The contract tests ensure service boundaries are well-defined and stable.
