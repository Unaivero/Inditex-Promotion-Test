package com.inditex.test.listeners;

import com.inditex.test.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryListener implements IRetryAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(RetryListener.class);
    
    private int retryCount = 0;
    private final int maxRetryCount;
    private final boolean retryEnabled;
    
    public RetryListener() {
        this.maxRetryCount = ConfigManager.getIntProperty("test.retry.count", 2);
        this.retryEnabled = ConfigManager.getBooleanProperty("test.retry.enabled", true);
        logger.debug("RetryListener initialized - Max retries: {}, Retry enabled: {}", 
            maxRetryCount, retryEnabled);
    }
    
    @Override
    public boolean retry(ITestResult result) {
        if (!retryEnabled) {
            logger.debug("Retry is disabled, not retrying test: {}", result.getMethod().getMethodName());
            return false;
        }
        
        if (retryCount < maxRetryCount) {
            retryCount++;
            
            String testName = result.getMethod().getMethodName();
            String errorMessage = result.getThrowable() != null ? 
                result.getThrowable().getMessage() : "Unknown error";
            
            logger.warn("Test '{}' failed (attempt {}/{}). Reason: {}. Retrying...", 
                testName, retryCount, maxRetryCount + 1, errorMessage);
            
            // Clear any existing WebDriver instance before retry
            try {
                com.inditex.test.utils.WebDriverFactory.quitDriver();
                Thread.sleep(1000); // Brief pause before retry
            } catch (Exception e) {
                logger.warn("Error during cleanup before retry", e);
            }
            
            return true;
        } else {
            logger.error("Test '{}' failed after {} attempts. Not retrying further.", 
                result.getMethod().getMethodName(), maxRetryCount + 1);
            retryCount = 0; // Reset for next test
            return false;
        }
    }
    
    /**
     * Check if a test should be retried based on the exception type
     */
    private boolean shouldRetry(ITestResult result) {
        if (result.getThrowable() == null) {
            return false;
        }
        
        Throwable throwable = result.getThrowable();
        String errorMessage = throwable.getMessage();
        
        // Don't retry for assertion failures (test logic errors)
        if (throwable instanceof AssertionError) {
            logger.debug("Not retrying AssertionError for test: {}", result.getMethod().getMethodName());
            return false;
        }
        
        // Retry for common transient issues
        if (errorMessage != null) {
            String lowerCaseMessage = errorMessage.toLowerCase();
            
            // Network-related issues
            if (lowerCaseMessage.contains("timeout") ||
                lowerCaseMessage.contains("connection") ||
                lowerCaseMessage.contains("network") ||
                lowerCaseMessage.contains("socket")) {
                logger.debug("Retrying due to network-related error: {}", errorMessage);
                return true;
            }
            
            // WebDriver-related issues
            if (lowerCaseMessage.contains("webdriver") ||
                lowerCaseMessage.contains("selenium") ||
                lowerCaseMessage.contains("stale element") ||
                lowerCaseMessage.contains("element not found")) {
                logger.debug("Retrying due to WebDriver-related error: {}", errorMessage);
                return true;
            }
            
            // Browser-related issues
            if (lowerCaseMessage.contains("browser") ||
                lowerCaseMessage.contains("chrome") ||
                lowerCaseMessage.contains("firefox") ||
                lowerCaseMessage.contains("edge")) {
                logger.debug("Retrying due to browser-related error: {}", errorMessage);
                return true;
            }
        }
        
        return true; // Default to retry for other exceptions
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public int getMaxRetryCount() {
        return maxRetryCount;
    }
    
    public boolean isRetryEnabled() {
        return retryEnabled;
    }
}
