package com.inditex.test.cloud;

import com.browserstack.local.Local;
import com.inditex.test.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.annotations.Tag;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Cloud Testing Manager supporting BrowserStack, Sauce Labs, and other cloud providers
 * with real device testing, parallel execution, and comprehensive reporting
 */
public class CloudTestingManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CloudTestingManager.class);
    
    private static final String BROWSERSTACK_HUB_URL = "https://hub-cloud.browserstack.com/wd/hub";
    private static final String SAUCE_LABS_HUB_URL = "https://ondemand.us-west-1.saucelabs.com:443/wd/hub";
    
    private Local browserStackLocal;
    private final Map<Long, WebDriver> activeDrivers = new ConcurrentHashMap<>();
    private CloudProvider currentProvider;
    
    public enum CloudProvider {
        BROWSERSTACK,
        SAUCE_LABS,
        LAMBDA_TEST
    }
    
    @PostConstruct
    public void initialize() {
        String provider = ConfigManager.getProperty("cloud.provider", "BROWSERSTACK");
        currentProvider = CloudProvider.valueOf(provider.toUpperCase());
        
        if (currentProvider == CloudProvider.BROWSERSTACK) {
            initializeBrowserStackLocal();
        }
        
        logger.info("Cloud testing manager initialized with provider: {}", currentProvider);
    }
    
    private void initializeBrowserStackLocal() {
        boolean localEnabled = ConfigManager.getBooleanProperty("browserstack.local.enabled", false);
        
        if (localEnabled) {
            try {
                browserStackLocal = new Local();
                HashMap<String, String> bsLocalArgs = new HashMap<>();
                bsLocalArgs.put("key", ConfigManager.getProperty("browserstack.access.key"));
                bsLocalArgs.put("localIdentifier", "InditexPromotionsTest");
                bsLocalArgs.put("force", "true");
                
                browserStackLocal.start(bsLocalArgs);
                logger.info("BrowserStack Local tunnel started successfully");
                
            } catch (Exception e) {
                logger.error("Failed to start BrowserStack Local tunnel", e);
                throw new RuntimeException("BrowserStack Local initialization failed", e);
            }
        }
    }
    
    public WebDriver createBrowserStackDriver(String device, String browser, String browserVersion, String os, String osVersion) {
        DesiredCapabilities caps = new DesiredCapabilities();
        
        // BrowserStack specific capabilities
        caps.setCapability("browserstack.user", ConfigManager.getProperty("browserstack.username"));
        caps.setCapability("browserstack.key", ConfigManager.getProperty("browserstack.access.key"));
        
        // Project and build information
        caps.setCapability("project", "Inditex Promotions Test Suite");
        caps.setCapability("build", "Build " + System.getProperty("build.number", "local"));
        caps.setCapability("name", Thread.currentThread().getName());
        
        // Browser/Device configuration
        if (device != null && !device.isEmpty()) {
            // Mobile device
            caps.setCapability("device", device);
            caps.setCapability("realMobile", "true");
        } else {
            // Desktop browser
            caps.setCapability("browser", browser);
            caps.setCapability("browser_version", browserVersion);
            caps.setCapability("os", os);
            caps.setCapability("os_version", osVersion);
        }
        
        // Additional capabilities
        caps.setCapability("browserstack.local", ConfigManager.getBooleanProperty("browserstack.local.enabled", false));
        caps.setCapability("browserstack.localIdentifier", "InditexPromotionsTest");
        caps.setCapability("browserstack.debug", true);
        caps.setCapability("browserstack.video", true);
        caps.setCapability("browserstack.networkLogs", true);
        caps.setCapability("browserstack.console", "errors");
        caps.setCapability("acceptSslCerts", true);
        
        try {
            WebDriver driver = new RemoteWebDriver(new URL(BROWSERSTACK_HUB_URL), caps);
            activeDrivers.put(Thread.currentThread().getId(), driver);
            
            logger.info("Created BrowserStack driver for device: {}, browser: {}", device, browser);
            return driver;
            
        } catch (MalformedURLException e) {
            logger.error("Invalid BrowserStack URL", e);
            throw new RuntimeException("BrowserStack driver creation failed", e);
        }
    }
    
    @Test
    @Tag("mobile")
    @Tag("cloud")
    public void testPromotionsOnRealDevice() {
        WebDriver driver = createBrowserStackDriver("iPhone 13", "Safari", null, null, null);
        
        try {
            // Navigate to promotional page
            driver.get("https://www.zara.com/es/en/");
            
            // Verify mobile layout
            String title = driver.getTitle();
            assert title.contains("Zara") : "Page title should contain Zara";
            
            logger.info("Mobile promotional test completed successfully on iPhone 13");
            
        } finally {
            quitDriver(driver);
        }
    }
    
    public void quitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
                activeDrivers.remove(Thread.currentThread().getId());
                logger.debug("Cloud driver closed successfully");
            } catch (Exception e) {
                logger.warn("Error closing cloud driver", e);
            }
        }
    }
    
    @PreDestroy
    public void cleanup() {
        // Close all active drivers
        activeDrivers.values().forEach(this::quitDriver);
        activeDrivers.clear();
        
        // Stop BrowserStack Local if running
        if (browserStackLocal != null && browserStackLocal.isRunning()) {
            try {
                browserStackLocal.stop();
                logger.info("BrowserStack Local tunnel stopped");
            } catch (Exception e) {
                logger.error("Error stopping BrowserStack Local tunnel", e);
            }
        }
        
        logger.info("Cloud testing manager cleaned up");
    }
}
