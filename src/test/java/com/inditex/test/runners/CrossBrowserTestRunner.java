package com.inditex.test.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.BeforeClass;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.inditex.test.stepdefinitions", "com.inditex.test.hooks"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cross-browser-tests.html",
        "json:target/cucumber-reports/cross-browser-tests.json",
        "junit:target/cucumber-reports/cross-browser-tests.xml",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@cross-browser or @smoke",
    monochrome = true,
    publish = false
)
public class CrossBrowserTestRunner extends AbstractTestNGCucumberTests {

    @Parameters({"browser"})
    @BeforeClass
    public void setUp(String browser) {
        // Set browser for cross-browser testing
        if (browser != null && !browser.isEmpty()) {
            System.setProperty("browser.type", browser);
        }
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
