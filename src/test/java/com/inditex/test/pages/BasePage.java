package com.inditex.test.pages;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.exceptions.PageObjectException;
import com.inditex.test.security.SecurityUtils;
import com.inditex.test.utils.WebDriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class BasePage {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    
    public BasePage() {
        this.driver = Objects.requireNonNull(WebDriverFactory.getDriver(), "WebDriver cannot be null");
        
        int explicitWait = ConfigManager.getIntProperty("browser.timeout.explicit", 15);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        this.actions = new Actions(driver);
        
        PageFactory.initElements(driver, this);
        logger.debug("Initialized {} with explicit wait: {}s", this.getClass().getSimpleName(), explicitWait);
    }
    
    public BasePage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "WebDriver cannot be null");
        
        int explicitWait = ConfigManager.getIntProperty("browser.timeout.explicit", 15);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        this.actions = new Actions(driver);
        
        PageFactory.initElements(driver, this);
        logger.debug("Initialized {} with provided WebDriver", this.getClass().getSimpleName());
    }

    public abstract String getPageTitle();
    
    public abstract String getPageUrl();
    
    public abstract boolean isLoaded();
    
    protected WebElement waitForElement(WebElement element) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for element to be visible: {}", element);
            throw new PageObjectException("Element not visible within timeout period", e);
        }
    }
    
    protected WebElement waitForElementToBeClickable(WebElement element) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for element to be clickable: {}", element);
            throw new PageObjectException("Element not clickable within timeout period", e);
        }
    }
    
    protected List<WebElement> waitForElements(By locator) {
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (TimeoutException e) {
            logger.error("Timeout waiting for elements: {}", locator);
            throw new PageObjectException("Elements not found within timeout period", e);
        }
    }
    
    protected void safeClick(WebElement element, String elementName) {
        handleStaleElementException(
            () -> waitForElementToBeClickable(element),
            () -> {
                element.click();
                logger.debug("Clicked on element: {}", elementName);
            }
        );
    }
    
    protected void safeSendKeys(WebElement element, String text, String elementName) {
        if (ConfigManager.getBooleanProperty("security.input.validation.enabled", true)) {
            SecurityUtils.validateInput(text, elementName);
        }
        
        handleStaleElementException(
            () -> waitForElement(element),
            () -> {
                element.clear();
                element.sendKeys(text);
                logger.debug("Entered text in element: {} (length: {})", elementName, text.length());
            }
        );
    }
    
    protected String safeGetText(WebElement element, String elementName) {
        return handleStaleElementException(
            () -> waitForElement(element),
            () -> {
                String text = element.getText();
                logger.debug("Retrieved text from element {}: {}", elementName, text);
                return text;
            }
        );
    }
    
    protected String safeGetAttribute(WebElement element, String attribute, String elementName) {
        return handleStaleElementException(
            () -> waitForElement(element),
            () -> {
                String value = element.getAttribute(attribute);
                logger.debug("Retrieved attribute {} from element {}: {}", attribute, elementName, value);
                return value;
            }
        );
    }
    
    protected boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
    
    protected void handleStaleElementException(Supplier<WebElement> elementSupplier, Runnable action) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                elementSupplier.get();
                action.run();
                return;
            } catch (StaleElementReferenceException e) {
                logger.warn("StaleElementReferenceException caught, attempt {} of {}", i + 1, maxRetries);
                if (i == maxRetries - 1) {
                    throw new PageObjectException("Element became stale after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PageObjectException("Thread interrupted during retry", ie);
                }
            }
        }
    }
    
    protected <T> T handleStaleElementException(Supplier<WebElement> elementSupplier, Supplier<T> action) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                elementSupplier.get();
                return action.get();
            } catch (StaleElementReferenceException e) {
                logger.warn("StaleElementReferenceException caught, attempt {} of {}", i + 1, maxRetries);
                if (i == maxRetries - 1) {
                    throw new PageObjectException("Element became stale after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PageObjectException("Thread interrupted during retry", ie);
                }
            }
        }
        return null;
    }
    
    public void navigateTo(String url) {
        if (!SecurityUtils.isValidUrl(url)) {
            throw new PageObjectException("Invalid URL provided: " + url);
        }
        
        try {
            logger.info("Navigating to URL: {}", url);
            driver.get(url);
            
            // Wait for page to load
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
            
            logger.debug("Page loaded successfully: {}", url);
        } catch (Exception e) {
            logger.error("Failed to navigate to URL: {}", url, e);
            throw new PageObjectException("Navigation failed to URL: " + url, e);
        }
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    public String getCurrentTitle() {
        return driver.getTitle();
    }
    
    public void refreshPage() {
        logger.info("Refreshing current page: {}", getCurrentUrl());
        driver.navigate().refresh();
    }
    
    public void goBack() {
        logger.info("Navigating back from: {}", getCurrentUrl());
        driver.navigate().back();
    }
    
    public void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            logger.debug("Scrolled to element: {}", element);
        } catch (Exception e) {
            logger.warn("Failed to scroll to element", e);
        }
    }
    
    public void waitForPageLoad() {
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
            .executeScript("return document.readyState").equals("complete"));
    }
}
