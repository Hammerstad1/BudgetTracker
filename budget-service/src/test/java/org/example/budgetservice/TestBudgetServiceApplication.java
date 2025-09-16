package org.example.budgetservice;

import org.springframework.boot.SpringApplication;

public class TestBudgetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(BudgetServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
