package com.inditex.test.stepdefinitions;

import com.inditex.test.mobile.MobileCapabilities;
import com.inditex.test.pages.HomePage;
import com.inditex.test.pages.ProductPage;
import com.inditex.test.pages.ShoppingCartPage;
import com.inditex.test.utils.WebDriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class MobilePromotionalSteps {
    private static final Logger logger = LoggerFactory.getLogger(MobilePromotionalSteps.class);
    
    private WebDriver driver;
    private HomePage homePage;
    private ProductPage productPage;
    private ShoppingCartPage shoppingCartPage;
    private MobileCapabilities mobileCapabilities;
    private String currentDeviceType;
    private String currentOrientation;
    private Dimension originalWindowSize;

    @Given("I am using a mobile device {string}")
    public void iAmUsingAMobileDevice(String deviceType) {
        driver = WebDriverFactory.getDriver();
        mobileCapabilities = new MobileCapabilities();
        
        // Store original window size
        originalWindowSize = driver.manage().window().getSize();
        
        // Set mobile viewport based on device type
        setMobileViewport(deviceType);
        this.currentDeviceType = deviceType;
        
        logger.info("Set mobile device viewport for: {}", deviceType);
    }

    @Given("I am using a tablet device {string}")
    public void iAmUsingATabletDevice(String deviceType) {
        driver = WebDriverFactory.getDriver();
        mobileCapabilities = new MobileCapabilities();
        
        // Store original window size
        originalWindowSize = driver.manage().window().getSize();
        
        // Set tablet viewport
        setTabletViewport(deviceType);
        this.currentDeviceType = deviceType;
        
        logger.info("Set tablet device viewport for: {}", deviceType);
    }

    @Given("the device is in {string} orientation")
    public void theDeviceIsInOrientation(String orientation) {
        this.currentOrientation = orientation;
        
        if ("landscape".equalsIgnoreCase(orientation)) {
            setLandscapeOrientation();
        } else {
            setPortraitOrientation();
        }
        
        logger.info("Set device orientation to: {}", orientation);
    }

    @Given("I navigate to the mobile homepage for brand {string}")
    public void iNavigateToTheMobileHomepage(String brand) {
        homePage = new HomePage();
        homePage.navigateToBrandHomepage(brand, "ES", "es");
        
        // Wait for mobile page to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
            .executeScript("return document.readyState").equals("complete"));
        
        logger.info("Navigated to mobile homepage for brand: {}", brand);
    }

    @When("I tap on the hamburger menu")
    public void iTapOnTheHamburgerMenu() {
        try {
            // Mobile-specific interaction
            mobileCapabilities.tapElement("hamburger-menu", "[data-testid='hamburger-menu'], .hamburger, .menu-toggle, .mobile-menu-btn");
            logger.info("Tapped on hamburger menu");
        } catch (Exception e) {
            logger.error("Failed to tap hamburger menu", e);
            throw new RuntimeException("Could not tap hamburger menu", e);
        }
    }

    @When("I swipe {string} on the product carousel")
    public void iSwipeOnTheProductCarousel(String direction) {
        try {
            String carouselSelector = "[data-testid='product-carousel'], .product-slider, .carousel, .swiper-container";
            
            if ("left".equalsIgnoreCase(direction)) {
                mobileCapabilities.swipeLeft(carouselSelector);
            } else if ("right".equalsIgnoreCase(direction)) {
                mobileCapabilities.swipeRight(carouselSelector);
            }
            
            logger.info("Swiped {} on product carousel", direction);
        } catch (Exception e) {
            logger.error("Failed to swipe on carousel", e);
            throw new RuntimeException("Could not swipe on carousel", e);
        }
    }

    @When("I pinch to zoom on the product image")
    public void iPinchToZoomOnTheProductImage() {
        try {
            String imageSelector = "[data-testid='product-image'], .product-image, .main-image img";
            mobileCapabilities.pinchZoom(imageSelector, 2.0); // 2x zoom
            logger.info("Performed pinch to zoom on product image");
        } catch (Exception e) {
            logger.error("Failed to pinch zoom", e);
            throw new RuntimeException("Could not perform pinch zoom", e);
        }
    }

    @When("I use pull-to-refresh gesture")
    public void iUsePullToRefreshGesture() {
        try {
            mobileCapabilities.pullToRefresh();
            logger.info("Performed pull-to-refresh gesture");
        } catch (Exception e) {
            logger.error("Failed to pull-to-refresh", e);
            throw new RuntimeException("Could not perform pull-to-refresh", e);
        }
    }

    @When("I scroll down to load more products")
    public void iScrollDownToLoadMoreProducts() {
        try {
            // Perform infinite scroll
            mobileCapabilities.scrollToBottom();
            
            // Wait for new products to load
            Thread.sleep(2000);
            
            logger.info("Scrolled down to load more products");
        } catch (Exception e) {
            logger.error("Failed to scroll for more products", e);
            throw new RuntimeException("Could not scroll for more products", e);
        }
    }

    @When("I long press on a product item")
    public void iLongPressOnAProductItem() {
        try {
            String productSelector = "[data-testid='product-item']:first-of-type, .product-item:first-of-type, .product-card:first-of-type";
            mobileCapabilities.longPress(productSelector);
            logger.info("Performed long press on product item");
        } catch (Exception e) {
            logger.error("Failed to long press", e);
            throw new RuntimeException("Could not perform long press", e);
        }
    }

    @When("I use voice search for {string}")
    public void iUseVoiceSearchFor(String searchTerm) {
        try {
            // Simulate voice search (in real implementation, this would trigger actual voice search)
            mobileCapabilities.simulateVoiceSearch(searchTerm);
            logger.info("Performed voice search for: {}", searchTerm);
        } catch (Exception e) {
            logger.error("Failed to use voice search", e);
            throw new RuntimeException("Could not use voice search", e);
        }
    }

    @When("I shake the device")
    public void iShakeTheDevice() {
        try {
            mobileCapabilities.simulateDeviceShake();
            logger.info("Simulated device shake");
        } catch (Exception e) {
            logger.error("Failed to shake device", e);
            throw new RuntimeException("Could not shake device", e);
        }
    }

    @When("I test the mobile checkout flow")
    public void iTestTheMobileCheckoutFlow() {
        try {
            // Navigate through mobile checkout
            productPage = new ProductPage();
            productPage.addProductToCart();
            
            shoppingCartPage = productPage.navigateToShoppingCart();
            Assert.assertTrue(shoppingCartPage.isLoaded(), "Shopping cart should load on mobile");
            
            // Verify mobile-specific checkout elements
            Assert.assertTrue(isMobileOptimized(), "Checkout should be mobile optimized");
            
            logger.info("Completed mobile checkout flow test");
        } catch (Exception e) {
            logger.error("Mobile checkout flow failed", e);
            throw new RuntimeException("Mobile checkout flow failed", e);
        }
    }

    @Then("the mobile menu should be displayed")
    public void theMobileMenuShouldBeDisplayed() {
        try {
            boolean menuVisible = mobileCapabilities.isElementVisible(
                "[data-testid='mobile-menu'], .mobile-menu, .hamburger-menu-content");
            Assert.assertTrue(menuVisible, "Mobile menu should be visible");
            logger.info("Mobile menu is displayed correctly");
        } catch (Exception e) {
            logger.error("Mobile menu validation failed", e);
            throw new RuntimeException("Mobile menu validation failed", e);
        }
    }

    @Then("the page should be responsive on mobile")
    public void thePageShouldBeResponsiveOnMobile() {
        try {
            // Check if page adapts to mobile viewport
            boolean isResponsive = mobileCapabilities.validateResponsiveDesign();
            Assert.assertTrue(isResponsive, "Page should be responsive on mobile");
            
            // Check for mobile-specific elements
            boolean hasMobileElements = mobileCapabilities.hasMobileOptimizations();
            Assert.assertTrue(hasMobileElements, "Page should have mobile optimizations");
            
            logger.info("Page is responsive on mobile device");
        } catch (Exception e) {
            logger.error("Mobile responsiveness validation failed", e);
            throw new RuntimeException("Mobile responsiveness validation failed", e);
        }
    }

    @Then("touch interactions should work correctly")
    public void touchInteractionsShouldWorkCorrectly() {
        try {
            // Test various touch interactions
            boolean tapWorks = mobileCapabilities.testTapInteraction();
            boolean swipeWorks = mobileCapabilities.testSwipeInteraction();
            boolean pinchWorks = mobileCapabilities.testPinchInteraction();
            
            Assert.assertTrue(tapWorks, "Tap interactions should work");
            Assert.assertTrue(swipeWorks, "Swipe interactions should work");
            Assert.assertTrue(pinchWorks, "Pinch interactions should work");
            
            logger.info("All touch interactions work correctly");
        } catch (Exception e) {
            logger.error("Touch interactions validation failed", e);
            throw new RuntimeException("Touch interactions validation failed", e);
        }
    }

    @Then("the product carousel should respond to swipe gestures")
    public void theProductCarouselShouldRespondToSwipeGestures() {
        try {
            boolean carouselResponsive = mobileCapabilities.validateCarouselSwipe();
            Assert.assertTrue(carouselResponsive, "Product carousel should respond to swipe gestures");
            logger.info("Product carousel responds correctly to swipe gestures");
        } catch (Exception e) {
            logger.error("Carousel swipe validation failed", e);
            throw new RuntimeException("Carousel swipe validation failed", e);
        }
    }

    @Then("the product image should support zoom gestures")
    public void theProductImageShouldSupportZoomGestures() {
        try {
            boolean zoomSupported = mobileCapabilities.validateImageZoom();
            Assert.assertTrue(zoomSupported, "Product image should support zoom gestures");
            logger.info("Product image supports zoom gestures correctly");
        } catch (Exception e) {
            logger.error("Image zoom validation failed", e);
            throw new RuntimeException("Image zoom validation failed", e);
        }
    }

    @Then("the page should reload with new content")
    public void thePageShouldReloadWithNewContent() {
        try {
            // Wait for page refresh and verify new content
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
            
            boolean contentRefreshed = mobileCapabilities.validateContentRefresh();
            Assert.assertTrue(contentRefreshed, "Page should reload with new content after pull-to-refresh");
            
            logger.info("Page reloaded with new content successfully");
        } catch (Exception e) {
            logger.error("Content refresh validation failed", e);
            throw new RuntimeException("Content refresh validation failed", e);
        }
    }

    @Then("more products should be loaded")
    public void moreProductsShouldBeLoaded() {
        try {
            boolean moreProductsLoaded = mobileCapabilities.validateInfiniteScroll();
            Assert.assertTrue(moreProductsLoaded, "More products should be loaded after scrolling");
            logger.info("More products loaded successfully through infinite scroll");
        } catch (Exception e) {
            logger.error("Infinite scroll validation failed", e);
            throw new RuntimeException("Infinite scroll validation failed", e);
        }
    }

    @Then("a context menu should appear")
    public void aContextMenuShouldAppear() {
        try {
            boolean contextMenuVisible = mobileCapabilities.isElementVisible(
                "[data-testid='context-menu'], .context-menu, .long-press-menu");
            Assert.assertTrue(contextMenuVisible, "Context menu should appear after long press");
            logger.info("Context menu appeared correctly after long press");
        } catch (Exception e) {
            logger.error("Context menu validation failed", e);
            throw new RuntimeException("Context menu validation failed", e);
        }
    }

    @Then("voice search results should be displayed")
    public void voiceSearchResultsShouldBeDisplayed() {
        try {
            boolean voiceResultsVisible = mobileCapabilities.validateVoiceSearchResults();
            Assert.assertTrue(voiceResultsVisible, "Voice search results should be displayed");
            logger.info("Voice search results displayed correctly");
        } catch (Exception e) {
            logger.error("Voice search validation failed", e);
            throw new RuntimeException("Voice search validation failed", e);
        }
    }

    @Then("a feedback animation should be triggered")
    public void aFeedbackAnimationShouldBeTriggered() {
        try {
            boolean animationTriggered = mobileCapabilities.validateShakeAnimation();
            Assert.assertTrue(animationTriggered, "Feedback animation should be triggered by device shake");
            logger.info("Feedback animation triggered correctly by device shake");
        } catch (Exception e) {
            logger.error("Shake animation validation failed", e);
            throw new RuntimeException("Shake animation validation failed", e);
        }
    }

    @Then("the checkout process should be mobile-optimized")
    public void theCheckoutProcessShouldBeMobileOptimized() {
        try {
            boolean mobileOptimized = isMobileOptimized();
            Assert.assertTrue(mobileOptimized, "Checkout process should be mobile-optimized");
            
            // Additional mobile checkout validations
            boolean hasLargeButtons = mobileCapabilities.hasLargeTouchTargets();
            boolean hasSimplifiedForms = mobileCapabilities.hasSimplifiedForms();
            boolean hasMobilePayment = mobileCapabilities.hasMobilePaymentOptions();
            
            Assert.assertTrue(hasLargeButtons, "Should have large touch targets for mobile");
            Assert.assertTrue(hasSimplifiedForms, "Should have simplified forms for mobile");
            Assert.assertTrue(hasMobilePayment, "Should have mobile payment options");
            
            logger.info("Checkout process is properly mobile-optimized");
        } catch (Exception e) {
            logger.error("Mobile checkout optimization validation failed", e);
            throw new RuntimeException("Mobile checkout optimization validation failed", e);
        }
    }

    @Then("the layout should adapt to {string} orientation")
    public void theLayoutShouldAdaptToOrientation(String orientation) {
        try {
            boolean layoutAdapted = mobileCapabilities.validateOrientationLayout(orientation);
            Assert.assertTrue(layoutAdapted, "Layout should adapt to " + orientation + " orientation");
            logger.info("Layout adapted correctly to {} orientation", orientation);
        } catch (Exception e) {
            logger.error("Orientation layout validation failed", e);
            throw new RuntimeException("Orientation layout validation failed", e);
        }
    }

    @Then("text should remain readable on mobile")
    public void textShouldRemainReadableOnMobile() {
        try {
            boolean textReadable = mobileCapabilities.validateTextReadability();
            Assert.assertTrue(textReadable, "Text should remain readable on mobile");
            logger.info("Text is readable on mobile device");
        } catch (Exception e) {
            logger.error("Text readability validation failed", e);
            throw new RuntimeException("Text readability validation failed", e);
        }
    }

    // Helper methods
    
    private void setMobileViewport(String deviceType) {
        Dimension mobileSize;
        
        switch (deviceType.toLowerCase()) {
            case "iphone":
            case "iphone 12":
                mobileSize = new Dimension(390, 844);
                break;
            case "samsung galaxy":
                mobileSize = new Dimension(360, 800);
                break;
            case "pixel":
                mobileSize = new Dimension(393, 851);
                break;
            default:
                mobileSize = new Dimension(375, 667); // Default mobile size
        }
        
        driver.manage().window().setSize(mobileSize);
    }
    
    private void setTabletViewport(String deviceType) {
        Dimension tabletSize;
        
        switch (deviceType.toLowerCase()) {
            case "ipad":
                tabletSize = new Dimension(768, 1024);
                break;
            case "android tablet":
                tabletSize = new Dimension(800, 1280);
                break;
            default:
                tabletSize = new Dimension(768, 1024); // Default tablet size
        }
        
        driver.manage().window().setSize(tabletSize);
    }
    
    private void setLandscapeOrientation() {
        Dimension currentSize = driver.manage().window().getSize();
        if (currentSize.height > currentSize.width) {
            // Swap dimensions for landscape
            driver.manage().window().setSize(new Dimension(currentSize.height, currentSize.width));
        }
    }
    
    private void setPortraitOrientation() {
        Dimension currentSize = driver.manage().window().getSize();
        if (currentSize.width > currentSize.height) {
            // Swap dimensions for portrait
            driver.manage().window().setSize(new Dimension(currentSize.height, currentSize.width));
        }
    }
    
    private boolean isMobileOptimized() {
        try {
            // Check for mobile-specific optimizations
            return mobileCapabilities.hasLargeTouchTargets() &&
                   mobileCapabilities.hasSimplifiedForms() &&
                   mobileCapabilities.validateResponsiveDesign();
        } catch (Exception e) {
            logger.error("Error checking mobile optimization", e);
            return false;
        }
    }
    
    // Cleanup method to restore original window size
    public void restoreOriginalViewport() {
        if (originalWindowSize != null && driver != null) {
            try {
                driver.manage().window().setSize(originalWindowSize);
                logger.info("Restored original viewport size");
            } catch (Exception e) {
                logger.warn("Failed to restore original viewport size", e);
            }
        }
    }
}
