package org.example.basketservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public record BasketDto(Long basketId, List<BasketItemDto> items) {}
