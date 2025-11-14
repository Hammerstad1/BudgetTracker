package org.example.pricingservice.rabbit;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

   public static final String EXCHANGE = "bt.events";

   @Bean
   public TopicExchange priceEventsExchange() {
       return new TopicExchange(EXCHANGE, true, false);
   }


   @Bean
   public Jackson2JsonMessageConverter messageConverter() {
       return new Jackson2JsonMessageConverter();
   }

   @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
       var t = new org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory);
       t.setMessageConverter(messageConverter);
       return t;
   }
}
