package com.inditex.test.stepdefinitions;

import com.inditex.test.pages.HomePage;
import com.inditex.test.pages.ProductPage;
import com.inditex.test.pages.ShoppingCartPage;
import com.inditex.test.utils.CsvDataReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class PromotionalPricingSteps {

    private HomePage homePage;
    private ProductPage productPage;
    private ShoppingCartPage shoppingCartPage;
    private String currentBrand;
    private String currentCountry;
    private String currentLanguage;
    private String currentCustomerType;
    private String currentSku;

    // Helper method to get data for current SKU
    private Map<String, String> getCurrentProductData() throws IOException {
        Optional<Map<String, String>> productDataOpt = CsvDataReader.getTestData("promotions_data.csv")
                .stream()
                .filter(row -> row.get("sku").equals(currentSku) && 
                               row.get("brand").equalsIgnoreCase(currentBrand) &&
                               row.get("country").equalsIgnoreCase(currentCountry))
                .findFirst();
        return productDataOpt.orElseThrow(() -> new AssertionError("Test data not found for SKU: " + currentSku + " in promotions_data.csv"));
    }

    @Given("I am on the homepage for brand {string} and country {string} in {string} language")
    public void i_am_on_the_homepage_for_brand_and_country_in_language(String brand, String country, String language) {
        this.currentBrand = brand;
        this.currentCountry = country;
        this.currentLanguage = language;
        homePage = new HomePage();
        homePage.navigateToBrandHomepage(brand, country, language);
        System.out.println("Navigated to homepage for: " + brand + "-" + country + "-" + language);
    }

    @Given("I navigate to a product with an active promotion")
    public void i_navigate_to_a_product_with_an_active_promotion() {
        // This step is more conceptual for the feature file.
        // The actual navigation or product selection will be driven by SKU in subsequent examples.
        // For now, we assume the context is set for a promotional product.
        // Actual navigation to a specific product page might be done here or based on SKU from examples.
        // For example, if a direct product URL part is in CSV:
        // Map<String, String> productData = getCurrentProductData(); // Assuming currentSku is set by an earlier step in examples
        // homePage.navigateToProductDirectly(productData.get("product_url_path"));
        System.out.println("Context: On a product page with an active promotion (actual navigation might differ).");
        if (productPage == null) {
            productPage = new ProductPage(); // Initialize if not already (e.g. direct navigation)
        }
    }

    @Given("I am a {string} customer")
    public void i_am_a_customer(String customerType) {
        this.currentCustomerType = customerType;
        // In a real application, this might involve actions like:
        // - Clicking a login button
        // - Entering credentials (from a secure source)
        // - Setting a cookie to simulate logged-in state for certain customer types
        // For now, this step primarily sets context for data retrieval or conditional logic.
        System.out.println("Customer type context set to: " + customerType);
        // Example: if (customerType.equalsIgnoreCase("member")) { loginPage.loginAsMember(); }
    }

    @When("I view the product details page")
    public void i_view_the_product_details_page() {
        // This step implies that the test is already on the product page.
        // If navigation to a specific product (e.g. by SKU) is required from the Examples table,
        // it should ideally happen in a @Given step or be part of "I navigate to a product..."
        // For instance, if 'currentSku' is set from an Example row:
        // homePage.navigateToProductDirectly("products/" + currentSku + "-details.html"); // Fictional URL structure
        // productPage = new ProductPage();
        Assert.assertNotNull(productPage, "ProductPage object is not initialized. Ensure navigation to product page happened.");
        System.out.println("Verifying details on product page for SKU: " + currentSku);
        // Potentially wait for key elements of the product page to be visible here
    }

    @Then("the promotional price should be displayed correctly for product SKU {string}")
    public void the_promotional_price_should_be_displayed_correctly_for_product_sku(String sku) throws IOException {
        this.currentSku = sku; // Set current SKU from the Gherkin step
        if (productPage == null) productPage = new ProductPage(); // Ensure page is initialized

        Map<String, String> productData = getCurrentProductData();
        String expectedPromotionalPrice = productData.get("promotional_price_expected");
        String actualPromotionalPrice = productPage.getPromotionalPrice();

        Assert.assertTrue(actualPromotionalPrice.contains(expectedPromotionalPrice),
            "FAIL: Promotional price for SKU " + sku + " was '" + actualPromotionalPrice + "'. Expected to contain: '" + expectedPromotionalPrice + "'");
        System.out.println("PASS: Promotional price for SKU " + sku + " is '" + actualPromotionalPrice + "' as expected.");
    }

    @Then("the original price should also be visible")
    public void the_original_price_should_also_be_visible() throws IOException {
        Map<String, String> productData = getCurrentProductData();
        String expectedOriginalPrice = productData.get("original_price");
        String actualOriginalPrice = productPage.getOriginalPrice();

        Assert.assertTrue(actualOriginalPrice.contains(expectedOriginalPrice),
            "FAIL: Original price for SKU " + currentSku + " was '" + actualOriginalPrice + "'. Expected to contain: '" + expectedOriginalPrice + "'");
        System.out.println("PASS: Original price for SKU " + currentSku + " is '" + actualOriginalPrice + "' as expected.");
    }

    @Then("the discount percentage or amount should be accurate")
    public void the_discount_percentage_or_amount_should_be_accurate() throws IOException {
        Map<String, String> productData = getCurrentProductData();
        // This assertion is conceptual. The actual discount display (text, badge) varies greatly.
        // You might need to check for discount_value, promotion_name, or a calculated percentage.
        String expectedDiscountText = productData.get("discount_value"); // Example: "20" for 20% or "10" for 10EUR
        // Or, if it's a text like "20% OFF"
        // String expectedDiscountText = productData.get("discount_value") + productData.get("discount_type_symbol_or_text");
        String actualDiscountInfo = productPage.getDiscountInfo();

        Assert.assertTrue(actualDiscountInfo.contains(expectedDiscountText), // This is a simplified check
            "FAIL: Discount info for SKU " + currentSku + " was '" + actualDiscountInfo + "'. Expected to contain: '" + expectedDiscountText + "' (from promotion_name or discount_value in CSV).");
        System.out.println("PASS: Discount info for SKU " + currentSku + " is '" + actualDiscountInfo + "' and contains expected text.");
    }

    @Given("I add product SKU {string} with an active promotion to the shopping cart")
    public void i_add_product_sku_with_an_active_promotion_to_the_shopping_cart(String sku) {
        this.currentSku = sku;
        if (productPage == null) {
            // This implies we might not be on a product page yet, or need to navigate.
            // For robust tests, ensure navigation to the specific product page for this SKU first.
            // Example: homePage.navigateToProductDirectly("path/to/product/" + sku);
            productPage = new ProductPage();
            System.out.println("ProductPage initialized in 'add product to cart' step. Ensure prior navigation if needed.");
        }
        productPage.addProductToCart(); // Assumes addProductToCart is for the currently viewed product
        System.out.println("Added product SKU " + sku + " (currently viewed product) to cart.");
    }

    @When("I proceed to the shopping cart page")
    public void i_proceed_to_the_shopping_cart_page() {
        if (productPage != null) {
            shoppingCartPage = productPage.navigateToShoppingCart();
        } else if (homePage != null) {
            // shoppingCartPage = homePage.navigateToCart(); // If HomePage has a direct cart link method
            System.out.println("ProductPage was null, attempting navigation to cart from general context (e.g. header link). This needs a method in BasePage or HomePage.");
            // For now, directly instantiate, assuming navigation happens via a common link not tied to productPage
            // This part needs a robust way to click a general cart icon/link, perhaps from BasePage
            shoppingCartPage = new ShoppingCartPage(); 
            // driver.findElement(By.xpath("//a[@data-testid='cart-icon']")).click(); // Example direct click
        } else {
            throw new IllegalStateException("Cannot navigate to cart, no page context (HomePage or ProductPage).");
        }
        Assert.assertNotNull(shoppingCartPage, "Failed to navigate or initialize ShoppingCartPage.");
        System.out.println("Proceeded to shopping cart page.");
    }

    @Then("the item price in the cart should reflect the promotional discount for SKU {string}")
    public void the_item_price_in_the_cart_should_reflect_the_promotional_discount_for_sku(String sku) throws IOException {
        this.currentSku = sku;
        Map<String, String> productData = getCurrentProductData();
        String expectedCartPrice = productData.get("promotional_price_expected");
        String productNameForLookup = productData.get("product_name"); // Use product name from CSV for lookup in cart if SKU not directly available

        String actualCartItemPrice = shoppingCartPage.getItemPriceInCart(sku); // Or use productNameForLookup
        
        Assert.assertTrue(actualCartItemPrice.contains(expectedCartPrice),
            "FAIL: Item price in cart for SKU " + sku + " was '" + actualCartItemPrice + "'. Expected to contain: '" + expectedCartPrice + "'");
        System.out.println("PASS: Item price in cart for SKU " + sku + " is '" + actualCartItemPrice + "' as expected.");
    }

    @Then("the cart total should be calculated correctly including the discount")
    public void the_cart_total_should_be_calculated_correctly_including_the_discount() throws IOException {
        // This is a simplified assertion assuming one item in the cart or that the total matches the single item's promo price.
        // For multiple items, this logic would need to be much more complex, summing expected prices.
        Map<String, String> productData = getCurrentProductData(); 
        String expectedTotalText = productData.get("promotional_price_expected"); 

        String actualCartTotal = shoppingCartPage.getCartTotalPrice();
        Assert.assertTrue(actualCartTotal.contains(expectedTotalText),
            "FAIL: Cart total was '" + actualCartTotal + "'. Expected to contain: '" + expectedTotalText + "' (based on single item SKU: " + currentSku + ")");
        System.out.println("PASS: Cart total '" + actualCartTotal + "' contains expected text based on current SKU.");
    }
}
