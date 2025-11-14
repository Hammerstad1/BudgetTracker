package org.example.basketservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddItemRequest (String ean, String name, String imageUrl, Double price) {}
