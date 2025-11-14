package org.example.basketservice.repo;

import org.example.basketservice.entity.Basket;
import org.example.basketservice.entity.BasketItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Long> {
    Optional<Basket> findTopByOrderByIdAsc();
}
