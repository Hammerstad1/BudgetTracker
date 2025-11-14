package org.example.pricingservice.repo;

import org.example.pricingservice.model.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductPriceRepo extends JpaRepository<ProductPrice, Long> {
    Optional<ProductPrice> findByEanAndStoreId(String ean, Long storeId);
    Optional<ProductPrice> findByEanAndStoreIdIsNull(String ean);
}
