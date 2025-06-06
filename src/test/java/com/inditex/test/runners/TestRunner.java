package com.inditex.test.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.inditex.test.stepdefinitions", "com.inditex.test.hooks"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cucumber-pretty.html",
        "json:target/cucumber-reports/CucumberTestReport.json",
        "rerun:target/cucumber-reports/rerun.txt",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = true,
    // tags = "@SmokeTest or @RegressionTest" // Example: Uncomment and modify to run specific tags
    // tags = "not @Ignore" // Example: Exclude tests tagged with @Ignore
    publish = false // Set to true to publish reports to Cucumber Reports service (requires setup)
)
public class TestRunner extends AbstractTestNGCucumberTests {

    // Enables parallel execution of scenarios
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
