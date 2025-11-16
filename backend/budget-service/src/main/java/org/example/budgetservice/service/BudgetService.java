package org.example.budgetservice.service;

import lombok.RequiredArgsConstructor;
import org.example.budgetservice.config.BudgetConfig;
import org.example.budgetservice.entity.Budget;
import lombok.extern.slf4j.Slf4j;
import org.example.budgetservice.model.BudgetDto;
import org.example.budgetservice.repo.BudgetRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final RabbitTemplate rabbitTemplate;
    private final BudgetConfig budgetConfig;

    public Optional<Budget> findBudget(String userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public BudgetDto summary(String userId) {
        Budget budget = budgetRepository.findByUserId(userId)
                .orElse(transientDefault(userId));
        double limit = nvl(budget.getMonthlyBudget()).doubleValue();
        double spent = nvl(budget.getCurrentSpent()).doubleValue();

        return new BudgetDto(userId, limit, spent, limit - spent);
    }

    private Budget transientDefault(String userId) {
        Budget b = new Budget();
        b.setUserId(userId);
        b.setMonthlyBudget(BigDecimal.valueOf(2000));
        b.setWarningThreshold(budgetConfig.getWarningThresholdAsDecimal());
        b.setNotificationEnabled(true);
        b.setCurrentSpent(BigDecimal.ZERO);
        return b;
    }

    @Transactional
    public Budget setBudget(String userId, BigDecimal monthlyBudget, BigDecimal threshold, boolean enabled) {
        Budget budget = budgetRepository.findByUserId(userId)
                .orElse(new Budget());

        budget.setUserId(userId);
        budget.setMonthlyBudget(monthlyBudget);
        budget.setWarningThreshold(threshold);
        budget.setNotificationEnabled(enabled);

        if (budget.getCurrentSpent() == null) {
            budget.setCurrentSpent(BigDecimal.ZERO);
        }

        return budgetRepository.save(budget);
    }

    @Transactional
    public void updateSpentFromTotal(String userId, BigDecimal total) {
        int updated = budgetRepository.updateCurrentSpent(userId, total);
        if (updated == 0) {
            log.warn("Budget update failed for userId={}, total={}", userId, total);
            return;
        }
    }

    private void checkAndNotify(Budget budget, BigDecimal currentTotal) {
        if(!budget.isNotificationEnabled()) {
            return;
        }

        BigDecimal monthly  = nvl(budget.getMonthlyBudget());
        BigDecimal threshold = nvl(budget.getWarningThreshold());
        BigDecimal limit = monthly.multiply(threshold);

        log.info("Budget check for {} -> total={}, limit={} (monthly {} * threshold {})",
                budget.getUserId(), currentTotal, limit, monthly, threshold);

        if (nvl(currentTotal).compareTo(limit) >= 0) {
            log.warn("Budget threshold exceeded {}: {} >= {}", budget.getUserId(), currentTotal, limit);
            publishBudgetAlert(budget.getUserId(), currentTotal, monthly, threshold);
        } else {
            log.info("Within budget for {}: total={} < limit={}", budget.getUserId(), currentTotal, limit);
        }

    }

    private void publishBudgetAlert(String userId, BigDecimal currentTotal, BigDecimal monthly, BigDecimal threshold) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "BUDGET_ALERT");
        event.put("userId", userId);
        event.put("total", currentTotal);
        event.put("monthlyBudget", monthly);
        event.put("threshold", threshold);
        event.put("currency", "NOK");
        event.put("message", "Budget threshold reached");
        event.put("timestamp", LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend("bt.events", "budget.threshold.exceeded", event);
        log.info("Published budget alert event for user {}", userId);
    }


    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
