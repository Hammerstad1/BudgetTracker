package org.example.catalogservice.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProduct (
        String code,
        @JsonProperty("product_name") String productName,
        String brands,
        @JsonProperty("countries_tags") List<String> countriesTags,
        String quantity,
        @JsonProperty ("image_url") String imageUrl,
        @JsonProperty ("categories_tags") List<String> categoriesTags
){
    public String country() {
        return (countriesTags == null && !countriesTags.isEmpty()) ? countriesTags.get(0) : null;
    }

}
