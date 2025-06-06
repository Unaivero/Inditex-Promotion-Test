package com.inditex.test.listeners;

import com.inditex.test.utils.WebDriverFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AllureTestListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(AllureTestListener.class);
    
    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Starting test: {}", result.getMethod().getMethodName());
        
        // Add test environment information
        Allure.addAttachment("Test Environment", "text/plain",
            String.format(
                "Test: %s%nClass: %s%nStart Time: %s%nBrowser: %s%nEnvironment: %s",
                result.getMethod().getMethodName(),
                result.getTestClass().getName(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                System.getProperty("browser.type", "chrome"),
                System.getProperty("test.environment", "dev")
            )
        );
        
        // Set test case name in Allure
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setName(result.getMethod().getMethodName());
            testResult.setDescription(getTestDescription(result));
        });
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}", result.getMethod().getMethodName());
        
        // Add success information
        Allure.addAttachment("Test Result", "text/plain",
            String.format("Test '%s' completed successfully in %d ms",
                result.getMethod().getMethodName(),
                result.getEndMillis() - result.getStartMillis())
        );
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {}", result.getMethod().getMethodName(), result.getThrowable());
        
        // Take screenshot on failure
        takeScreenshot(result.getMethod().getMethodName());
        
        // Add failure information
        Allure.addAttachment("Failure Details", "text/plain",
            String.format(
                "Test: %s%nFailure Reason: %s%nFailure Time: %s%nDuration: %d ms",
                result.getMethod().getMethodName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                result.getEndMillis() - result.getStartMillis()
            )
        );
        
        // Add stack trace
        if (result.getThrowable() != null) {
            Allure.addAttachment("Stack Trace", "text/plain",
                getStackTrace(result.getThrowable())
            );
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}", result.getMethod().getMethodName());
        
        Allure.addAttachment("Skip Reason", "text/plain",
            String.format("Test '%s' was skipped. Reason: %s",
                result.getMethod().getMethodName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown")
        );
    }
    
    private void takeScreenshot(String testName) {
        try {
            WebDriver driver = WebDriverFactory.getDriver();
            if (driver != null && driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Failure Screenshot - " + testName, 
                    "image/png", new ByteArrayInputStream(screenshot), "png");
                logger.info("Screenshot captured for failed test: {}", testName);
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot for test: {}", testName, e);
        }
    }
    
    private String getTestDescription(ITestResult result) {
        // Try to get description from test annotation or method name
        if (result.getMethod().getDescription() != null && !result.getMethod().getDescription().isEmpty()) {
            return result.getMethod().getDescription();
        }
        return "Automated test for " + result.getMethod().getMethodName();
    }
    
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        if (throwable.getCause() != null) {
            sb.append("Caused by: ").append(getStackTrace(throwable.getCause()));
        }
        return sb.toString();
    }
}
