package org.example.pricingservice.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class DevPriceController {
    private final RabbitTemplate rabbitTemplate;
    public DevPriceController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/dev/price")
    public ResponseEntity<String> send(@RequestParam("userId") String userId,
                                       @RequestParam("total") double total) {
        rabbitTemplate.convertAndSend("bt.events", "pricing.item.priced", Map.of(
                "userId", userId,
                "basketId", "b-1",
                "total", total,
                "currency", "NOK",
                "calculatedAt", Instant.now().toString()
        ));
        return ResponseEntity.ok("sent");
    }
}
