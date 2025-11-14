package org.example.catalogservice.client.off;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.catalogservice.client.model.OffProduct;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProductResponse (
        String code,
        @com.fasterxml.jackson.annotation.JsonProperty("product_name") String productName,
        String brands,
        String country,
        String quantity,
        @com.fasterxml.jackson.annotation.JsonProperty("image_url") String imageUrl
){
}
