package com.inditex.test.model;

import java.util.Objects;

public class PromotionTestData {
    private final String sku;
    private final String brand;
    private final String country;
    private final String language;
    private final String customerType;
    private final String promotionType;
    private final String discountValue;
    private final String expectedDiscountedPrice;
    private final String originalPrice;
    
    public PromotionTestData(String sku, String brand, String country, String language, 
                           String customerType, String promotionType, String discountValue, 
                           String expectedDiscountedPrice, String originalPrice) {
        this.sku = sku;
        this.brand = brand;
        this.country = country;
        this.language = language;
        this.customerType = customerType;
        this.promotionType = promotionType;
        this.discountValue = discountValue;
        this.expectedDiscountedPrice = expectedDiscountedPrice;
        this.originalPrice = originalPrice;
    }
    
    // Getters
    public String getSku() { return sku; }
    public String getBrand() { return brand; }
    public String getCountry() { return country; }
    public String getLanguage() { return language; }
    public String getCustomerType() { return customerType; }
    public String getPromotionType() { return promotionType; }
    public String getDiscountValue() { return discountValue; }
    public String getExpectedDiscountedPrice() { return expectedDiscountedPrice; }
    public String getOriginalPrice() { return originalPrice; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionTestData that = (PromotionTestData) o;
        return Objects.equals(sku, that.sku) &&
               Objects.equals(brand, that.brand) &&
               Objects.equals(country, that.country);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sku, brand, country);
    }
    
    @Override
    public String toString() {
        return String.format("PromotionTestData{sku='%s', brand='%s', country='%s', language='%s', " +
                           "customerType='%s', promotionType='%s', discountValue='%s'}", 
                           sku, brand, country, language, customerType, promotionType, discountValue);
    }
}