package org.example.pricingservice.service;


import lombok.RequiredArgsConstructor;
import org.example.pricingservice.model.PriceRequest;
import org.example.pricingservice.model.PriceTotalRequest;
import org.example.pricingservice.model.PriceTotalResponse;
import org.example.pricingservice.model.ProductPrice;
import org.example.pricingservice.repo.ProductPriceRepo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final ProductPriceRepo productPriceRepo;
    private final RabbitTemplate rabbitTemplate;

    @Value("${basket.base-url}")
    private String basketBaseUrl;

    @Value("${price.events.exchange:price.events}")
    private String priceEventsExchange;

    @Value("${price.events.routing-key:price.upserted}")
    private String priceUpsertRoutingKey;

    private RestTemplate restTemplate() {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(2000);
        return new RestTemplate(f);
    }

    public ProductPrice upsertPrice(PriceRequest req){
        var existing = productPriceRepo.findByEanAndStoreId(req.ean(), req.storeId()).orElse(null);
        if (existing == null && req.storeId() != null){
            existing = productPriceRepo.findByEanAndStoreIdIsNull(req.ean()).orElse(null);
        }

        if (existing == null) {
            var p = new ProductPrice();
            p.setEan(req.ean());
            p.setStoreId(req.storeId());
            p.setPrice(req.price());
            p.setCurrency(req.currency());
            var saved = productPriceRepo.save(p);

            publishPriceUpserted(saved);
            return saved;
        } else {
            existing.setPrice(req.price());
            existing.setCurrency(req.currency());
            var saved = productPriceRepo.save(existing);
            publishPriceUpserted(saved);
            return saved;
        }

    }

    public Optional<ProductPrice> getPrice(String ean, Long storeId) {
        if (storeId != null) {
            return productPriceRepo.findByEanAndStoreId(ean, storeId);
        }
        return productPriceRepo.findByEanAndStoreIdIsNull(ean);
    }

    public PriceTotalResponse computeTotal(PriceTotalRequest req){
        try {
            var url = basketBaseUrl + "/items";
            var rt = restTemplate();

            BasketDto basket = rt.getForObject(url, BasketDto.class);

            if (basket == null || basket.items() == null || basket.items().isEmpty()) {
                return new PriceTotalResponse(BigDecimal.ZERO, "NOK", List.of());
            }

            BigDecimal total = BigDecimal.ZERO;
            String currency = "NOK";
            List<String> missing = new ArrayList<>();

            for (var it : basket.items()) {

                if (it.price() != null) {
                    total = total.add(BigDecimal.valueOf(it.price()).multiply(BigDecimal.valueOf(it.qty())));
                    continue;
                }


                var opt = getPrice(it.ean(), req.storeId());
                if (opt.isEmpty()) {
                    missing.add(it.ean());
                    continue;
                }
                var p = opt.get();
                currency = p.getCurrency();
                total = total.add(p.getPrice().multiply(BigDecimal.valueOf(it.qty())));
            }

            publishTotalCalculated(req.userId(), basket.basketId, total, currency);

            return new PriceTotalResponse(total, currency, missing);

        }catch (Exception e){
            System.err.println("Failed to fetch the basket/prices: " + e.getMessage());
            return new PriceTotalResponse(BigDecimal.ZERO,"NOK", List.of());
        }
    }

    private void publishPriceUpserted(ProductPrice p){
        Map<String, Object> evt = new java.util.HashMap<>();
        evt.put("type", "PriceUpserted");
        evt.put("ean", p.getEan());

        if (p.getStoreId() != null) evt.put("storeId", p.getStoreId());
        if (p.getPrice() != null) evt.put("price", p.getPrice());
        if (p.getCurrency() != null) evt.put("currency", p.getCurrency());
        if (p.getUpdatedAt() != null) evt.put("updatedAt", p.getUpdatedAt());

        rabbitTemplate.convertAndSend(priceEventsExchange, priceUpsertRoutingKey, evt);
    }

    private void publishTotalCalculated(String userId, Long basketId, BigDecimal total, String currency){
        Map<String, Object> evt = new HashMap<>();
        evt.put("type", "TotalCalculated");
        evt.put("userId", userId);
        evt.put("basketId", basketId);
        evt.put("total", total);
        evt.put("currency", currency);
        evt.put("calculatedAt", LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend("bt.events", "pricing.total.calculated", evt);

        System.out.println("Published pricing event for user " + userId + " with total " + total);
    }

    public record BasketDto(Long basketId, List<BasketItemView> items) {}
    public record BasketItemView(String ean, int qty, Double price) {}


}
