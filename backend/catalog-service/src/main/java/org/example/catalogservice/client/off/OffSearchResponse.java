package org.example.catalogservice.client.off;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import org.example.catalogservice.client.model.OffProduct;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffSearchResponse (
        long count,
        int page,
        @JsonProperty("page_size") int pageSize,
        List<org.example.catalogservice.client.model.OffProduct> products
) {
}
