package com.inditex.test.parallel;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.exceptions.WebDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe WebDriver factory optimized for parallel test execution
 */
public class ParallelWebDriverFactory {
    private static final Logger logger = LoggerFactory.getLogger(ParallelWebDriverFactory.class);
    
    private static final String WEBDRIVER_THREADLOCAL_KEY = "webdriver";
    private static final ConcurrentHashMap<Long, WebDriverInfo> driverRegistry = new ConcurrentHashMap<>();
    private static final AtomicInteger driverInstanceCounter = new AtomicInteger(0);
    
    public enum BrowserType {
        CHROME("chrome"),
        FIREFOX("firefox"),
        EDGE("edge"),
        SAFARI("safari");
        
        private final String browserName;
        
        BrowserType(String browserName) {
            this.browserName = browserName;
        }
        
        public String getBrowserName() {
            return browserName;
        }
        
        public static BrowserType fromString(String browser) {
            for (BrowserType type : BrowserType.values()) {
                if (type.browserName.equalsIgnoreCase(browser)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown browser type: " + browser);
        }
    }
    
    public static WebDriver getDriver() {
        return getDriver(null, false, null);
    }
    
    public static WebDriver getDriver(BrowserType browserType, boolean headless, String gridUrl) {
        // Register thread if not already registered
        if (!ThreadLocalManager.getActiveThreads().containsKey(Thread.currentThread().getId())) {
            ThreadLocalManager.registerThread("TestExecution");
        }
        
        ThreadLocal<WebDriver> driverThreadLocal = ThreadLocalManager.getOrCreateThreadLocal(
            WEBDRIVER_THREADLOCAL_KEY, 
            () -> createWebDriver(browserType, headless, gridUrl)
        );
        
        WebDriver driver = driverThreadLocal.get();
        
        if (driver == null) {
            driver = createWebDriver(browserType, headless, gridUrl);
            driverThreadLocal.set(driver);
        }
        
        return driver;
    }
    
    private static WebDriver createWebDriver(BrowserType browserType, boolean headless, String gridUrl) {
        long threadId = Thread.currentThread().getId();
        int instanceId = driverInstanceCounter.incrementAndGet();
        
        // Determine browser type from configuration if not specified
        if (browserType == null) {
            String browserName = ConfigManager.getProperty("browser.default", "chrome");
            browserType = BrowserType.fromString(browserName);
        }
        
        // Determine headless mode from configuration if not specified
        if (!headless) {
            headless = ConfigManager.getBooleanProperty("browser.headless", false);
        }
        
        // Determine grid URL from configuration if not specified
        if (gridUrl == null) {
            gridUrl = ConfigManager.getProperty("grid.url", "");
        }
        
        logger.info("Creating WebDriver instance #{} for thread {} - Browser: {}, Headless: {}, Grid: {}", 
                   instanceId, threadId, browserType, headless, !gridUrl.isEmpty());
        
        try {
            WebDriver driver;
            
            if (!gridUrl.isEmpty()) {
                driver = createRemoteDriver(browserType, headless, gridUrl);
            } else {
                driver = createLocalDriver(browserType, headless);
            }
            
            configureDriver(driver);
            
            // Register driver info
            WebDriverInfo driverInfo = new WebDriverInfo(instanceId, threadId, browserType, headless, gridUrl);
            driverRegistry.put(threadId, driverInfo);
            
            logger.info("WebDriver instance #{} created successfully for thread {}", instanceId, threadId);
            return driver;
            
        } catch (Exception e) {
            logger.error("Failed to create WebDriver instance #{} for thread {}: {}", 
                        instanceId, threadId, e.getMessage(), e);
            throw new WebDriverException("Failed to create WebDriver", e);
        }
    }
    
    private static WebDriver createLocalDriver(BrowserType browserType, boolean headless) {
        return switch (browserType) {
            case CHROME -> {
                io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
                yield new ChromeDriver(getChromeOptions(headless));
            }
            case FIREFOX -> {
                io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
                yield new FirefoxDriver(getFirefoxOptions(headless));
            }
            case EDGE -> {
                io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();
                yield new EdgeDriver(getEdgeOptions(headless));
            }
            default -> throw new WebDriverException("Unsupported browser for local execution: " + browserType);
        };
    }
    
    private static WebDriver createRemoteDriver(BrowserType browserType, boolean headless, String gridUrl) 
            throws MalformedURLException {
        URL hubUrl = new URL(gridUrl);
        
        return switch (browserType) {
            case CHROME -> new RemoteWebDriver(hubUrl, getChromeOptions(headless));
            case FIREFOX -> new RemoteWebDriver(hubUrl, getFirefoxOptions(headless));
            case EDGE -> new RemoteWebDriver(hubUrl, getEdgeOptions(headless));
            default -> throw new WebDriverException("Unsupported browser for remote execution: " + browserType);
        };
    }
    
    private static ChromeOptions getChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        
        // Performance optimizations for parallel execution
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-images");
        options.addArguments("--disable-javascript");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-ipc-flooding-protection");
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-sync");
        options.addArguments("--metrics-recording-only");
        options.addArguments("--no-first-run");
        options.addArguments("--safebrowsing-disable-auto-update");
        options.addArguments("--disable-default-apps");
        options.addArguments("--mute-audio");
        
        // Window management
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        } else {
            options.addArguments("--start-maximized");
        }
        
