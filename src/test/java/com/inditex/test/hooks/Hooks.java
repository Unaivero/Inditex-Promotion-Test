package com.inditex.test.hooks;

import com.inditex.test.utils.WebDriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

public class Hooks {

    private WebDriver driver;

    @Before
    public void setUp(Scenario scenario) {
        System.out.println("Starting scenario: " + scenario.getName());
        // driver = WebDriverFactory.getDriver(); // Driver is initialized when first called by a step or page object
        // No need to explicitly get driver here unless some pre-scenario setup requires it directly
    }

    @After
    public void tearDown(Scenario scenario) {
        System.out.println("Finished scenario: " + scenario.getName() + " with status: " + scenario.getStatus());
        driver = WebDriverFactory.getDriver(); // Ensure driver is available for screenshot
        if (scenario.isFailed()) {
            if (driver instanceof TakesScreenshot) {
                try {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    Allure.addAttachment(scenario.getName() + " - Failure Screenshot", new ByteArrayInputStream(screenshot));
                    System.out.println("Screenshot taken for failed scenario: " + scenario.getName());
                } catch (Exception e) {
                    System.err.println("Failed to take screenshot: " + e.getMessage());
                }
            }
        }
        WebDriverFactory.quitDriver();
        System.out.println("Browser closed and WebDriver instance quit.");
    }
}
