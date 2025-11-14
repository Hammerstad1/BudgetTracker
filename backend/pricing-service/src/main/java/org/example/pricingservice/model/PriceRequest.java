package org.example.pricingservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PriceRequest (
        @NotBlank String ean,
        @NotNull @Positive BigDecimal price,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        Long storeId

) {}
