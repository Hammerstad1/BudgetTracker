package org.example.catalogservice.repo;

import org.example.catalogservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository <Product, Long> {
    Optional<Product> findByEan(String ean);


}
