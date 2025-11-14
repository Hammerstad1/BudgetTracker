package org.example.budgetservice.model;

public record PricedEvent(
        String userId,
        String basketId,
        double total,
        String currency,
        String calculatedAt
) {
}
