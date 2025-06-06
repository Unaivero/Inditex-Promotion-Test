package com.inditex.test.listeners;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.utils.WebDriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotListener.class);
    
    private final boolean screenshotsEnabled;
    private final String screenshotDirectory;
    private final boolean screenshotOnSuccess;
    private final boolean screenshotOnFailure;
    
    public ScreenshotListener() {
        this.screenshotsEnabled = ConfigManager.getBooleanProperty("reporting.screenshots.enabled", true);
        this.screenshotDirectory = ConfigManager.getProperty("screenshot.directory", "target/screenshots");
        this.screenshotOnSuccess = ConfigManager.getBooleanProperty("screenshot.on.success", false);
        this.screenshotOnFailure = ConfigManager.getBooleanProperty("screenshot.on.failure", true);
        
        // Create screenshot directory if it doesn't exist
        createScreenshotDirectory();
        
        logger.debug("ScreenshotListener initialized - Enabled: {}, Directory: {}, On Success: {}, On Failure: {}", 
            screenshotsEnabled, screenshotDirectory, screenshotOnSuccess, screenshotOnFailure);
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        // Optional: Take screenshot at test start
        if (screenshotsEnabled && ConfigManager.getBooleanProperty("screenshot.on.start", false)) {
            takeScreenshot(result, "start");
        }
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        if (screenshotsEnabled && screenshotOnSuccess) {
            takeScreenshot(result, "success");
        }
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        if (screenshotsEnabled && screenshotOnFailure) {
            String screenshotPath = takeScreenshot(result, "failure");
            if (screenshotPath != null) {
                // Add screenshot path to test result for reporting
                System.setProperty("screenshot.path", screenshotPath);
                logger.info("Failure screenshot saved: {}", screenshotPath);
            }
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        if (screenshotsEnabled && ConfigManager.getBooleanProperty("screenshot.on.skip", false)) {
            takeScreenshot(result, "skipped");
        }
    }
    
    private String takeScreenshot(ITestResult result, String status) {
        try {
            WebDriver driver = WebDriverFactory.getDriver();
            if (driver == null) {
                logger.debug("WebDriver is null, cannot take screenshot for test: {}", 
                    result.getMethod().getMethodName());
                return null;
            }
            
            if (!(driver instanceof TakesScreenshot)) {
                logger.warn("WebDriver does not support screenshots for test: {}", 
                    result.getMethod().getMethodName());
                return null;
            }
            
            // Generate screenshot filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String className = result.getTestClass().getName().substring(
                result.getTestClass().getName().lastIndexOf('.') + 1);
            String methodName = result.getMethod().getMethodName();
            String filename = String.format("%s_%s_%s_%s.png", className, methodName, status, timestamp);
            
            // Take screenshot
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            
            // Save screenshot to file
            Path screenshotPath = Paths.get(screenshotDirectory, filename);
            Files.write(screenshotPath, screenshot);
            
            String absolutePath = screenshotPath.toAbsolutePath().toString();
            logger.debug("Screenshot saved: {}", absolutePath);
            
            return absolutePath;
            
        } catch (Exception e) {
            logger.error("Failed to take screenshot for test: {} - {}", 
                result.getMethod().getMethodName(), e.getMessage());
            return null;
        }
    }
    
    private void createScreenshotDirectory() {
        try {
            Path path = Paths.get(screenshotDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.debug("Created screenshot directory: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create screenshot directory: {}", screenshotDirectory, e);
        }
    }
    
    /**
     * Utility method to take a manual screenshot with custom name
     */
    public static String takeManualScreenshot(String customName) {
        ScreenshotListener listener = new ScreenshotListener();
        try {
            WebDriver driver = WebDriverFactory.getDriver();
            if (driver instanceof TakesScreenshot) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                String filename = String.format("%s_%s.png", customName, timestamp);
                
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Path screenshotPath = Paths.get(listener.screenshotDirectory, filename);
                Files.write(screenshotPath, screenshot);
                
                String absolutePath = screenshotPath.toAbsolutePath().toString();
                logger.info("Manual screenshot saved: {}", absolutePath);
                return absolutePath;
            }
        } catch (Exception e) {
            logger.error("Failed to take manual screenshot: {}", customName, e);
        }
        return null;
    }
    
    public boolean isScreenshotsEnabled() {
        return screenshotsEnabled;
    }
    
    public String getScreenshotDirectory() {
        return screenshotDirectory;
    }
}
