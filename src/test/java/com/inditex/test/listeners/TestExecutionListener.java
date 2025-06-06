package com.inditex.test.listeners;

import com.inditex.test.utils.WebDriverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class TestExecutionListener implements ITestListener, IExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutionListener.class);
    
    private LocalDateTime executionStartTime;
    private LocalDateTime executionEndTime;
    private final AtomicInteger totalTests = new AtomicInteger(0);
    private final AtomicInteger passedTests = new AtomicInteger(0);
    private final AtomicInteger failedTests = new AtomicInteger(0);
    private final AtomicInteger skippedTests = new AtomicInteger(0);
    
    @Override
    public void onExecutionStart() {
        executionStartTime = LocalDateTime.now();
        logger.info("=== Test Execution Started at {} ===", 
            executionStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Log environment information
        logger.info("Test Environment Configuration:");
        logger.info("  - Browser: {}", System.getProperty("browser.type", "chrome"));
        logger.info("  - Environment: {}", System.getProperty("test.environment", "dev"));
        logger.info("  - Headless: {}", System.getProperty("browser.headless", "false"));
        logger.info("  - Grid Enabled: {}", System.getProperty("grid.enabled", "false"));
        logger.info("  - Parallel Threads: {}", System.getProperty("parallel.thread.count", "1"));
        
        // Reset counters
        totalTests.set(0);
        passedTests.set(0);
        failedTests.set(0);
        skippedTests.set(0);
    }
    
    @Override
    public void onExecutionFinish() {
        executionEndTime = LocalDateTime.now();
        Duration executionDuration = Duration.between(executionStartTime, executionEndTime);
        
        logger.info("=== Test Execution Finished at {} ===", 
            executionEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logger.info("=== Execution Summary ===");
        logger.info("  Total Duration: {} minutes {} seconds", 
            executionDuration.toMinutes(), executionDuration.getSeconds() % 60);
        logger.info("  Total Tests: {}", totalTests.get());
        logger.info("  Passed: {}", passedTests.get());
        logger.info("  Failed: {}", failedTests.get());
        logger.info("  Skipped: {}", skippedTests.get());
        
        if (totalTests.get() > 0) {
            double successRate = (double) passedTests.get() / totalTests.get() * 100;
            logger.info("  Success Rate: {:.2f}%", successRate);
        }
        
        // Cleanup WebDriver instances
        cleanupResources();
        
        logger.info("=== End of Test Execution ===");
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        totalTests.incrementAndGet();
        logger.debug("Starting test {}.{}", 
            result.getTestClass().getName(), 
            result.getMethod().getMethodName());
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        passedTests.incrementAndGet();
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.debug("Test passed: {}.{} ({}ms)", 
            result.getTestClass().getName(), 
            result.getMethod().getMethodName(), 
            duration);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        failedTests.incrementAndGet();
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.error("Test failed: {}.{} ({}ms) - {}", 
            result.getTestClass().getName(), 
            result.getMethod().getMethodName(), 
            duration,
            result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown error");
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        skippedTests.incrementAndGet();
        logger.warn("Test skipped: {}.{} - {}", 
            result.getTestClass().getName(), 
            result.getMethod().getMethodName(),
            result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown reason");
    }
    
    private void cleanupResources() {
        try {
            logger.info("Cleaning up test execution resources...");
            
            // Close all WebDriver instances
            int activeDrivers = WebDriverFactory.getActiveDriverCount();
            if (activeDrivers > 0) {
                logger.info("Closing {} active WebDriver instances", activeDrivers);
                WebDriverFactory.quitAllDrivers();
            }
            
            logger.info("Resource cleanup completed");
        } catch (Exception e) {
            logger.error("Error during resource cleanup", e);
        }
    }
    
    public int getTotalTests() {
        return totalTests.get();
    }
    
    public int getPassedTests() {
        return passedTests.get();
    }
    
    public int getFailedTests() {
        return failedTests.get();
    }
    
    public int getSkippedTests() {
        return skippedTests.get();
    }
    
    public double getSuccessRate() {
        if (totalTests.get() == 0) return 0.0;
        return (double) passedTests.get() / totalTests.get() * 100;
    }
}
