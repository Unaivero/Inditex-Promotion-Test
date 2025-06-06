package com.inditex.test.utils;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.exceptions.WebDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WebDriverFactory {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);
    private static final ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<Long, WebDriver> driverInstances = new ConcurrentHashMap<>();
    private static final AtomicInteger instanceCounter = new AtomicInteger(0);
    
    public enum BrowserType {
        CHROME, FIREFOX, EDGE, SAFARI
    }

    public static WebDriver getDriver() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver == null) {
            String browserName = ConfigManager.getProperty("browser.default", "chrome");
            boolean headless = ConfigManager.getBooleanProperty("browser.headless", false);
            String gridUrl = ConfigManager.getProperty("grid.url");
            
            driver = createDriver(BrowserType.valueOf(browserName.toUpperCase()), headless, gridUrl);
            webDriverThreadLocal.set(driver);
            driverInstances.put(Thread.currentThread().getId(), driver);
            
            int instanceCount = instanceCounter.incrementAndGet();
            logger.info("Created WebDriver instance #{} for thread {}", instanceCount, Thread.currentThread().getId());
        }
        return driver;
    }
    
    public static WebDriver createDriver(BrowserType browserType, boolean headless, String gridUrl) {
        try {
            WebDriver driver;
            
            if (gridUrl != null && !gridUrl.trim().isEmpty()) {
                driver = createRemoteDriver(browserType, gridUrl, headless);
            } else {
                driver = createLocalDriver(browserType, headless);
            }
            
            configureDriver(driver);
            return driver;
            
        } catch (Exception e) {
            logger.error("Failed to create WebDriver for browser: {}", browserType, e);
            throw new WebDriverException("WebDriver creation failed for browser: " + browserType, e);
        }
    }
    
    private static WebDriver createLocalDriver(BrowserType browserType, boolean headless) {
        switch (browserType) {
            case FIREFOX:
                io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver(getFirefoxOptions(headless));
            case EDGE:
                io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();
                return new EdgeDriver(getEdgeOptions(headless));
            case CHROME:
            default:
                io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
                return new ChromeDriver(getChromeOptions(headless));
        }
    }
    
    private static WebDriver createRemoteDriver(BrowserType browserType, String gridUrl, boolean headless) throws MalformedURLException {
        URL hubUrl = new URL(gridUrl);
        
        switch (browserType) {
            case FIREFOX:
                return new RemoteWebDriver(hubUrl, getFirefoxOptions(headless));
            case EDGE:
                return new RemoteWebDriver(hubUrl, getEdgeOptions(headless));
            case CHROME:
            default:
                return new RemoteWebDriver(hubUrl, getChromeOptions(headless));
        }
    }
    
    private static ChromeOptions getChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        return options;
    }
    
    private static FirefoxOptions getFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        return options;
    }
    
    private static EdgeOptions getEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        return options;
    }
    
    private static void configureDriver(WebDriver driver) {
        driver.manage().deleteAllCookies();
        
        int implicitWait = ConfigManager.getIntProperty("browser.timeout.implicit", 10);
        int pageLoadTimeout = ConfigManager.getIntProperty("browser.timeout.page.load", 30);
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().window().maximize();
        
        logger.debug("WebDriver configured with implicit wait: {}s, page load timeout: {}s", 
                    implicitWait, pageLoadTimeout);
    }

    public static void quitDriver() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver != null) {
            try {
                long threadId = Thread.currentThread().getId();
                driver.quit();
                webDriverThreadLocal.remove();
                driverInstances.remove(threadId);
                
                logger.info("WebDriver instance closed for thread {}", threadId);
            } catch (Exception e) {
                logger.error("Error while quitting WebDriver", e);
            }
        }
    }
    
    public static void quitAllDrivers() {
        logger.info("Shutting down all WebDriver instances. Total active: {}", driverInstances.size());
        
        driverInstances.values().parallelStream().forEach(driver -> {
            try {
                if (driver != null) {
                    driver.quit();
                }
            } catch (Exception e) {
                logger.error("Error quitting driver during shutdown", e);
            }
        });
        
        driverInstances.clear();
        
        // Clean up ThreadLocal to prevent memory leaks
        webDriverThreadLocal.remove();
        
        logger.info("All WebDriver instances have been shut down");
    }
    
    public static int getActiveDriverCount() {
        return driverInstances.size();
    }
    
    // Cleanup hook for JVM shutdown
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM shutdown detected, cleaning up WebDriver instances");
            quitAllDrivers();
        }));
    }
}
