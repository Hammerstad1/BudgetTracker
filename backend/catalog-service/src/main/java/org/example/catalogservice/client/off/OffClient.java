package org.example.catalogservice.client.off;

import org.example.catalogservice.client.model.OffProduct;

import java.util.Optional;

public interface OffClient {
    Optional<OffProduct> fetchByEan(String ean);

    OffSearchResponse search(String query, int page, int pageSize);
}
