package org.example.pricingservice.model;

import org.example.pricingservice.service.PricingService;

public record PriceTotalResponse (java.math.BigDecimal total, String currency, java.util.List<String> missing) {
}
