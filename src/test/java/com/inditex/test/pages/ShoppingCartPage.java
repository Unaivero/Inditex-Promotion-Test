package com.inditex.test.pages;

import com.inditex.test.exceptions.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShoppingCartPage extends BasePage {

    // Cart page elements
    @FindBy(css = "[data-testid='cart-items-container'], .cart-items, .shopping-cart-items")
    private WebElement cartItemsContainer;
    
    @FindBy(css = "[data-testid='cart-total-price'], .cart-total, .total-price, .summary-total")
    private WebElement cartTotalPrice;
    
    @FindBy(css = "[data-testid='checkout-button'], .checkout-btn, .btn-checkout, button[class*='checkout']")
    private WebElement checkoutButton;
    
    @FindBy(css = "[data-testid='cart-empty-message'], .cart-empty, .empty-cart-message")
    private WebElement emptyCartMessage;
    
    @FindBy(css = "[data-testid='cart-item-count'], .cart-count, .items-count")
    private WebElement cartItemCount;
    
    @FindBy(css = "[data-testid='continue-shopping'], .continue-shopping, .back-to-shop")
    private WebElement continueShoppingButton;
    
    @FindBy(css = "[data-testid='cart-subtotal'], .cart-subtotal, .subtotal")
    private WebElement cartSubtotal;
    
    @FindBy(css = "[data-testid='shipping-cost'], .shipping-cost, .delivery-cost")
    private WebElement shippingCost;
    
    @FindBy(css = "[data-testid='tax-amount'], .tax-amount, .taxes")
    private WebElement taxAmount;
    
    @FindBy(css = "[data-testid='discount-amount'], .discount-amount, .promotion-discount")
    private WebElement discountAmount;

    // Dynamic locators for cart items
    private static final String CART_ITEM_BY_SKU = "[data-sku='%s'], [data-product-id='%s']";
    private static final String CART_ITEM_BY_NAME = "//*[contains(@class, 'cart-item') and .//*[contains(text(), '%s')]]";
    private static final String ITEM_PRICE_WITHIN_ITEM = ".//span[contains(@class, 'price') or contains(@data-testid, 'price') or contains(@class, 'amount')]";
    private static final String ITEM_QUANTITY_WITHIN_ITEM = ".//input[contains(@class, 'quantity') or @data-testid='quantity'] | .//select[contains(@class, 'quantity')] | .//*[contains(@class, 'qty')]";
    private static final String REMOVE_BUTTON_WITHIN_ITEM = ".//button[contains(@class, 'remove') or @data-testid='remove' or contains(@aria-label, 'remove')]";

    public ShoppingCartPage() {
        super();
        waitForPageLoad();
    }

    @Override
    public String getPageTitle() {
        return driver.getTitle();
    }

    @Override
    public String getPageUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public boolean isLoaded() {
        try {
            return isElementDisplayed(cartItemsContainer) || isElementDisplayed(emptyCartMessage);
        } catch (Exception e) {
            return getCurrentUrl().contains("/cart") || getCurrentUrl().contains("/shopping-cart") || 
                   getCurrentUrl().contains("/cesta") || getCurrentUrl().contains("/panier");
        }
    }

    /**
     * Get cart item element by SKU or product name
     */
    private WebElement getCartItemElement(String skuOrProductName) {
        try {
            // First try to find by SKU
            String skuSelector = String.format(CART_ITEM_BY_SKU, skuOrProductName, skuOrProductName);
            try {
                return driver.findElement(By.cssSelector(skuSelector));
            } catch (NoSuchElementException e) {
                // If SKU selector fails, try by product name
                String nameXPath = String.format(CART_ITEM_BY_NAME, skuOrProductName);
                return driver.findElement(By.xpath(nameXPath));
            }
        } catch (NoSuchElementException e) {
            logger.error("Cart item not found for SKU/name: {}", skuOrProductName);
            throw new PageObjectException("Cart item not found: " + skuOrProductName, e);
        }
    }

    /**
     * Get the price of a specific item in the cart
     */
    public String getItemPriceInCart(String skuOrProductName) {
        try {
            WebElement cartItemElement = getCartItemElement(skuOrProductName);
            WebElement priceElement = cartItemElement.findElement(By.xpath(ITEM_PRICE_WITHIN_ITEM));
            return safeGetText(priceElement, "item price for " + skuOrProductName);
        } catch (Exception e) {
            logger.error("Failed to get item price for: {}", skuOrProductName, e);
            return "0.00";
        }
    }

    /**
     * Get the total price of the cart
     */
    public String getCartTotalPrice() {
        try {
            return safeGetText(cartTotalPrice, "cart total price");
        } catch (Exception e) {
            logger.error("Failed to get cart total price", e);
            return "0.00";
        }
    }

    /**
     * Parse price string to BigDecimal
     */
    public BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Remove currency symbols and whitespace, extract numbers
        String cleanPrice = priceText.replaceAll("[^0-9.,]", "");
        
        // Handle different decimal separators
        if (cleanPrice.contains(",") && cleanPrice.contains(".")) {
            // Format like 1.234,56 (European)
            cleanPrice = cleanPrice.replace(".", "").replace(",", ".");
        } else if (cleanPrice.contains(",") && !cleanPrice.contains(".")) {
            // Format like 1234,56 (European decimal comma)
            cleanPrice = cleanPrice.replace(",", ".");
        }
        
        try {
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse price: '{}', returning 0.00", priceText);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get cart total as BigDecimal
     */
    public BigDecimal getCartTotalAsBigDecimal() {
        String totalText = getCartTotalPrice();
        return parsePrice(totalText);
    }

    /**
     * Get item price as BigDecimal
     */
    public BigDecimal getItemPriceAsBigDecimal(String skuOrProductName) {
        String priceText = getItemPriceInCart(skuOrProductName);
        return parsePrice(priceText);
    }

    /**
     * Check if an item is in the cart
     */
    public boolean isItemInCart(String skuOrProductName) {
        try {
            getCartItemElement(skuOrProductName);
            return true;
        } catch (PageObjectException e) {
            return false;
        }
    }

    /**
     * Get the quantity of a specific item in the cart
     */
    public int getItemQuantity(String skuOrProductName) {
        try {
            WebElement cartItemElement = getCartItemElement(skuOrProductName);
            WebElement quantityElement = cartItemElement.findElement(By.xpath(ITEM_QUANTITY_WITHIN_ITEM));
            
            String quantityText;
            if (quantityElement.getTagName().equals("input") || quantityElement.getTagName().equals("select")) {
                quantityText = quantityElement.getAttribute("value");
            } else {
                quantityText = quantityElement.getText();
            }
            
            return Integer.parseInt(quantityText.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            logger.error("Failed to get quantity for item: {}", skuOrProductName, e);
            return 0;
        }
    }

    /**
     * Remove an item from the cart
     */
    public void removeItemFromCart(String skuOrProductName) {
        try {
            WebElement cartItemElement = getCartItemElement(skuOrProductName);
            WebElement removeButton = cartItemElement.findElement(By.xpath(REMOVE_BUTTON_WITHIN_ITEM));
            safeClick(removeButton, "remove button for " + skuOrProductName);
            
            // Wait for item to be removed
            wait.until(ExpectedConditions.invisibilityOf(cartItemElement));
            logger.info("Successfully removed item from cart: {}", skuOrProductName);
        } catch (Exception e) {
            logger.error("Failed to remove item from cart: {}", skuOrProductName, e);
            throw new PageObjectException("Failed to remove item: " + skuOrProductName, e);
        }
    }

    /**
     * Update item quantity in cart
     */
    public void updateItemQuantity(String skuOrProductName, int newQuantity) {
        try {
            WebElement cartItemElement = getCartItemElement(skuOrProductName);
            WebElement quantityElement = cartItemElement.findElement(By.xpath(ITEM_QUANTITY_WITHIN_ITEM));
            
            if (quantityElement.getTagName().equals("input")) {
                safeSendKeys(quantityElement, String.valueOf(newQuantity), "quantity input for " + skuOrProductName);
            } else if (quantityElement.getTagName().equals("select")) {
                // Handle select dropdown for quantity
                quantityElement.findElement(By.xpath(".//option[@value='" + newQuantity + "']")).click();
            }
            
            logger.info("Updated quantity for item {} to {}", skuOrProductName, newQuantity);
        } catch (Exception e) {
            logger.error("Failed to update quantity for item: {}", skuOrProductName, e);
            throw new PageObjectException("Failed to update quantity for: " + skuOrProductName, e);
        }
    }

    /**
     * Get the number of items in cart
     */
    public int getCartItemCount() {
        try {
            if (isElementDisplayed(cartItemCount)) {
                String countText = safeGetText(cartItemCount, "cart item count");
                return Integer.parseInt(countText.replaceAll("[^0-9]", ""));
            } else {
                // Fallback: count cart item elements
                List<WebElement> items = driver.findElements(By.cssSelector("[class*='cart-item'], [data-testid*='cart-item']"));
                return items.size();
            }
        } catch (Exception e) {
            logger.error("Failed to get cart item count", e);
            return 0;
        }
    }

    /**
     * Check if cart is empty
     */
    public boolean isCartEmpty() {
        return getCartItemCount() == 0 || isElementDisplayed(emptyCartMessage);
    }

    /**
     * Get cart subtotal (before taxes and shipping)
     */
    public String getCartSubtotal() {
        try {
            if (isElementDisplayed(cartSubtotal)) {
                return safeGetText(cartSubtotal, "cart subtotal");
            }
            return "0.00";
        } catch (Exception e) {
            logger.error("Failed to get cart subtotal", e);
            return "0.00";
        }
    }

    /**
     * Get shipping cost
     */
    public String getShippingCost() {
        try {
            if (isElementDisplayed(shippingCost)) {
                return safeGetText(shippingCost, "shipping cost");
            }
            return "0.00";
        } catch (Exception e) {
            logger.error("Failed to get shipping cost", e);
            return "0.00";
        }
    }

    /**
     * Get tax amount
     */
    public String getTaxAmount() {
        try {
            if (isElementDisplayed(taxAmount)) {
                return safeGetText(taxAmount, "tax amount");
            }
            return "0.00";
        } catch (Exception e) {
            logger.error("Failed to get tax amount", e);
            return "0.00";
        }
    }

    /**
     * Get discount amount
     */
    public String getDiscountAmount() {
        try {
            if (isElementDisplayed(discountAmount)) {
                return safeGetText(discountAmount, "discount amount");
            }
            return "0.00";
        } catch (Exception e) {
            logger.error("Failed to get discount amount", e);
            return "0.00";
        }
    }

    /**
     * Proceed to checkout
     */
    public void proceedToCheckout() {
        try {
            safeClick(checkoutButton, "checkout button");
            logger.info("Proceeded to checkout");
        } catch (Exception e) {
            logger.error("Failed to proceed to checkout", e);
            throw new PageObjectException("Failed to proceed to checkout", e);
        }
    }

    /**
     * Continue shopping
     */
    public void continueShopping() {
        try {
            if (isElementDisplayed(continueShoppingButton)) {
                safeClick(continueShoppingButton, "continue shopping button");
                logger.info("Continued shopping");
            }
        } catch (Exception e) {
            logger.error("Failed to continue shopping", e);
            throw new PageObjectException("Failed to continue shopping", e);
        }
    }

    /**
     * Validate cart total calculation
     */
    public boolean validateCartTotalCalculation() {
        try {
            BigDecimal subtotal = parsePrice(getCartSubtotal());
            BigDecimal shipping = parsePrice(getShippingCost());
            BigDecimal tax = parsePrice(getTaxAmount());
            BigDecimal discount = parsePrice(getDiscountAmount());
            BigDecimal expectedTotal = subtotal.add(shipping).add(tax).subtract(discount);
            BigDecimal actualTotal = getCartTotalAsBigDecimal();
            
            // Allow for small rounding differences
            BigDecimal difference = expectedTotal.subtract(actualTotal).abs();
            boolean isValid = difference.compareTo(new BigDecimal("0.01")) <= 0;
            
            if (!isValid) {
                logger.warn("Cart total calculation mismatch. Expected: {}, Actual: {}", expectedTotal, actualTotal);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Failed to validate cart total calculation", e);
            return false;
        }
    }

    /**
     * Get all cart item names
     */
    public List<String> getAllCartItemNames() {
        try {
            List<WebElement> itemNameElements = driver.findElements(
                By.cssSelector("[class*='cart-item'] [class*='name'], [class*='cart-item'] [class*='title'], [data-testid*='item-name']"));
            
            return itemNameElements.stream()
                .map(element -> element.getText())
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get cart item names", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Clear entire cart
     */
    public void clearCart() {
        try {
            List<String> itemNames = getAllCartItemNames();
            for (String itemName : itemNames) {
                removeItemFromCart(itemName);
            }
            logger.info("Successfully cleared cart");
        } catch (Exception e) {
            logger.error("Failed to clear cart", e);
            throw new PageObjectException("Failed to clear cart", e);
        }
    }
}
