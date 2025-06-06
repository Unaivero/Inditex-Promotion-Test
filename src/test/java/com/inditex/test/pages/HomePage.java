package com.inditex.test.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {

    // Example locators - these would need to be specific to the actual website
    // ### IMPORTANT: Update these locators for the actual website ###
    @FindBy(id = "search-input-id") // Placeholder: e.g., for a search input field
    private WebElement searchInput;

    @FindBy(xpath = "//button[@id='onetrust-accept-btn-handler' or contains(text(),'Accept Cookies') or contains(text(),'Aceptar cookies')]") // Placeholder: Common cookie banner button
    private WebElement acceptCookiesButton;
    
    @FindBy(xpath = "//a[@data-testid='logo-link']") // Placeholder: Link to the main logo, good for ensuring page load
    private WebElement logoElement;
    // ### END IMPORTANT ###

    private String baseUrl;

    public HomePage() {
        super();
    }

    public void navigateToBrandHomepage(String brand, String countryCode, String languageCode) {
        // This logic would ideally come from a properties file or a more robust configuration manager
        switch (brand.toLowerCase()) {
            case "zara":
                baseUrl = String.format("https://www.zara.com/%s/%s/", countryCode.toLowerCase(), languageCode.toLowerCase());
                break;
            case "bershka":
                baseUrl = String.format("https://www.bershka.com/%s/%s/", countryCode.toLowerCase(), languageCode.toLowerCase());
                break;
            // Add other Inditex brands as needed (Pull&Bear, Massimo Dutti, etc.)
            default:
                throw new IllegalArgumentException("Brand URL not configured: " + brand);
        }
        System.out.println("Navigating to brand homepage: " + baseUrl);
        driver.get(baseUrl);
        handleCookieBanner();
        wait.until(ExpectedConditions.visibilityOf(logoElement)); // Wait for a common element like logo
    }

    private void handleCookieBanner() {
        try {
            // Wait for the cookie button to be potentially visible and clickable
            WebElement cookieButton = wait.until(ExpectedConditions.elementToBeClickable(acceptCookiesButton));
            cookieButton.click();
            System.out.println("Cookie banner handled.");
        } catch (Exception e) {
            // If the cookie banner is not found or not clickable, log it and continue.
            // This might be because it was already accepted, or its structure changed.
            System.out.println("Cookie banner not found or could not be interacted with: " + e.getMessage());
        }
    }

    public ProductPage searchForProduct(String productName) {
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.clear();
        searchInput.sendKeys(productName);
        // Assuming search might be auto-submit or require a specific search button click
        // searchInput.submit(); // Or click a search button if separate
        System.out.println("Searched for product: " + productName);
        // After search, it's assumed the page navigates or updates to show results,
        // leading to a ProductPage or a SearchResultsPage.
        // For this example, we'll assume it directly leads to a state where ProductPage can be used.
        return new ProductPage(); 
    }

    public void navigateToProductDirectly(String productUrlPath) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalStateException("Base URL is not set. Call navigateToBrandHomepage first.");
        }
        // productUrlPath should be the relative path from the base URL, e.g., "products/dress-p12345.html"
        String fullProductUrl = baseUrl.endsWith("/") ? baseUrl + productUrlPath : baseUrl + "/" + productUrlPath;
        System.out.println("Navigating directly to product: " + fullProductUrl);
        driver.get(fullProductUrl);
        handleCookieBanner(); // Handle cookies again if navigation re-triggers it
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
