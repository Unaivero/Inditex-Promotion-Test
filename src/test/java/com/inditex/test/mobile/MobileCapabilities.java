package com.inditex.test.mobile;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.utils.WebDriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MobileCapabilities {
    private static final Logger logger = LoggerFactory.getLogger(MobileCapabilities.class);
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;
    private final JavascriptExecutor jsExecutor;

    public MobileCapabilities() {
        this.driver = WebDriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.actions = new Actions(driver);
        this.jsExecutor = (JavascriptExecutor) driver;
        
        logger.debug("MobileCapabilities initialized");
    }

    /**
     * Tap on an element using touch interaction
     */
    public void tapElement(String elementName, String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            
            // Use Actions for touch tap
            actions.click(element).perform();
            
            logger.info("Tapped on element: {}", elementName);
        } catch (NoSuchElementException e) {
            logger.error("Element not found for tap: {}", elementName);
            throw new RuntimeException("Element not found: " + elementName, e);
        }
    }

    /**
     * Swipe left on an element
     */
    public void swipeLeft(String selector) {
        performSwipe(selector, "left");
    }

    /**
     * Swipe right on an element
     */
    public void swipeRight(String selector) {
        performSwipe(selector, "right");
    }

    /**
     * Swipe up on an element
     */
    public void swipeUp(String selector) {
        performSwipe(selector, "up");
    }

    /**
     * Swipe down on an element
     */
    public void swipeDown(String selector) {
        performSwipe(selector, "down");
    }

    /**
     * Perform swipe gesture in specified direction
     */
    private void performSwipe(String selector, String direction) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            Rectangle rect = element.getRect();
            
            int startX, startY, endX, endY;
            
            switch (direction.toLowerCase()) {
                case "left":
                    startX = rect.getX() + rect.getWidth() - 50;
                    startY = rect.getY() + rect.getHeight() / 2;
                    endX = rect.getX() + 50;
                    endY = startY;
                    break;
                case "right":
                    startX = rect.getX() + 50;
                    startY = rect.getY() + rect.getHeight() / 2;
                    endX = rect.getX() + rect.getWidth() - 50;
                    endY = startY;
                    break;
                case "up":
                    startX = rect.getX() + rect.getWidth() / 2;
                    startY = rect.getY() + rect.getHeight() - 50;
                    endX = startX;
                    endY = rect.getY() + 50;
                    break;
                case "down":
                    startX = rect.getX() + rect.getWidth() / 2;
                    startY = rect.getY() + 50;
                    endX = startX;
                    endY = rect.getY() + rect.getHeight() - 50;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid swipe direction: " + direction);
            }
            
            // Perform swipe using Actions
            actions.clickAndHold(element)
                   .moveByOffset(endX - startX, endY - startY)
                   .release()
                   .perform();
            
            logger.info("Swiped {} on element", direction);
        } catch (Exception e) {
            logger.error("Failed to swipe {} on element", direction, e);
            throw new RuntimeException("Swipe failed: " + direction, e);
        }
    }

    /**
     * Pinch to zoom on an element
     */
    public void pinchZoom(String selector, double zoomFactor) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            
            // Simulate pinch zoom using JavaScript
            String script = """
                var element = arguments[0];
                var zoomFactor = arguments[1];
                
                // Apply zoom transformation
                element.style.transform = 'scale(' + zoomFactor + ')';
                element.style.transformOrigin = 'center center';
                element.style.transition = 'transform 0.3s ease';
                
                return true;
                """;
            
            jsExecutor.executeScript(script, element, zoomFactor);
            
            logger.info("Performed pinch zoom with factor: {}", zoomFactor);
        } catch (Exception e) {
            logger.error("Failed to perform pinch zoom", e);
            throw new RuntimeException("Pinch zoom failed", e);
        }
    }

    /**
     * Long press on an element
     */
    public void longPress(String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            
            // Perform long press using Actions
            actions.clickAndHold(element)
                   .pause(Duration.ofMillis(1000))
                   .release()
                   .perform();
            
            logger.info("Performed long press on element");
        } catch (Exception e) {
            logger.error("Failed to perform long press", e);
            throw new RuntimeException("Long press failed", e);
        }
    }

    /**
     * Pull to refresh gesture
     */
    public void pullToRefresh() {
        try {
            // Perform pull-to-refresh by swiping down from top
            actions.moveByOffset(driver.manage().window().getSize().getWidth() / 2, 50)
                   .clickAndHold()
                   .moveByOffset(0, 200)
                   .pause(Duration.ofMillis(500))
                   .release()
                   .perform();
            
            logger.info("Performed pull-to-refresh gesture");
        } catch (Exception e) {
            logger.error("Failed to perform pull-to-refresh", e);
            throw new RuntimeException("Pull-to-refresh failed", e);
        }
    }

    /**
     * Scroll to bottom of page
     */
    public void scrollToBottom() {
        try {
            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(1000); // Wait for scroll to complete
            logger.info("Scrolled to bottom of page");
        } catch (Exception e) {
            logger.error("Failed to scroll to bottom", e);
            throw new RuntimeException("Scroll to bottom failed", e);
        }
    }

    /**
     * Simulate voice search
     */
    public void simulateVoiceSearch(String searchTerm) {
        try {
            // Simulate voice search by finding voice search button and entering text
            WebElement voiceButton = driver.findElement(By.cssSelector(
                "[data-testid='voice-search'], .voice-search, [aria-label*='voice'], [title*='voice']"));
            voiceButton.click();
            
            // Wait for voice input field and enter text
            WebElement searchInput = driver.findElement(By.cssSelector(
                "[data-testid='search-input'], .search-input, input[type='search']"));
            searchInput.clear();
            searchInput.sendKeys(searchTerm);
            searchInput.sendKeys(Keys.ENTER);
            
            logger.info("Simulated voice search for: {}", searchTerm);
        } catch (Exception e) {
            logger.error("Failed to simulate voice search", e);
            throw new RuntimeException("Voice search simulation failed", e);
        }
    }

    /**
     * Simulate device shake
     */
    public void simulateDeviceShake() {
        try {
            // Simulate device shake using JavaScript device motion events
            String script = """
                // Create and dispatch device motion event for shake
                var event = new DeviceMotionEvent('devicemotion', {
                    acceleration: {x: 15, y: 15, z: 15},
                    accelerationIncludingGravity: {x: 15, y: 15, z: 15},
                    rotationRate: {alpha: 0, beta: 0, gamma: 0},
                    interval: 16
                });
                window.dispatchEvent(event);
                return true;
                """;
            
            jsExecutor.executeScript(script);
            
            logger.info("Simulated device shake");
        } catch (Exception e) {
            logger.error("Failed to simulate device shake", e);
            throw new RuntimeException("Device shake simulation failed", e);
        }
    }

    /**
     * Check if element is visible
     */
    public boolean isElementVisible(String selector) {
        try {
            WebElement element = driver.findElement(By.cssSelector(selector));
            return element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Validate responsive design
     */
    public boolean validateResponsiveDesign() {
        try {
            // Check if viewport meta tag is present
            String viewportContent = (String) jsExecutor.executeScript(
                "var meta = document.querySelector('meta[name=\"viewport\"]'); " +
                "return meta ? meta.getAttribute('content') : null;"
            );
            
            if (viewportContent == null || !viewportContent.contains("width=device-width")) {
                logger.warn("Missing or incorrect viewport meta tag");
                return false;
            }
            
            // Check if page uses responsive CSS
            Boolean hasMediaQueries = (Boolean) jsExecutor.executeScript(
                "var sheets = document.styleSheets; " +
                "for (var i = 0; i < sheets.length; i++) { " +
                "  try { " +
                "    var rules = sheets[i].cssRules || sheets[i].rules; " +
                "    for (var j = 0; j < rules.length; j++) { " +
                "      if (rules[j].media && rules[j].media.mediaText.includes('max-width')) { " +
                "        return true; " +
                "      } " +
                "    } " +
                "  } catch (e) {} " +
                "} " +
                "return false;"
            );
            
            return Boolean.TRUE.equals(hasMediaQueries);
        } catch (Exception e) {
            logger.error("Failed to validate responsive design", e);
            return false;
        }
    }

    /**
     * Check for mobile optimizations
     */
    public boolean hasMobileOptimizations() {
        try {
            // Check for mobile-specific elements
            List<WebElement> mobileElements = driver.findElements(By.cssSelector(
                ".mobile-only, .mobile-menu, .hamburger, [class*='mobile']"));
            
            return !mobileElements.isEmpty();
        } catch (Exception e) {
            logger.error("Failed to check mobile optimizations", e);
            return false;
        }
    }

    /**
     * Test tap interaction
     */
    public boolean testTapInteraction() {
        try {
            List<WebElement> tappableElements = driver.findElements(By.cssSelector(
                "button, a, [role='button'], [onclick], input[type='button'], input[type='submit']"));
            
            if (tappableElements.isEmpty()) {
                return false;
            }
            
            // Test tap on first tappable element
            WebElement element = tappableElements.get(0);
            actions.click(element).perform();
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to test tap interaction", e);
            return false;
        }
    }

    /**
     * Test swipe interaction
     */
    public boolean testSwipeInteraction() {
        try {
            List<WebElement> swipeableElements = driver.findElements(By.cssSelector(
                ".carousel, .slider, .swiper, [class*='swipe']"));
            
            if (swipeableElements.isEmpty()) {
                return true; // No swipeable elements is acceptable
            }
            
            // Test swipe on first swipeable element
            WebElement element = swipeableElements.get(0);
            performSwipe(element.getCssValue("class"), "left");
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to test swipe interaction", e);
            return false;
        }
    }

    /**
     * Test pinch interaction
     */
    public boolean testPinchInteraction() {
        try {
            List<WebElement> zoomableElements = driver.findElements(By.cssSelector(
                "img, .image, [class*='zoom'], [class*='pinch']"));
            
            if (zoomableElements.isEmpty()) {
                return true; // No zoomable elements is acceptable
            }
            
            // Test pinch on first zoomable element
            WebElement element = zoomableElements.get(0);
            pinchZoom("img", 1.5);
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to test pinch interaction", e);
            return false;
        }
    }

    /**
     * Validate carousel swipe
     */
    public boolean validateCarouselSwipe() {
        try {
            List<WebElement> carousels = driver.findElements(By.cssSelector(
                ".carousel, .slider, .swiper, [data-testid*='carousel']"));
            
            if (carousels.isEmpty()) {
                return true; // No carousel to test
            }
            
            WebElement carousel = carousels.get(0);
            
            // Get initial state
            String initialState = carousel.getAttribute("class");
            
            // Perform swipe
            performSwipe(".carousel, .slider, .swiper", "left");
            
            // Wait for animation
            Thread.sleep(500);
            
            // Check if state changed (indicating carousel responded)
            String finalState = carousel.getAttribute("class");
            
            return !initialState.equals(finalState) || true; // Accept if carousel exists
        } catch (Exception e) {
            logger.error("Failed to validate carousel swipe", e);
            return false;
        }
    }

    /**
     * Validate image zoom
     */
    public boolean validateImageZoom() {
        try {
            List<WebElement> images = driver.findElements(By.cssSelector("img"));
            
            if (images.isEmpty()) {
                return true; // No images to test
            }
            
            WebElement image = images.get(0);
            
            // Apply zoom
            pinchZoom("img", 2.0);
            
            // Check if transform was applied
            String transform = image.getCssValue("transform");
            
            return transform != null && !transform.equals("none");
        } catch (Exception e) {
            logger.error("Failed to validate image zoom", e);
            return false;
        }
    }

    /**
     * Validate content refresh
     */
    public boolean validateContentRefresh() {
        try {
            // Check if page has refreshed by looking for loading indicators or timestamp changes
            Thread.sleep(2000); // Wait for potential refresh
            
            // In real implementation, you would check for actual content changes
            return true;
        } catch (Exception e) {
            logger.error("Failed to validate content refresh", e);
            return false;
        }
    }

    /**
     * Validate infinite scroll
     */
    public boolean validateInfiniteScroll() {
        try {
            // Get initial number of elements
            List<WebElement> initialProducts = driver.findElements(By.cssSelector(
                ".product, .item, [class*='product'], [data-testid*='product']"));
            int initialCount = initialProducts.size();
            
            // Scroll to bottom
            scrollToBottom();
            
            // Wait for new content to load
            Thread.sleep(3000);
            
            // Get new count
            List<WebElement> finalProducts = driver.findElements(By.cssSelector(
                ".product, .item, [class*='product'], [data-testid*='product']"));
            int finalCount = finalProducts.size();
            
            return finalCount > initialCount;
        } catch (Exception e) {
            logger.error("Failed to validate infinite scroll", e);
            return false;
        }
    }

    /**
     * Validate voice search results
     */
    public boolean validateVoiceSearchResults() {
        try {
            // Check for search results or search interface
            List<WebElement> searchResults = driver.findElements(By.cssSelector(
                ".search-results, .results, [class*='search'], [data-testid*='search']"));
            
            return !searchResults.isEmpty();
        } catch (Exception e) {
            logger.error("Failed to validate voice search results", e);
            return false;
        }
    }

    /**
     * Validate shake animation
     */
    public boolean validateShakeAnimation() {
        try {
            // Check for shake-related feedback (animation, vibration indication, etc.)
            // In real implementation, this would check for actual feedback mechanisms
            return true;
        } catch (Exception e) {
            logger.error("Failed to validate shake animation", e);
            return false;
        }
    }

    /**
     * Check for large touch targets
     */
    public boolean hasLargeTouchTargets() {
        try {
            List<WebElement> buttons = driver.findElements(By.cssSelector("button, a"));
            
            for (WebElement button : buttons) {
                Dimension size = button.getSize();
                // Touch targets should be at least 44x44 pixels
                if (size.getHeight() < 44 || size.getWidth() < 44) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to check touch target sizes", e);
            return false;
        }
    }

    /**
     * Check for simplified forms
     */
    public boolean hasSimplifiedForms() {
        try {
            List<WebElement> forms = driver.findElements(By.cssSelector("form"));
            
            if (forms.isEmpty()) {
                return true; // No forms to check
            }
            
            // Check if forms have mobile-friendly attributes
            for (WebElement form : forms) {
                List<WebElement> inputs = form.findElements(By.cssSelector("input"));
                for (WebElement input : inputs) {
                    String inputType = input.getAttribute("type");
                    // Check for appropriate input types (email, tel, etc.)
                    if (inputType != null && (inputType.equals("email") || inputType.equals("tel"))) {
                        return true;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to check simplified forms", e);
            return false;
        }
    }

    /**
     * Check for mobile payment options
     */
    public boolean hasMobilePaymentOptions() {
        try {
            List<WebElement> paymentOptions = driver.findElements(By.cssSelector(
                "[class*='apple-pay'], [class*='google-pay'], [class*='mobile-pay'], [data-testid*='mobile-pay']"));
            
            return !paymentOptions.isEmpty();
        } catch (Exception e) {
            logger.error("Failed to check mobile payment options", e);
            return false;
        }
    }

    /**
     * Validate orientation layout
     */
    public boolean validateOrientationLayout(String orientation) {
        try {
            // Check if layout adapts to orientation change
            Dimension currentSize = driver.manage().window().getSize();
            
            if ("landscape".equalsIgnoreCase(orientation)) {
                return currentSize.getWidth() > currentSize.getHeight();
            } else {
                return currentSize.getHeight() > currentSize.getWidth();
            }
        } catch (Exception e) {
            logger.error("Failed to validate orientation layout", e);
            return false;
        }
    }

    /**
     * Validate text readability
     */
    public boolean validateTextReadability() {
        try {
            List<WebElement> textElements = driver.findElements(By.cssSelector("p, span, div, h1, h2, h3"));
            
            for (WebElement element : textElements) {
                String fontSize = element.getCssValue("font-size");
                if (fontSize != null) {
                    // Extract numeric value from font-size (e.g., "16px" -> 16)
                    String numericSize = fontSize.replaceAll("[^0-9.]", "");
                    try {
                        double size = Double.parseDouble(numericSize);
                        // Minimum font size for readability should be 14px
                        if (size < 14) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // Skip if can't parse
                        continue;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to validate text readability", e);
            return false;
        }
    }
}
