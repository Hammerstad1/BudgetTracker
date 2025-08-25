package org.example.budgettracker;

import org.springframework.boot.SpringApplication;

public class TestBudgetTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.from(BudgetTrackerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
