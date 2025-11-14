package org.example.budgetservice.model;

public record BudgetDto (String userId, double monthLimit, double spent, double remaining){
}
