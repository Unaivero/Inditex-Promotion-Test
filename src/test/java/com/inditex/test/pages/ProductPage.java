package com.inditex.test.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class ProductPage extends BasePage {

    // ### IMPORTANT: Update these locators for the actual website ###
    @FindBy(xpath = "//h1[contains(@class, 'product-name') or contains(@class, 'ProductName') or @data-testid='product-title']") // Placeholder for product name
    private WebElement productNameText;

    @FindBy(xpath = "//span[contains(@class, 'price--original') or contains(@class, 'Price-original') or contains(@class, 'product-price--original') or @data-testid='price-original']") // Placeholder
    private WebElement originalPriceText;

    @FindBy(xpath = "//span[contains(@class, 'price--promotional') or contains(@class, 'Price-sales') or contains(@class, 'product-price--discounted') or @data-testid='price-promotional']") // Placeholder
    private WebElement promotionalPriceText;

    @FindBy(xpath = "//span[contains(@class, 'discount-percentage') or contains(@class, 'DiscountBadge') or @data-testid='discount-badge']") // Placeholder
    private WebElement discountPercentageText;
    
    @FindBy(xpath = "//button[contains(@id, 'add-to-cart') or contains(@data-testid, 'add-to-cart-button') or .//span[contains(text(),'Add to bag') or contains(text(),'Añadir a la cesta')]]") // Placeholder
    private WebElement addToCartButton;

    // This might be a general cart icon/link available on many pages, not just product page
    @FindBy(xpath = "//a[contains(@href, 'cart') or contains(@href, 'shopping-bag') or @data-testid='cart-link-header']") // Placeholder
    private WebElement viewCartButton;
    // ### END IMPORTANT ###

    // productContainerLocatorBySku is complex and usually not needed if you navigate to a specific product's page.
    // If you are on a category page with multiple products, then you'd need a strategy to find a product by SKU.

    public ProductPage() {
        super();
    }

    public String getProductName() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(productNameText)).getText();
        } catch (Exception e) {
            System.err.println("Product name element not found: " + e.getMessage());
            return "Product Name Not Found";
        }
    }

    public String getOriginalPrice() { // SKU might not be needed if on a dedicated product page
        try {
            return wait.until(ExpectedConditions.visibilityOf(originalPriceText)).getText();
        } catch (Exception e) {
            System.err.println("Original price element not found. Error: " + e.getMessage());
            return "Original Price Not Found";
        }
    }

    public String getPromotionalPrice() { // SKU might not be needed if on a dedicated product page
        try {
            return wait.until(ExpectedConditions.visibilityOf(promotionalPriceText)).getText();
        } catch (Exception e) {
            System.err.println("Promotional price element not found. Error: " + e.getMessage());
            return "Promotional Price Not Found";
        }
    }

    public String getDiscountInfo() { // SKU might not be needed if on a dedicated product page
         try {
            return wait.until(ExpectedConditions.visibilityOf(discountPercentageText)).getText();
        } catch (Exception e) {
            System.err.println("Discount info element not found. Error: " + e.getMessage());
            return "Discount Info Not Found";
        }
    }

    public void addProductToCart() { // SKU might not be needed if on a dedicated product page
        try {
            wait.until(ExpectedConditions.elementToBeClickable(addToCartButton)).click();
            System.out.println("Product added to cart.");
            // Add a small wait or check for a confirmation message if applicable
            // For example: wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("toast-confirmation")));
        } catch (Exception e) {
            System.err.println("Could not add product to cart: " + e.getMessage());
            throw new RuntimeException("Failed to add product to cart", e);
        }
    }

    public ShoppingCartPage navigateToShoppingCart() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(viewCartButton)).click();
            System.out.println("Navigated to shopping cart page.");
            return new ShoppingCartPage();
        } catch (Exception e) {
            System.err.println("Could not navigate to shopping cart: " + e.getMessage());
            throw new RuntimeException("Failed to navigate to shopping cart", e);
        }
    }

    @Override
    public String getPageTitle() {
        return driver.getTitle();
    }

    @Override
    public String getPageUrl() {
        return driver.getCurrentUrl();
    }
}
