package org.example.pricingservice.model;

import java.math.BigDecimal;
import java.util.List;

public record PriceTotalRequest(
        String userId,
        Long storeId
) {}
