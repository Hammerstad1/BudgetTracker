package org.example.budgetservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.budgetservice.entity.Budget;
import org.example.budgetservice.model.BudgetDto;
import org.example.budgetservice.service.BudgetService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @GetMapping("/{userId}")
    public BudgetDto getBudget(@PathVariable String userId){
        return budgetService.summary(userId);
    }

    @PostMapping("/set")
    public Budget setBudget(@RequestBody Map<String, Object> request){
        String userId = (String) request.get("userId");
        BigDecimal monthlyBudget = new BigDecimal(request.get("monthlyBudget").toString());
        BigDecimal threshold = new BigDecimal(request.get("warningThreshold").toString());
        boolean enabled = (Boolean)  request.getOrDefault("notificationEnabled", true);

        return budgetService.setBudget(userId, monthlyBudget, threshold, enabled);
    }

}
