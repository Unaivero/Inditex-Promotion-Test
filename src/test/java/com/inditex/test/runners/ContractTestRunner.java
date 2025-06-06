package com.inditex.test.runners;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Contract Tests Suite")
@SelectPackages("com.inditex.test.contract")
@IncludeClassNamePatterns(".*ContractTest")
public class ContractTestRunner {
    // This class runs all contract tests using JUnit 5 Platform Suite
    // It will execute both consumer and provider contract tests
}
