package org.example.pricingservice.controller;

import org.example.pricingservice.model.PriceDto;
import org.example.pricingservice.model.PriceRequest;
import org.example.pricingservice.model.PriceTotalRequest;
import org.example.pricingservice.model.PriceTotalResponse;
import org.example.pricingservice.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/pricing")
public class PricingController {
    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }


    @PostMapping("/upsert")
    public ResponseEntity<?> upsertPrice(@RequestBody PriceRequest priceRequest){
        var saved = pricingService.upsertPrice(priceRequest);
        return ResponseEntity.ok(
                new PriceDto(saved.getEan(), saved.getPrice(), saved.getCurrency(), saved.getStoreId())
        );
    }

    @GetMapping("/{ean}")
    public ResponseEntity<PriceDto> get(@PathVariable String ean, @RequestParam(required = false) Long storeId) {
        return pricingService.getPrice(ean, storeId)
                .map(p -> ResponseEntity.ok(new PriceDto(
                    p.getEan(),
                    p.getPrice(),
                    p.getCurrency(),
                    p.getStoreId()
        )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/total")
    public ResponseEntity<PriceTotalResponse> total(@RequestBody PriceTotalRequest req)  {
        return ResponseEntity.ok(pricingService.computeTotal(req));
    }

}
