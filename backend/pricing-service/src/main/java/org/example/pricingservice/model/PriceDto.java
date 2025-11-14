package org.example.pricingservice.model;

public record PriceDto (
        String ean,
        java.math.BigDecimal price,
        String currency,
        Long storeId){

}