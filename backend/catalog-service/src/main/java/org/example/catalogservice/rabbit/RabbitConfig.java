package org.example.catalogservice.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "bt.events";
    @Bean TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @RestController
    @RequiredArgsConstructor
    class DevPublishController {
        private final RabbitTemplate rabbitTemplate;

        @PostMapping("/dev/publish")
        public void publish() {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "catalog.import.created", Map.of("id", "test-1", "evenType", "CREATED"));
        }
    }
}
