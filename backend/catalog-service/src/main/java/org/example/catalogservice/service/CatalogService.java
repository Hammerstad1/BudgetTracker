package org.example.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.example.catalogservice.rabbit.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/catalog/import")
@RequiredArgsConstructor
public class CatalogService {
    private final ImportService importService;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/openfoodfacts")
    public ResponseEntity<Map<String, Object>> importOff(
            @RequestParam String country,
            @RequestParam(defaultValue = "500") int max,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "0") int startPage
    ) {
        var summary = importService.importFromOff(country, max, pageSize, startPage);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/dev/publish")
    public ResponseEntity<String> publishDev() {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                "catalog.product.created", Map.of("id", "test-1", "eventType", "CREATED"));
        return ResponseEntity.ok("sent");
    }

}