        // Memory management
        options.addArguments("--max_old_space_size=4096");
        options.addArguments("--memory-pressure-off");
        
        // Security
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        
        return options;
    }
    
    private static FirefoxOptions getFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Performance optimizations
        options.addPreference("dom.disable_beforeunload", true);
        options.addPreference("browser.tabs.remote.autostart", false);
        options.addPreference("browser.startup.homepage", "about:blank");
        options.addPreference("media.volume_scale", "0.0");
        
        return options;
    }
    
    private static EdgeOptions getEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        
        return options;
    }
    
    private static void configureDriver(WebDriver driver) {
        // Configure timeouts
        int implicitWait = ConfigManager.getIntProperty("browser.timeout.implicit", 10);
        int pageLoadTimeout = ConfigManager.getIntProperty("browser.timeout.page.load", 30);
        int scriptTimeout = ConfigManager.getIntProperty("browser.timeout.script", 30);
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(scriptTimeout));
        
        // Clear cookies and storage
        driver.manage().deleteAllCookies();
        
        logger.debug("WebDriver configured with timeouts - Implicit: {}s, PageLoad: {}s, Script: {}s", 
                    implicitWait, pageLoadTimeout, scriptTimeout);
    }
    
    public static void quitDriver() {
        long threadId = Thread.currentThread().getId();
        WebDriver driver = ThreadLocalManager.get(WEBDRIVER_THREADLOCAL_KEY);
        
        if (driver != null) {
            try {
                WebDriverInfo driverInfo = driverRegistry.get(threadId);
                if (driverInfo != null) {
                    logger.info("Quitting WebDriver instance #{} for thread {}", 
                               driverInfo.getInstanceId(), threadId);
                } else {
                    logger.info("Quitting WebDriver for thread {}", threadId);
                }
                
                driver.quit();
                ThreadLocalManager.remove(WEBDRIVER_THREADLOCAL_KEY);
                driverRegistry.remove(threadId);
                
                logger.debug("WebDriver successfully quit for thread {}", threadId);
                
            } catch (Exception e) {
                logger.error("Error quitting WebDriver for thread {}: {}", threadId, e.getMessage(), e);
            }
        }
    }
    
    public static void quitAllDrivers() {
        logger.info("Shutting down all WebDriver instances - Total active: {}", driverRegistry.size());
        
        driverRegistry.keySet().parallelStream().forEach(threadId -> {
            try {
                WebDriver driver = ThreadLocalManager.get(WEBDRIVER_THREADLOCAL_KEY);
                if (driver != null) {
                    driver.quit();
                }
            } catch (Exception e) {
                logger.error("Error quitting driver for thread {}: {}", threadId, e.getMessage());
            }
        });
        
        driverRegistry.clear();
        ThreadLocalManager.cleanupAllThreadLocals();
        
        logger.info("All WebDriver instances have been shut down");
    }
    
    public static int getActiveDriverCount() {
        return driverRegistry.size();
    }
    
    public static ConcurrentHashMap<Long, WebDriverInfo> getDriverRegistry() {
        return new ConcurrentHashMap<>(driverRegistry);
    }
    
    public static WebDriverInfo getCurrentDriverInfo() {
        return driverRegistry.get(Thread.currentThread().getId());
    }
    
    /**
     * WebDriver information data class
     */
    public static class WebDriverInfo {
        private final int instanceId;
        private final long threadId;
        private final BrowserType browserType;
        private final boolean headless;
        private final String gridUrl;
        private final long creationTime;
        
        public WebDriverInfo(int instanceId, long threadId, BrowserType browserType, 
                           boolean headless, String gridUrl) {
            this.instanceId = instanceId;
            this.threadId = threadId;
            this.browserType = browserType;
            this.headless = headless;
            this.gridUrl = gridUrl;
            this.creationTime = System.currentTimeMillis();
        }
        
        public int getInstanceId() { return instanceId; }
        public long getThreadId() { return threadId; }
        public BrowserType getBrowserType() { return browserType; }
        public boolean isHeadless() { return headless; }
        public String getGridUrl() { return gridUrl; }
        public long getCreationTime() { return creationTime; }
        public boolean isRemote() { return gridUrl != null && !gridUrl.isEmpty(); }
        
        @Override
        public String toString() {
            return String.format("WebDriverInfo{id=%d, thread=%d, browser=%s, headless=%s, remote=%s}", 
                               instanceId, threadId, browserType, headless, isRemote());
        }
    }
}