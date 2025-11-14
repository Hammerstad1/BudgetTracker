package org.example.catalogservice.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProductResponse(
        int status,
        OffProduct product
) {}