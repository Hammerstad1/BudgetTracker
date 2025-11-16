package org.example.budgetservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Component
public class BudgetConfig {

    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    private int warningThresholdPercent;

    @PostConstruct
    public void loadConfig() {

        String url = "http://consul:8500/v1/kv/config/budget-service/warningThreshold?raw=true";

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                warningThresholdPercent = Integer.parseInt(response.trim());
            } else {
                warningThresholdPercent = 80;
            }
        } catch (Exception e) {
            log.warn("Could not load warning threshold from Consul, using default 80%", e);
            warningThresholdPercent = 80;
        }

        log.warn("Budget warningThreshold loaded from Consul or fallback: {}", warningThresholdPercent);
    }

    public BigDecimal getWarningThresholdAsDecimal() {
        return BigDecimal
                .valueOf(warningThresholdPercent)
                .divide(BigDecimal.valueOf(100));

    }
}
