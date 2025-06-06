package com.inditex.test.visual;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.Eyes;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.selenium.ClassicRunner;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.inditex.test.config.ConfigManager;
import io.percy.selenium.Percy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Visual testing helper using Applitools Eyes and Percy for visual regression testing
 */
public class VisualTestHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(VisualTestHelper.class);
    
    private Eyes eyes;
    private Percy percy;
    private ClassicRunner runner;
    private Configuration config;
    private boolean visualTestingEnabled;
    
    @PostConstruct
    public void initialize() {
        visualTestingEnabled = ConfigManager.getBooleanProperty("visual.testing.enabled", false);
        
        if (!visualTestingEnabled) {
            logger.info("Visual testing is disabled");
            return;
        }
        
        initializeApplitools();
        initializePercy();
        
        logger.info("Visual testing initialized successfully");
    }
    
    private void initializeApplitools() {
        String apiKey = ConfigManager.getProperty("applitools.api.key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Applitools API key not configured, visual testing will be limited");
            return;
        }
        
        runner = new ClassicRunner();
        eyes = new Eyes(runner);
        
        config = new Configuration();
        config.setApiKey(apiKey);
        config.setAppName("Inditex Promotions Test");
        config.setBatch(createBatchInfo());
        
        // Configure viewport size
        config.setViewportSize(new RectangleSize(1920, 1080));
        
        // Set match level
        config.setMatchLevel(com.applitools.eyes.MatchLevel.LAYOUT);
        
        eyes.setConfiguration(config);
        
        logger.info("Applitools Eyes initialized");
    }
    
    private void initializePercy() {
        String percyToken = ConfigManager.getProperty("percy.token");
        if (percyToken != null && !percyToken.trim().isEmpty()) {
            percy = new Percy();
            logger.info("Percy visual testing initialized");
        } else {
            logger.warn("Percy token not configured, Percy visual testing will be skipped");
        }
    }
    
    private BatchInfo createBatchInfo() {
        String batchName = String.format("Inditex Promotions - %s - %s",
            ConfigManager.getProperty("test.environment", "dev"),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        
        BatchInfo batch = new BatchInfo(batchName);
        batch.setId(System.getProperty("batch.id", "default-batch"));
        
        return batch;
    }
    
    /**
     * Validates the entire promotional page layout
     */
    public void validatePromotionalPageLayout(WebDriver driver, String testName, String pageName) {
        if (!visualTestingEnabled) {
            logger.debug("Visual testing skipped for: {}", testName);
            return;
        }
        
        try {
            if (eyes != null) {
                eyes.open(driver, "Promotional Pages", testName);
                eyes.checkWindow(pageName);
                eyes.closeAsync();
            }
            
            if (percy != null) {
                percy.screenshot(driver, testName + " - " + pageName);
            }
            
            logger.info("Visual validation completed for: {} - {}", testName, pageName);
            
        } catch (Exception e) {
            logger.error("Visual validation failed for: {} - {}", testName, pageName, e);
            throw new RuntimeException("Visual validation failed", e);
        }
    }
    
    /**
     * Validates promotional banners and discount displays
     */
    public void validatePromotionalBanner(WebDriver driver, String testName, By bannerLocator) {
        if (!visualTestingEnabled) return;
        
        try {
            if (eyes != null) {
                eyes.open(driver, "Promotional Banners", testName);
                eyes.check(Target.region(bannerLocator).withName("Promotional Banner"));
                eyes.closeAsync();
            }
            
            if (percy != null) {
                percy.screenshot(driver, testName + " - Banner");
            }
            
            logger.info("Promotional banner validation completed for: {}", testName);
            
        } catch (Exception e) {
            logger.error("Promotional banner validation failed for: {}", testName, e);
            throw new RuntimeException("Banner validation failed", e);
        }
    }
    
    /**
     * Validates product pricing display consistency
     */
    public void validatePricingDisplay(WebDriver driver, String testName, By pricingSection) {
        if (!visualTestingEnabled) return;
        
        try {
            if (eyes != null) {
                eyes.open(driver, "Pricing Display", testName);
                eyes.check(Target.region(pricingSection)
                    .withName("Pricing Section")
                    .layout());
                eyes.closeAsync();
            }
            
            if (percy != null) {
                percy.screenshot(driver, testName + " - Pricing");
            }
            
            logger.info("Pricing display validation completed for: {}", testName);
            
        } catch (Exception e) {
            logger.error("Pricing display validation failed for: {}", testName, e);
            throw new RuntimeException("Pricing validation failed", e);
        }
    }
    
    /**
     * Validates shopping cart visual consistency
     */
    public void validateShoppingCart(WebDriver driver, String testName) {
        if (!visualTestingEnabled) return;
        
        try {
            if (eyes != null) {
                eyes.open(driver, "Shopping Cart", testName);
                eyes.checkWindow("Shopping Cart View");
                eyes.closeAsync();
            }
            
            if (percy != null) {
                percy.screenshot(driver, testName + " - Cart");
            }
            
            logger.info("Shopping cart validation completed for: {}", testName);
            
        } catch (Exception e) {
            logger.error("Shopping cart validation failed for: {}", testName, e);
            throw new RuntimeException("Cart validation failed", e);
        }
    }
    
    /**
     * Validates mobile responsive design
     */
    public void validateMobileView(WebDriver driver, String testName, String deviceName) {
        if (!visualTestingEnabled) return;
        
        try {
            if (eyes != null) {
                eyes.open(driver, "Mobile Views", testName + " - " + deviceName);
                eyes.checkWindow("Mobile Layout");
                eyes.closeAsync();
            }
            
            if (percy != null) {
                percy.screenshot(driver, testName + " - " + deviceName);
            }
            
            logger.info("Mobile view validation completed for: {} on {}", testName, deviceName);
            
        } catch (Exception e) {
            logger.error("Mobile view validation failed for: {} on {}", testName, deviceName, e);
            throw new RuntimeException("Mobile validation failed", e);
        }
    }
    
    /**
     * Validates cross-browser visual consistency
     */
    public void validateCrossBrowserConsistency(WebDriver driver, String testName, String browserName) {
        if (!visualTestingEnabled) return;
        
        try {
            if (eyes != null) {
                eyes.open(driver, "Cross-Browser", testName + " - " + browserName);
                eyes.checkWindow("Browser Layout");
                eyes.closeAsync();
            }
            
            if (percy != null) {
                percy.screenshot(driver, testName + " - " + browserName);
            }
            
            logger.info("Cross-browser validation completed for: {} on {}", testName, browserName);
            
        } catch (Exception e) {
            logger.error("Cross-browser validation failed for: {} on {}", testName, browserName, e);
            throw new RuntimeException("Cross-browser validation failed", e);
        }
    }
    
    /**
     * Finalizes all visual tests and generates reports
     */
    @PreDestroy
    public void finalizeTests() {
        if (!visualTestingEnabled) return;
        
        try {
            if (runner != null) {
                com.applitools.eyes.TestResultsSummary allTestResults = runner.getAllTestResults(false);
                logger.info("Visual test results: Total tests: {}, Passed: {}, Failed: {}", 
                    allTestResults.size(),
                    allTestResults.size() - allTestResults.getFailed(),
                    allTestResults.getFailed()
                );
                
                if (allTestResults.getFailed() > 0) {
                    logger.error("Visual tests failed! Check Applitools dashboard for details");
                }
            }
            
            logger.info("Visual testing finalized");
            
        } catch (Exception e) {
            logger.error("Error finalizing visual tests", e);
        }
    }
    
    /**
     * Gets the visual test results summary
     */
    public VisualTestResults getTestResults() {
        if (!visualTestingEnabled || runner == null) {
            return new VisualTestResults(0, 0, 0);
        }
        
        try {
            com.applitools.eyes.TestResultsSummary summary = runner.getAllTestResults(false);
            return new VisualTestResults(
                summary.size(),
                summary.size() - summary.getFailed(),
                summary.getFailed()
            );
        } catch (Exception e) {
            logger.error("Error getting visual test results", e);
            return new VisualTestResults(0, 0, 1);
        }
    }
    
    public static class VisualTestResults {
        private final int totalTests;
        private final int passedTests;
        private final int failedTests;
        
        public VisualTestResults(int totalTests, int passedTests, int failedTests) {
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.failedTests = failedTests;
        }
        
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return failedTests; }
        public double getPassRate() { 
            return totalTests > 0 ? (double) passedTests / totalTests * 100 : 0; 
        }
    }
}
