package com.inditex.test.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.inditex.test.stepdefinitions", "com.inditex.test.hooks"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/performance-tests.html",
        "json:target/cucumber-reports/performance-tests.json",
        "junit:target/cucumber-reports/performance-tests.xml",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@performance",
    monochrome = true,
    publish = false
)
public class PerformanceTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
