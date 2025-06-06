package com.inditex.test.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class PromotionalPriceResponse {
    @JsonProperty("sku")
    private String sku;
    
    @JsonProperty("brand")
    private String brand;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("originalPrice")
    private BigDecimal originalPrice;
    
    @JsonProperty("promotionalPrice")
    private BigDecimal promotionalPrice;
    
    @JsonProperty("discountPercentage")
    private BigDecimal discountPercentage;
    
    @JsonProperty("discountAmount")
    private BigDecimal discountAmount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("promotionName")
    private String promotionName;
    
    @JsonProperty("promotionCode")
    private String promotionCode;
    
    @JsonProperty("validFrom")
    private LocalDateTime validFrom;
    
    @JsonProperty("validTo")
    private LocalDateTime validTo;
    
    @JsonProperty("stockQuantity")
    private Integer stockQuantity;
    
    @JsonProperty("isInStock")
    private Boolean isInStock;
    
    @JsonProperty("customerType")
    private String customerType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    public PromotionalPriceResponse() {}
    
    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    
    public BigDecimal getPromotionalPrice() { return promotionalPrice; }
    public void setPromotionalPrice(BigDecimal promotionalPrice) { this.promotionalPrice = promotionalPrice; }
    
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    
    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
    
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    
    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public Boolean getIsInStock() { return isInStock; }
    public void setIsInStock(Boolean isInStock) { this.isInStock = isInStock; }
    
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean hasPromotion() {
        return promotionalPrice != null && originalPrice != null && 
               promotionalPrice.compareTo(originalPrice) < 0;
    }
    
    public BigDecimal getSavings() {
        if (originalPrice != null && promotionalPrice != null) {
            return originalPrice.subtract(promotionalPrice);
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionalPriceResponse that = (PromotionalPriceResponse) o;
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
        return String.format("PromotionalPriceResponse{sku='%s', brand='%s', originalPrice=%s, promotionalPrice=%s, currency='%s'}", 
                           sku, brand, originalPrice, promotionalPrice, currency);
    }
}