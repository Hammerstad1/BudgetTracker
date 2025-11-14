package org.example.budgetservice.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.budgetservice.service.BudgetService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.amqp.core.QueueBuilder.durable;

@EnableRabbit
@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "bt.events";
    public static final String PRICE_QUEUE = "budget.pricing.events";
    public static final String BUDGET_ALERT_QUEUE = "frontend.budget.alert";
    public static final String BASKET_QUEUE = "budget.basket.events";

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }
    @Bean
    Queue priceQueue() {
        return durable(PRICE_QUEUE).build();
    }

    @Bean
    Queue budgetAlertQueue() {
        return QueueBuilder.durable(BUDGET_ALERT_QUEUE).build();
    }

    @Bean
    Binding priceBinding(Queue priceQueue, TopicExchange exchange) {
        return BindingBuilder.bind(priceQueue).to(exchange).with("pricing.#");
    }

    @Bean
    Binding budgetAlertBinding(Queue budgetAlertQueue, TopicExchange exchange) {
        return BindingBuilder.bind(budgetAlertQueue).to(exchange).with("budget.threshold.exceeded");
    }

    @Bean
    Queue basketQueue() {
        return durable(BASKET_QUEUE).build();
    }

    @Bean
    Binding basketBinding(Queue basketQueue, TopicExchange exchange) {
        return BindingBuilder.bind(basketQueue).to(exchange).with("basket.#");
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Service
    @Slf4j
    @RequiredArgsConstructor
    public static class PricingEventListener{
         private final BudgetService budgetService;
         private final RabbitTemplate rabbitTemplate;

         @RabbitListener(queues = RabbitConfig.PRICE_QUEUE)
        public void onPriceEvent(Map<String, Object> event) {
             log.info("Budget received pricing event{}", event);

             String type = (String) event.get("type");
             if (!"TotalCalculated".equals(type) && !"TotalUpdated".equals(type)) {
                 return;
             }

             String userId = (String) event.get("userId");
             Object totalObj = event.get("total");
             if (userId == null || totalObj == null) {
                 log.warn("Missing userId or total in event: {}", event);
                 return;
             }


             BigDecimal total = totalObj instanceof Number
                     ? BigDecimal.valueOf(((Number) totalObj).doubleValue())
                     : new BigDecimal(totalObj.toString());

             log.info("Updating budget for user {} with total {}", userId, total);
             checkBudgetAndAlert(userId, total);
        }

        @RabbitListener(queues = RabbitConfig.BASKET_QUEUE)
        public void onBasketEvent(Map<String, Object> event) {
             log.info("Budget received basket event: {}", event);

             String type = (String) event.get("type");
             if (!"BasketUpdated".equals(type)) {
                 return;
             }

             String userId = (String) event.get("userId");
             Object totalObj = event.get("total");

             if (userId == null || totalObj == null) {
                 log.warn("Missing userId or total in event: {}", event);
                 return;
             }

             BigDecimal total = totalObj instanceof Number
                     ? BigDecimal.valueOf(((Number) totalObj).doubleValue())
                     : new BigDecimal(totalObj.toString());

             log.info("Updating basket for user {} with total {}", userId, total);
             checkBudgetAndAlert(userId, total);
        }

        private void checkBudgetAndAlert(String userId, BigDecimal total) {
             budgetService.updateSpentFromTotal(userId, total);

            var budgetOpt = budgetService.findBudget(userId);
            if (budgetOpt.isEmpty()) {
                log.info("No budget set for {}, skipping alert", userId);
                return;
            }

            var b = budgetOpt.get();

            boolean notifications = b.isNotificationEnabled();
            BigDecimal monthly = b.getMonthlyBudget();
            BigDecimal threshold = b.getWarningThreshold();

            log.info("Summary for {} -> monthly {}, threshold={}, notifications={}",
                    userId, monthly, threshold, notifications);

            if(!notifications) {
                log.info("Notification disabled for {}, skipping alert", userId);
                return;
            }

            BigDecimal limit = monthly.multiply(threshold);
            if(total.compareTo(limit) >= 0) {
                Map<String, Object> alert = Map.of(
                        "type", "BUDGET_ALERT",
                        "userId", userId,
                        "currentTotal", total,
                        "budget", monthly,
                        "thresholdPercentage", threshold.multiply(BigDecimal.valueOf(100)).intValue(),
                        "currency", "NOK",
                        "message", "Budget threshold reached"
                );

                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "budget.threshold.exceeded", alert);
                log.info("Published BUDGET_ALERT for {} (total={} < limit={})", userId, total, limit);
            } else {
                log.info("Below threshold for {} (total={} >= limit={})", userId, total, limit);
            }

        }
    }

}

