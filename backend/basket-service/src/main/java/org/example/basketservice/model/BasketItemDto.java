package org.example.basketservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record BasketItemDto(String ean, String name, int qty, String imageUrl, Double price) {}
