package org.example.basketservice.service;

import org.example.basketservice.entity.Basket;
import org.example.basketservice.entity.BasketItem;
import org.example.basketservice.model.AddItemRequest;
import org.example.basketservice.model.BasketDto;
import org.example.basketservice.model.BasketItemDto;
import org.example.basketservice.repo.BasketItemRepository;
import org.example.basketservice.repo.BasketRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BasketDomainService {

    private final BasketRepository basketRepository;
    private final BasketItemRepository basketItemRepository;
    private final BasketSseService sse;
    private final RabbitTemplate rabbitTemplate;

    public BasketDomainService(BasketRepository basketRepository, BasketItemRepository basketItemRepository, BasketSseService sse, RabbitTemplate rabbitTemplate) {
        this.basketRepository = basketRepository;
        this.basketItemRepository = basketItemRepository;
        this.sse = sse;
        this.rabbitTemplate = rabbitTemplate;
    }


    private Basket getOrCreateActivebasket() {
        return basketRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> {
                    Basket b = new Basket();
                    b.setUserId("default");
                    return basketRepository.save(b);
                });
    }

    public BasketDto getActiveBasketSnapshot() {
        return basketRepository.findTopByOrderByIdAsc()
                .map(b -> {
                    List<BasketItem> items = basketItemRepository.findByBasketId(b.getId());
                    return toDto(b, items);
                })
                .orElseGet(() -> new BasketDto(0L, List.of()));
    }

    private BasketDto getActiveBasketForBroadcast() {
        Basket basket = getOrCreateActivebasket();
        List<BasketItem> items = basketItemRepository.findByBasketId(basket.getId());
        return toDto(basket, items);
    }

    @Transactional
    public BasketDto getActiveBasket() {
        Basket basket = getOrCreateActivebasket();
        List<BasketItem> basketItems = basketItemRepository.findByBasketId(basket.getId());
        return toDto(basket, basketItems);
    }

    @Transactional
    public void addItem(AddItemRequest addItemRequest) {
        Basket b = getOrCreateActivebasket();
        BasketItem basketItem = basketItemRepository.findByBasketIdAndEan(b.getId(), addItemRequest.ean())
                .map(i -> { i.setQty(i.getQty() + 1); return i; })
                .orElseGet(() -> {
                    BasketItem i = new BasketItem();
                    i.setBasket(b);
                    i.setEan(addItemRequest.ean());
                    i.setName(addItemRequest.name());
                    i.setPrice(addItemRequest.price());
                    i.setImageUrl(addItemRequest.imageUrl());
                    i.setQty(1);
                    return i;
                });
        basketItemRepository.save(basketItem);

        BasketDto basketDto = getActiveBasketForBroadcast();
        sse.broadcast(basketDto);
        publishBasketEvent(basketDto);
    }

    @Transactional
    public void clean () {
        Basket basket = getOrCreateActivebasket();
        basketItemRepository.deleteByBasketId(basket.getId());

        BasketDto basketDto = getActiveBasketForBroadcast();
        sse.broadcast(basketDto);
        publishBasketEvent(basketDto);
    }

    private BasketDto toDto(Basket basket, List<BasketItem> items) {
        List<BasketItemDto> dtoItems = items.stream()
                .map(i -> new BasketItemDto(i.getEan(), i.getName(), i.getQty(), i.getImageUrl(), i.getPrice()))
                .toList();
        return new BasketDto(basket.getId(), dtoItems);
    }

    @Transactional
    public void removeItem(String ean, int qty) {
        Basket b = getOrCreateActivebasket();

        var itemOpt = basketItemRepository.findByBasketIdAndEan(b.getId(), ean);
        if (itemOpt.isEmpty()) {
            return;
        }

        BasketItem item = itemOpt.get();
        if (item.getQty() <= qty) {
            basketItemRepository.delete(item);
        } else {
            item.setQty(item.getQty() - qty);
            basketItemRepository.save(item);
        }

        BasketDto basketDto = getActiveBasketForBroadcast();
        sse.broadcast(basketDto);
        publishBasketEvent(basketDto);
    }

    private void publishBasketEvent(BasketDto basketDto) {
        double total = basketDto.items().stream()
                .mapToDouble(item -> {
                    Double price = item.price();
                    return (price != null ? price : 0.0) * item.qty();
                })
                .sum();

        Map<String, Object> event = Map.of(
                "type", "BasketUpdated",
                "userId", "u1",
                "basketId", basketDto.basketId(),
                "total", total
        );
        System.out.println("=== PUBLISHING BASKET EVENT ===");
        System.out.println("EVENT: " + event);
        rabbitTemplate.convertAndSend("bt.events","basket.updated", event);
        System.out.println("=== PUBLISHING BASKET EVENT ===");
    }
}
