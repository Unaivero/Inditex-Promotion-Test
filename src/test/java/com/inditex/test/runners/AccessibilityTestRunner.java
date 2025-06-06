package com.inditex.test.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.inditex.test.stepdefinitions", "com.inditex.test.hooks"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/accessibility-tests.html",
        "json:target/cucumber-reports/accessibility-tests.json",
        "junit:target/cucumber-reports/accessibility-tests.xml",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@accessibility or @wcag",
    monochrome = true,
    publish = false
)
public class AccessibilityTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
