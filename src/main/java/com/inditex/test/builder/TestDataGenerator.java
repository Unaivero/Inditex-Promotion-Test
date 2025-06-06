package com.inditex.test.builder;

import com.inditex.test.model.PromotionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for generating comprehensive test data for promotional campaigns.
 * Generates realistic test scenarios covering multiple brands, countries, customer types,
 * and promotional strategies including seasonal campaigns.
 */
public class TestDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);
    
    // Brand configuration
    private static final String[] BRANDS = {"Zara", "Bershka", "Pull&Bear", "Massimo Dutti", "Stradivarius", "Oysho", "Zara Home", "Lefties"};
    
    // Geographic configuration
    private static final String[] COUNTRIES = {"ES", "FR", "DE", "GB", "IT", "US", "PT", "NL", "BE", "AT", "CH", "IE", "SE", "DK", "NO", "FI", "PL", "CZ", "RO", "HR", "JP", "CN", "AU", "CA", "MX", "BR", "AR", "CL", "CO", "PE"};
    private static final Map<String, String> COUNTRY_LANGUAGES = Map.of(
        "ES", "es", "FR", "fr", "DE", "de", "GB", "en", "IT", "it", 
        "US", "en", "PT", "pt", "NL", "nl", "BE", "fr", "AT", "de",
        "CH", "de", "IE", "en", "SE", "sv", "DK", "da", "NO", "no",
        "FI", "fi", "PL", "pl", "CZ", "cs", "RO", "ro", "HR", "hr",
        "JP", "ja", "CN", "zh", "AU", "en", "CA", "en", "MX", "es",
        "BR", "pt", "AR", "es", "CL", "es", "CO", "es", "PE", "es"
    );
    
    // Customer types and segments
    private static final String[] CUSTOMER_TYPES = {"guest", "member", "vip", "premium", "student", "employee", "corporate"};
    
    // Product categories and pricing
    private static final Map<String, List<ProductTemplate>> BRAND_PRODUCTS = Map.of(
        "Zara", Arrays.asList(
            new ProductTemplate("Dress", 40.0, 120.0, "clothing"),
            new ProductTemplate("Shirt", 25.0, 80.0, "clothing"),
            new ProductTemplate("Jacket", 60.0, 200.0, "outerwear"),
            new ProductTemplate("Trousers", 30.0, 90.0, "clothing"),
            new ProductTemplate("Shoes", 50.0, 150.0, "footwear"),
            new ProductTemplate("Bag", 30.0, 100.0, "accessories"),
            new ProductTemplate("Scarf", 15.0, 50.0, "accessories")
        ),
        "Bershka", Arrays.asList(
            new ProductTemplate("Jeans", 20.0, 60.0, "clothing"),
            new ProductTemplate("T-Shirt", 10.0, 30.0, "clothing"),
            new ProductTemplate("Hoodie", 25.0, 70.0, "clothing"),
            new ProductTemplate("Sneakers", 30.0, 80.0, "footwear"),
            new ProductTemplate("Cap", 10.0, 25.0, "accessories")
        ),
        "Pull&Bear", Arrays.asList(
            new ProductTemplate("Sweatshirt", 20.0, 50.0, "clothing"),
            new ProductTemplate("Cargo Pants", 25.0, 65.0, "clothing"),
            new ProductTemplate("Basic Tee", 8.0, 20.0, "clothing"),
            new ProductTemplate("Denim Jacket", 35.0, 80.0, "outerwear")
        ),
        "Massimo Dutti", Arrays.asList(
            new ProductTemplate("Blazer", 80.0, 250.0, "formal"),
            new ProductTemplate("Silk Blouse", 60.0, 150.0, "formal"),
            new ProductTemplate("Leather Shoes", 100.0, 300.0, "footwear"),
            new ProductTemplate("Cashmere Sweater", 120.0, 400.0, "luxury")
        ),
        "Stradivarius", Arrays.asList(
            new ProductTemplate("Mini Skirt", 15.0, 40.0, "clothing"),
            new ProductTemplate("Crop Top", 12.0, 35.0, "clothing"),
            new ProductTemplate("Platform Shoes", 25.0, 70.0, "footwear")
        ),
        "Oysho", Arrays.asList(
            new ProductTemplate("Sports Bra", 20.0, 50.0, "activewear"),
            new ProductTemplate("Yoga Leggings", 25.0, 60.0, "activewear"),
            new ProductTemplate("Swimsuit", 30.0, 80.0, "swimwear"),
            new ProductTemplate("Pajamas", 25.0, 70.0, "loungewear")
        ),
        "Zara Home", Arrays.asList(
            new ProductTemplate("Cushion", 15.0, 45.0, "home"),
            new ProductTemplate("Candle", 10.0, 30.0, "home"),
            new ProductTemplate("Throw Blanket", 30.0, 90.0, "home")
        ),
        "Lefties", Arrays.asList(
            new ProductTemplate("Basic Jeans", 12.0, 25.0, "clothing"),
            new ProductTemplate("Cotton T-Shirt", 5.0, 15.0, "clothing"),
            new ProductTemplate("Canvas Shoes", 15.0, 35.0, "footwear")
        )
    );
    
    // Discount types and strategies
    private static final String[] DISCOUNT_TYPES = {"PERCENTAGE", "FIXED_AMOUNT", "MULTI_BUY", "BOGO", "FREE_SHIPPING", "BUNDLE"};
    
    // Seasonal campaigns
    private static final List<SeasonalCampaign> SEASONAL_CAMPAIGNS = Arrays.asList(
        new SeasonalCampaign("Spring Sale", LocalDate.of(2024, 3, 15), LocalDate.of(2024, 4, 15), 10, 30),
        new SeasonalCampaign("Summer Clearance", LocalDate.of(2024, 6, 15), LocalDate.of(2024, 8, 15), 20, 50),
        new SeasonalCampaign("Back to School", LocalDate.of(2024, 8, 15), LocalDate.of(2024, 9, 15), 15, 25),
        new SeasonalCampaign("Halloween Special", LocalDate.of(2024, 10, 15), LocalDate.of(2024, 11, 1), 20, 40),
        new SeasonalCampaign("Black Friday", LocalDate.of(2024, 11, 25), LocalDate.of(2024, 11, 29), 30, 70),
        new SeasonalCampaign("Cyber Monday", LocalDate.of(2024, 12, 2), LocalDate.of(2024, 12, 2), 25, 60),
        new SeasonalCampaign("Christmas Sale", LocalDate.of(2024, 12, 10), LocalDate.of(2024, 12, 24), 20, 45),
        new SeasonalCampaign("New Year Clearance", LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 31), 30, 60),
        new SeasonalCampaign("Valentine's Day", LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 16), 15, 35),
        new SeasonalCampaign("End of Season", LocalDate.of(2025, 2, 20), LocalDate.of(2025, 3, 15), 40, 70)
    );
    
    /**
     * Generates comprehensive test data including regular promotions and seasonal campaigns
     */
    public static List<PromotionTestData> generateComprehensiveTestData(int totalRecords) {
        logger.info("Generating {} comprehensive test data records", totalRecords);
        
        List<PromotionTestData> testData = new ArrayList<>();
        Random random = new Random(12345); // Fixed seed for reproducible results
        
        // Generate regular promotions (70% of data)
        int regularPromotions = (int) (totalRecords * 0.7);
        testData.addAll(generateRegularPromotions(regularPromotions, random));
        
        // Generate seasonal campaigns (30% of data)
        int seasonalPromotions = totalRecords - regularPromotions;
        testData.addAll(generateSeasonalPromotions(seasonalPromotions, random));
        
        // Shuffle to mix regular and seasonal data
        Collections.shuffle(testData, random);
        
        logger.info("Generated {} total test records ({} regular, {} seasonal)", 
                   testData.size(), regularPromotions, seasonalPromotions);
        
        return testData;
    }
    
    /**
     * Generates regular promotional test data
     */
    private static List<PromotionTestData> generateRegularPromotions(int count, Random random) {
        List<PromotionTestData> promotions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String brand = getRandomElement(BRANDS, random);
            String country = getRandomElement(COUNTRIES, random);
            String language = COUNTRY_LANGUAGES.get(country);
            String customerType = getRandomElement(CUSTOMER_TYPES, random);
            
            ProductTemplate product = getRandomProduct(brand, random);
            BigDecimal originalPrice = generatePrice(product.minPrice, product.maxPrice, random);
            
            String discountType = getRandomElement(DISCOUNT_TYPES, random);
            PromotionDetails promotion = generatePromotion(discountType, originalPrice, customerType, random);
            
            String sku = generateSKU(brand, country, i);
            String productName = generateProductName(product, random);
            
            promotions.add(new PromotionTestData(
                brand, country, language, customerType, sku, productName,
                promotion.name, promotion.discountType, promotion.discountValue,
                originalPrice, promotion.expectedPrice, "regular", null, null
            ));
        }
        
        return promotions;
    }
    
    /**
     * Generates seasonal campaign test data
     */
    private static List<PromotionTestData> generateSeasonalPromotions(int count, Random random) {
        List<PromotionTestData> promotions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            SeasonalCampaign campaign = getRandomElement(SEASONAL_CAMPAIGNS, random);
            String brand = getRandomElement(BRANDS, random);
            String country = getRandomElement(COUNTRIES, random);
            String language = COUNTRY_LANGUAGES.get(country);
            String customerType = getRandomElement(CUSTOMER_TYPES, random);
            
            ProductTemplate product = getRandomProduct(brand, random);
            BigDecimal originalPrice = generatePrice(product.minPrice, product.maxPrice, random);
            
            // Seasonal promotions tend to have higher discounts
            int discountPercentage = ThreadLocalRandom.current().nextInt(campaign.minDiscount, campaign.maxDiscount + 1);
            BigDecimal discountValue = BigDecimal.valueOf(discountPercentage);
            BigDecimal expectedPrice = originalPrice.multiply(BigDecimal.valueOf(100 - discountPercentage))
                                                   .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            String sku = generateSKU(brand, country, i + 10000);
            String productName = generateProductName(product, random);
            String promotionName = campaign.name + " " + discountPercentage + "% Off";
            
            promotions.add(new PromotionTestData(
                brand, country, language, customerType, sku, productName,
                promotionName, "PERCENTAGE", discountValue,
                originalPrice, expectedPrice, "seasonal",
                campaign.startDate, campaign.endDate
            ));
        }
        
        return promotions;
    }
    
    /**
     * Generates bulk discount scenarios (buy 2 get 1 free, etc.)
     */
    public static List<PromotionTestData> generateBulkDiscountScenarios(int count) {
        List<PromotionTestData> bulkPromotions = new ArrayList<>();
        Random random = new Random(67890);
        
        String[] bulkTypes = {"2_FOR_1", "3_FOR_2", "BUY_2_GET_50_OFF", "BUY_3_GET_FREE_SHIPPING"};
        
        for (int i = 0; i < count; i++) {
            String brand = getRandomElement(BRANDS, random);
            String country = getRandomElement(COUNTRIES, random);
            String language = COUNTRY_LANGUAGES.get(country);
            String customerType = getRandomElement(CUSTOMER_TYPES, random);
            
            ProductTemplate product = getRandomProduct(brand, random);
            BigDecimal originalPrice = generatePrice(product.minPrice, product.maxPrice, random);
            
            String bulkType = getRandomElement(bulkTypes, random);
            PromotionDetails promotion = generateBulkPromotion(bulkType, originalPrice, random);
            
            String sku = generateSKU(brand, country, i + 20000);
            String productName = generateProductName(product, random);
            
            bulkPromotions.add(new PromotionTestData(
                brand, country, language, customerType, sku, productName,
                promotion.name, promotion.discountType, promotion.discountValue,
                originalPrice, promotion.expectedPrice, "bulk", null, null
            ));
        }
        
        return bulkPromotions;
    }
    
    /**
     * Writes test data to CSV file
     */
    public static void writeToCSV(List<PromotionTestData> testData, String filePath) throws IOException {
        logger.info("Writing {} test data records to {}", testData.size(), filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("brand,country,language,customer_type,sku,product_name,promotion_name,discount_type,discount_value,original_price,promotional_price_expected,campaign_type,start_date,end_date\n");
            
            // Write data
            for (PromotionTestData data : testData) {
                writer.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%s,%s,%s\n",
                    data.brand, data.country, data.language, data.customerType,
                    data.sku, escapeCsvValue(data.productName), escapeCsvValue(data.promotionName),
                    data.discountType, data.discountValue, data.originalPrice, data.expectedPrice,
                    data.campaignType,
                    data.startDate != null ? data.startDate.toString() : "",
                    data.endDate != null ? data.endDate.toString() : ""
                ));
            }
        }
        
        logger.info("Successfully wrote test data to {}", filePath);
    }
    
    // Helper methods
    private static <T> T getRandomElement(T[] array, Random random) {
        return array[random.nextInt(array.length)];
    }
    
    private static <T> T getRandomElement(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }
    
    private static ProductTemplate getRandomProduct(String brand, Random random) {
        List<ProductTemplate> products = BRAND_PRODUCTS.getOrDefault(brand, BRAND_PRODUCTS.get("Zara"));
        return getRandomElement(products, random);
    }
    
    private static BigDecimal generatePrice(double min, double max, Random random) {
        double price = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }
    
    private static String generateSKU(String brand, String country, int index) {
        String brandCode = brand.substring(0, Math.min(3, brand.length())).toUpperCase();
        return String.format("%s%03d%s", brandCode, index % 1000, country);
    }
    
    private static String generateProductName(ProductTemplate template, Random random) {
        String[] adjectives = {"Basic", "Premium", "Casual", "Elegant", "Trendy", "Classic", "Modern", "Vintage"};
        String[] colors = {"Black", "White", "Navy", "Beige", "Red", "Blue", "Green", "Pink", "Grey", "Brown"};
        
        String adjective = getRandomElement(adjectives, random);
        String color = getRandomElement(colors, random);
        
        return String.format("%s %s %s", adjective, color, template.name);
    }
    
    private static PromotionDetails generatePromotion(String discountType, BigDecimal originalPrice, String customerType, Random random) {
        switch (discountType) {
            case "PERCENTAGE":
                int percentage = getDiscountPercentage(customerType, random);
                BigDecimal percentagePrice = originalPrice.multiply(BigDecimal.valueOf(100 - percentage))
                                                         .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                return new PromotionDetails(
                    percentage + "% Off",
                    "PERCENTAGE",
                    BigDecimal.valueOf(percentage),
                    percentagePrice
                );
                
            case "FIXED_AMOUNT":
                int fixedAmount = ThreadLocalRandom.current().nextInt(5, Math.min(50, originalPrice.intValue()));
                BigDecimal fixedPrice = originalPrice.subtract(BigDecimal.valueOf(fixedAmount));
                return new PromotionDetails(
                    fixedAmount + " EUR Off",
                    "FIXED_AMOUNT",
                    BigDecimal.valueOf(fixedAmount),
                    fixedPrice.max(BigDecimal.ZERO)
                );
                
            default:
                return generatePromotion("PERCENTAGE", originalPrice, customerType, random);
        }
    }
    
    private static PromotionDetails generateBulkPromotion(String bulkType, BigDecimal originalPrice, Random random) {
        switch (bulkType) {
            case "2_FOR_1":
                return new PromotionDetails("Buy 2 Get 1 Free", "MULTI_BUY", BigDecimal.valueOf(50), 
                                          originalPrice.multiply(BigDecimal.valueOf(0.67)).setScale(2, RoundingMode.HALF_UP));
            case "3_FOR_2":
                return new PromotionDetails("Buy 3 Pay 2", "MULTI_BUY", BigDecimal.valueOf(33), 
                                          originalPrice.multiply(BigDecimal.valueOf(0.67)).setScale(2, RoundingMode.HALF_UP));
            default:
                return new PromotionDetails("Bulk Discount", "MULTI_BUY", BigDecimal.valueOf(25), 
                                          originalPrice.multiply(BigDecimal.valueOf(0.75)).setScale(2, RoundingMode.HALF_UP));
        }
    }
    
    private static int getDiscountPercentage(String customerType, Random random) {
        switch (customerType) {
            case "vip":
            case "premium":
                return ThreadLocalRandom.current().nextInt(15, 40);
            case "member":
                return ThreadLocalRandom.current().nextInt(10, 30);
            case "student":
                return ThreadLocalRandom.current().nextInt(10, 25);
            case "employee":
                return ThreadLocalRandom.current().nextInt(20, 50);
            default:
                return ThreadLocalRandom.current().nextInt(5, 25);
        }
    }
    
    private static String escapeCsvValue(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    // Data classes
    public static class PromotionTestData {
        public final String brand, country, language, customerType, sku, productName;
        public final String promotionName, discountType, campaignType;
        public final BigDecimal discountValue, originalPrice, expectedPrice;
        public final LocalDate startDate, endDate;
        
        public PromotionTestData(String brand, String country, String language, String customerType,
                               String sku, String productName, String promotionName, String discountType,
                               BigDecimal discountValue, BigDecimal originalPrice, BigDecimal expectedPrice,
                               String campaignType, LocalDate startDate, LocalDate endDate) {
            this.brand = brand;
            this.country = country;
            this.language = language;
            this.customerType = customerType;
            this.sku = sku;
            this.productName = productName;
            this.promotionName = promotionName;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.originalPrice = originalPrice;
            this.expectedPrice = expectedPrice;
            this.campaignType = campaignType;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
    
    private static class ProductTemplate {
        final String name, category;
        final double minPrice, maxPrice;
        
        ProductTemplate(String name, double minPrice, double maxPrice, String category) {
            this.name = name;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.category = category;
        }
    }
    
    private static class PromotionDetails {
        final String name, discountType;
        final BigDecimal discountValue, expectedPrice;
        
        PromotionDetails(String name, String discountType, BigDecimal discountValue, BigDecimal expectedPrice) {
            this.name = name;
            this.discountType = discountType;
            this.discountValue = discountValue;
            this.expectedPrice = expectedPrice;
        }
    }
    
    private static class SeasonalCampaign {
        final String name;
        final LocalDate startDate, endDate;
        final int minDiscount, maxDiscount;
        
        SeasonalCampaign(String name, LocalDate startDate, LocalDate endDate, int minDiscount, int maxDiscount) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.minDiscount = minDiscount;
            this.maxDiscount = maxDiscount;
        }
    }
}