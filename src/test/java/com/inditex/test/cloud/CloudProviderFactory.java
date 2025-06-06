package com.inditex.test.cloud;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.exceptions.WebDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating WebDriver instances for cloud providers
 * Supports Selenium Grid, BrowserStack, Sauce Labs, and other cloud services
 */
public class CloudProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(CloudProviderFactory.class);
    
    public enum CloudProvider {
        SELENIUM_GRID,
        BROWSERSTACK,
        SAUCE_LABS,
        LAMBDA_TEST,
        PERFECTO,
        CROSSBROWSERTESTING
    }
    
    public enum Platform {
        WINDOWS_10("Windows 10"),
        WINDOWS_11("Windows 11"),
        MACOS_MONTEREY("macOS Monterey"),
        MACOS_VENTURA("macOS Ventura"),
        MACOS_SONOMA("macOS Sonoma"),
        LINUX("Linux");
        
        private final String platformName;
        
        Platform(String platformName) {
            this.platformName = platformName;
        }
        
        public String getPlatformName() {
            return platformName;
        }
    }
    
    public static WebDriver createCloudDriver(CloudProvider provider, String browser, String browserVersion, 
                                            Platform platform, Map<String, String> options) {
        logger.info("Creating cloud WebDriver - Provider: {}, Browser: {} {}, Platform: {}", 
                   provider, browser, browserVersion, platform.getPlatformName());
        
        try {
            return switch (provider) {
                case SELENIUM_GRID -> createSeleniumGridDriver(browser, browserVersion, platform, options);
                case BROWSERSTACK -> createBrowserStackDriver(browser, browserVersion, platform, options);
                case SAUCE_LABS -> createSauceLabsDriver(browser, browserVersion, platform, options);
                case LAMBDA_TEST -> createLambdaTestDriver(browser, browserVersion, platform, options);
                case PERFECTO -> createPerfectoDriver(browser, browserVersion, platform, options);
                case CROSSBROWSERTESTING -> createCrossBrowserTestingDriver(browser, browserVersion, platform, options);
            };
        } catch (Exception e) {
            logger.error("Failed to create cloud WebDriver: {}", e.getMessage(), e);
            throw new WebDriverException("Failed to create cloud WebDriver", e);
        }
    }
    
    private static WebDriver createSeleniumGridDriver(String browser, String browserVersion, 
                                                     Platform platform, Map<String, String> options) 
            throws MalformedURLException {
        String gridUrl = ConfigManager.getProperty("selenium.grid.url", "http://localhost:4444/wd/hub");
        
        var capabilities = switch (browser.toLowerCase()) {
            case "chrome" -> {
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setPlatformName(platform.getPlatformName());
                chromeOptions.setBrowserVersion(browserVersion);
                addCommonGridOptions(chromeOptions, options);
                yield chromeOptions;
            }
            case "firefox" -> {
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setPlatformName(platform.getPlatformName());
                firefoxOptions.setBrowserVersion(browserVersion);
                addCommonGridOptions(firefoxOptions, options);
                yield firefoxOptions;
            }
            case "edge" -> {
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.setPlatformName(platform.getPlatformName());
                edgeOptions.setBrowserVersion(browserVersion);
                addCommonGridOptions(edgeOptions, options);
                yield edgeOptions;
            }
            case "safari" -> {
                SafariOptions safariOptions = new SafariOptions();
                safariOptions.setPlatformName(platform.getPlatformName());
                safariOptions.setBrowserVersion(browserVersion);
                addCommonGridOptions(safariOptions, options);
                yield safariOptions;
            }
            default -> throw new WebDriverException("Unsupported browser for Selenium Grid: " + browser);
        };
        
        return new RemoteWebDriver(new URL(gridUrl), capabilities);
    }
    
    private static WebDriver createBrowserStackDriver(String browser, String browserVersion, 
                                                     Platform platform, Map<String, String> options) 
            throws MalformedURLException {
        String username = ConfigManager.getEncryptedProperty("browserstack.username");
        String accessKey = ConfigManager.getEncryptedProperty("browserstack.access.key");
        String hubUrl = String.format("https://%s:%s@hub-cloud.browserstack.com/wd/hub", username, accessKey);
        
        var capabilities = createBrowserStackCapabilities(browser, browserVersion, platform, options);
        
        return new RemoteWebDriver(new URL(hubUrl), capabilities);
    }
    
    private static WebDriver createSauceLabsDriver(String browser, String browserVersion, 
                                                  Platform platform, Map<String, String> options) 
            throws MalformedURLException {
        String username = ConfigManager.getEncryptedProperty("saucelabs.username");
        String accessKey = ConfigManager.getEncryptedProperty("saucelabs.access.key");
        String dataCenter = ConfigManager.getProperty("saucelabs.datacenter", "us-west-1");
        String hubUrl = String.format("https://%s:%s@ondemand.%s.saucelabs.com:443/wd/hub", 
                                     username, accessKey, dataCenter);
        
        var capabilities = createSauceLabsCapabilities(browser, browserVersion, platform, options);
        
        return new RemoteWebDriver(new URL(hubUrl), capabilities);
    }
    
    private static WebDriver createLambdaTestDriver(String browser, String browserVersion, 
                                                   Platform platform, Map<String, String> options) 
            throws MalformedURLException {
        String username = ConfigManager.getEncryptedProperty("lambdatest.username");
        String accessKey = ConfigManager.getEncryptedProperty("lambdatest.access.key");
        String hubUrl = String.format("https://%s:%s@hub.lambdatest.com/wd/hub", username, accessKey);
        
        var capabilities = createLambdaTestCapabilities(browser, browserVersion, platform, options);
        
        return new RemoteWebDriver(new URL(hubUrl), capabilities);
    }
    
    private static WebDriver createPerfectoDriver(String browser, String browserVersion, 
                                                 Platform platform, Map<String, String> options) 
            throws MalformedURLException {
        String cloudName = ConfigManager.getProperty("perfecto.cloud.name");
        String securityToken = ConfigManager.getEncryptedProperty("perfecto.security.token");
        String hubUrl = String.format("https://%s.perfectomobile.com/nexperience/perfectomobile/wd/hub", cloudName);
        
        var capabilities = createPerfectoCapabilities(browser, browserVersion, platform, options, securityToken);
        
        return new RemoteWebDriver(new URL(hubUrl), capabilities);
    }
    
    private static WebDriver createCrossBrowserTestingDriver(String browser, String browserVersion, 
                                                           Platform platform, Map<String, String> options) 
            throws MalformedURLException {
        String username = ConfigManager.getEncryptedProperty("crossbrowsertesting.username");
        String authKey = ConfigManager.getEncryptedProperty("crossbrowsertesting.auth.key");
        String hubUrl = String.format("http://%s:%s@hub.crossbrowsertesting.com:80/wd/hub", username, authKey);
        
        var capabilities = createCrossBrowserTestingCapabilities(browser, browserVersion, platform, options);
        
        return new RemoteWebDriver(new URL(hubUrl), capabilities);
    }
    
    private static void addCommonGridOptions(org.openqa.selenium.MutableCapabilities capabilities, 
                                           Map<String, String> options) {
        // Add test name and build information
        String testName = options.getOrDefault("testName", "InditexPromotionsTest");
        String buildName = options.getOrDefault("buildName", "Build-" + System.currentTimeMillis());
        
        capabilities.setCapability("se:name", testName);
        capabilities.setCapability("se:build", buildName);
        
        // Add video recording if enabled
        if (ConfigManager.getBooleanProperty("grid.video.enabled", false)) {
            capabilities.setCapability("se:recordVideo", true);
        }
        
        // Add VNC viewing if enabled
        if (ConfigManager.getBooleanProperty("grid.vnc.enabled", false)) {
            capabilities.setCapability("se:vncEnabled", true);
        }
    }
    
    private static ChromeOptions createBrowserStackCapabilities(String browser, String browserVersion, 
                                                               Platform platform, Map<String, String> options) {
        ChromeOptions capabilities = new ChromeOptions();
        
        Map<String, Object> bsOptions = new HashMap<>();
        bsOptions.put("os", getPlatformForBrowserStack(platform));
        bsOptions.put("osVersion", getPlatformVersionForBrowserStack(platform));
        bsOptions.put("browserVersion", browserVersion);
        bsOptions.put("sessionName", options.getOrDefault("testName", "InditexPromotionsTest"));
        bsOptions.put("buildName", options.getOrDefault("buildName", "Build-" + System.currentTimeMillis()));
        bsOptions.put("projectName", options.getOrDefault("projectName", "InditexPromotionsTest"));
        bsOptions.put("debug", true);
        bsOptions.put("networkLogs", true);
        bsOptions.put("consoleLogs", "info");
        bsOptions.put("seleniumVersion", "4.15.0");
        
        capabilities.setCapability("bstack:options", bsOptions);
        
        return capabilities;
    }
    
    private static ChromeOptions createSauceLabsCapabilities(String browser, String browserVersion, 
                                                            Platform platform, Map<String, String> options) {
        ChromeOptions capabilities = new ChromeOptions();
        
        Map<String, Object> sauceOptions = new HashMap<>();
        sauceOptions.put("browserName", browser);
        sauceOptions.put("browserVersion", browserVersion);
        sauceOptions.put("platformName", platform.getPlatformName());
        sauceOptions.put("name", options.getOrDefault("testName", "InditexPromotionsTest"));
        sauceOptions.put("build", options.getOrDefault("buildName", "Build-" + System.currentTimeMillis()));
        sauceOptions.put("tags", java.util.List.of("promotional", "regression"));
        sauceOptions.put("recordVideo", true);
        sauceOptions.put("recordScreenshots", true);
        sauceOptions.put("extendedDebugging", true);
        sauceOptions.put("capturePerformance", true);
        
        capabilities.setCapability("sauce:options", sauceOptions);
        
        return capabilities;
    }
    
    private static ChromeOptions createLambdaTestCapabilities(String browser, String browserVersion, 
                                                             Platform platform, Map<String, String> options) {
        ChromeOptions capabilities = new ChromeOptions();
        
        Map<String, Object> ltOptions = new HashMap<>();
        ltOptions.put("browserName", browser);
        ltOptions.put("browserVersion", browserVersion);
        ltOptions.put("platformName", platform.getPlatformName());
        ltOptions.put("name", options.getOrDefault("testName", "InditexPromotionsTest"));
        ltOptions.put("build", options.getOrDefault("buildName", "Build-" + System.currentTimeMillis()));
        ltOptions.put("project", options.getOrDefault("projectName", "InditexPromotionsTest"));
        ltOptions.put("video", true);
        ltOptions.put("screenshot", true);
        ltOptions.put("network", true);
        ltOptions.put("console", true);
        ltOptions.put("w3c", true);
        ltOptions.put("plugin", "java-testNG");
        
        capabilities.setCapability("LT:Options", ltOptions);
        
        return capabilities;
    }
    
    private static ChromeOptions createPerfectoCapabilities(String browser, String browserVersion, 
                                                           Platform platform, Map<String, String> options, 
                                                           String securityToken) {
        ChromeOptions capabilities = new ChromeOptions();
        
        capabilities.setCapability("securityToken", securityToken);
        capabilities.setCapability("browserName", browser);
        capabilities.setCapability("browserVersion", browserVersion);
        capabilities.setCapability("platformName", platform.getPlatformName());
        capabilities.setCapability("scriptName", options.getOrDefault("testName", "InditexPromotionsTest"));
        capabilities.setCapability("testName", options.getOrDefault("buildName", "Build-" + System.currentTimeMillis()));
        capabilities.setCapability("takeScreenshot", "true");
        capabilities.setCapability("screenshotOnError", "true");
        
        return capabilities;
    }
    
    private static ChromeOptions createCrossBrowserTestingCapabilities(String browser, String browserVersion, 
                                                                      Platform platform, Map<String, String> options) {
        ChromeOptions capabilities = new ChromeOptions();
        
        capabilities.setCapability("browserName", browser);
        capabilities.setCapability("version", browserVersion);
        capabilities.setCapability("platform", platform.getPlatformName());
        capabilities.setCapability("name", options.getOrDefault("testName", "InditexPromotionsTest"));
        capabilities.setCapability("build", options.getOrDefault("buildName", "Build-" + System.currentTimeMillis()));
        capabilities.setCapability("record_video", "true");
        capabilities.setCapability("record_network", "true");
        capabilities.setCapability("max_duration", "3600");
        
        return capabilities;
    }
    
    private static String getPlatformForBrowserStack(Platform platform) {
        return switch (platform) {
            case WINDOWS_10, WINDOWS_11 -> "Windows";
            case MACOS_MONTEREY, MACOS_VENTURA, MACOS_SONOMA -> "OS X";
            case LINUX -> "Linux";
        };
    }
    
    private static String getPlatformVersionForBrowserStack(Platform platform) {
        return switch (platform) {
            case WINDOWS_10 -> "10";
            case WINDOWS_11 -> "11";
            case MACOS_MONTEREY -> "Monterey";
            case MACOS_VENTURA -> "Ventura";
            case MACOS_SONOMA -> "Sonoma";
            case LINUX -> "ubuntu";
        };
    }
    
    public static Map<String, String> createTestOptions(String testName, String buildName, String projectName) {
        Map<String, String> options = new HashMap<>();
        options.put("testName", testName);
        options.put("buildName", buildName);
        options.put("projectName", projectName);
        return options;
    }
}