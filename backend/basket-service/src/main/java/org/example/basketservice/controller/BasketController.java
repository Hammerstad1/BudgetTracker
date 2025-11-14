package org.example.basketservice.controller;


import org.example.basketservice.model.AddItemRequest;
import org.example.basketservice.model.BasketDto;
import org.example.basketservice.service.BasketDomainService;
import org.example.basketservice.service.BasketSseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class BasketController {

    private final BasketDomainService basketService;
    private final BasketSseService basketSseService;

    public BasketController(BasketDomainService basketService, BasketSseService basketSseService) {
        this.basketService = basketService;
        this.basketSseService = basketSseService;
    }


    @PostMapping("/items")
    public void addItem(@RequestBody AddItemRequest addItemRequest) {
        if (addItemRequest.ean() == null || addItemRequest.name() == null) {
            throw new IllegalArgumentException("Ean can't be empty");
        }
        basketService.addItem(addItemRequest);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return basketSseService.subscribe(basketService.getActiveBasketSnapshot());
    }

    @GetMapping("/items")
    public BasketDto getBasket() {
        return basketService.getActiveBasketSnapshot();
    }

    @DeleteMapping("/items/{ean}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @PathVariable String ean,
            @RequestParam(name = "qty", defaultValue = "1") int qty
    ) {
        basketService.removeItem(ean, qty);
    }


    @DeleteMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> clearBasket(){
        basketService.clean();
        return ResponseEntity.noContent().build();
    }

}
