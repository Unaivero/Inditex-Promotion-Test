package com.inditex.test.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PromotionalPriceRequest {
    @JsonProperty("sku")
    private String sku;
    
    @JsonProperty("brand")
    private String brand;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("customerType")
    private String customerType;
    
    @JsonProperty("includeInventory")
    private boolean includeInventory = false;
    
    public PromotionalPriceRequest() {}
    
    public PromotionalPriceRequest(String sku, String brand, String country) {
        this.sku = sku;
        this.brand = brand;
        this.country = country;
    }
    
    public PromotionalPriceRequest(String sku, String brand, String country, String language, String customerType) {
        this.sku = sku;
        this.brand = brand;
        this.country = country;
        this.language = language;
        this.customerType = customerType;
    }
    
    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    
    public boolean isIncludeInventory() { return includeInventory; }
    public void setIncludeInventory(boolean includeInventory) { this.includeInventory = includeInventory; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionalPriceRequest that = (PromotionalPriceRequest) o;
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
        return String.format("PromotionalPriceRequest{sku='%s', brand='%s', country='%s', customerType='%s'}", 
                           sku, brand, country, customerType);
    }
}