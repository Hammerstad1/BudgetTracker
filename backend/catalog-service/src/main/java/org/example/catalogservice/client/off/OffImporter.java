package org.example.catalogservice.client.off;

import org.example.catalogservice.client.model.OffProduct;
import org.example.catalogservice.entity.Product;
import org.example.catalogservice.repo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.example.catalogservice.client.off.QuantityParser.parse;

@Service
public class OffImporter {
    private static final Logger log = LoggerFactory.getLogger(OffImporter.class);

    private final OffClient offClient;
    private final ProductRepository repository;

    public OffImporter(OffClient offClient, ProductRepository repository) {
        this.offClient = offClient;
        this.repository = repository;
    }

    private Product mapOffProduct(OffProduct off) {
        Product p = new Product();
        p.setEan(off.code());
        p.setName(off.productName());
        p.setBrand(off.brands());
        p.setCountryCode(off.country());
        p.setImageUrl(off.imageUrl());

        p.setQuantity(off.quantity());
        p.setRaw(off.quantity());

        var q = parse(off.quantity());
        if (q != null && q.totalValue() != null && q.totalUnit() != null) {
            p.setQuantityValue(q.totalValue());
            p.setQuantityUnit(q.totalUnit());
        }

        p.setLastSyncedAt(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        return p;

    }


    @Transactional
    public ImportResult importpage (String country, int page, int pageSize) {
        OffSearchResponse response = offClient.search(country, page, pageSize);

        int returned = (response != null && response.products() != null)
                ? response.products().size() : 0;
        log.info("OFF page={} pageSize={} returned products={}", page, pageSize, returned);


        List<OffProduct> items = (response != null && response.products() != null) ? response.products() : List.of();

        int seen = 0, imported = 0, updated = 0;

        for (OffProduct off: items) {
            seen++;
            var mapped = mapOffProduct(off);
            var existing = repository.findByEan(mapped.getEan()).orElse(null);
            if (existing == null) {
                repository.save(mapped);
                imported++;
            } else {
                existing.setName(mapped.getName());
                existing.setBrand(mapped.getBrand());
                existing.setCountryCode(mapped.getCountryCode());
                existing.setImageUrl(mapped.getImageUrl());
                existing.setQuantity(mapped.getQuantity());
                existing.setRaw(mapped.getRaw());
                existing.setQuantityValue(mapped.getQuantityValue());
                existing.setQuantityUnit(mapped.getQuantityUnit());
                existing.setLastSyncedAt(OffsetDateTime.now());
                repository.save(existing);
                updated++;
            }

        }

        return new ImportResult(seen, imported, updated);
    }

    public record ImportResult(int seen, int imported, int updated) { }
}
