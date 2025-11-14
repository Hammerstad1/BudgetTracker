package org.example.basketservice.repo;

import org.example.basketservice.entity.Basket;
import org.example.basketservice.entity.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {
    List<BasketItem> findByBasketId(Long basketId);
    Optional<BasketItem> findByBasketIdAndEan(Long basketId, String ean);
    void deleteByBasketId(Long basketId);
    void deleteByBasketIdAndEan(Long basketId, String ean);

}
