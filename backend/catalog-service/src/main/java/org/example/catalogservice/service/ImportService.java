package org.example.catalogservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.catalogservice.client.off.OffClient;
import org.example.catalogservice.entity.Category;
import org.example.catalogservice.entity.ImportProgress;
import org.example.catalogservice.entity.Product;
import org.example.catalogservice.repo.CategoryRepository;
import org.example.catalogservice.repo.ImportProgressRepository;
import org.example.catalogservice.repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.example.catalogservice.client.model.OffProduct;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final OffClient offClient;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final ImportProgressRepository importProgressRepository;

    private static boolean hasNorwayTag(OffProduct off) {
        return off.countriesTags() != null && off.countriesTags().stream()
                .filter(s -> s != null)
                .map(String::toLowerCase)
                .map(s -> s.startsWith("en:") ? s.substring(3) : s)
                .anyMatch(s -> s.equals("norway") || s.equals("no") || s.equals("norge"));
    }

    private static boolean isNorwegianEan(String ean) {
        return ean != null && ean.startsWith("70");
    }


    @Transactional
    public Map<String, Object> importFromOff(String country, int limit, int pageSize, int startPage) {
        int effectiveStart = (startPage > 0)
                ? startPage
                : (importProgressRepository.findByCountryCode(country.toLowerCase())
                        .map(p -> p.getLastPage() + 1)
                        .orElse(1));
        int pagesProcessed = 0, productsProcessed = 0, imported = 0, updated = 0;
        int page = effectiveStart;
        for(int i = 0; i < limit; i++, page++) {
            var pageResp = offClient.search(country, page, pageSize);
            List<OffProduct> items = (pageResp != null && pageResp.products() != null) ? pageResp.products() : List.of();


            if (items.isEmpty()) {
                break;
            }
            pagesProcessed++;
            productsProcessed += items.size();

            for (OffProduct off : items) {
                String ean = off.code();
                if (ean == null || ean.isBlank()) continue;

                if("no".equals(country)) {
                    boolean inNorway = hasNorwayTag(off) || isNorwegianEan(ean);
                    if(!inNorway) continue;
                }

                if("no".equalsIgnoreCase(country)){
                    boolean inNorway =  off.countriesTags() != null && off.countriesTags().stream()
                            .filter(s -> s != null)
                            .map(String::toLowerCase)
                            .map(s -> s.startsWith("en:") ? s.substring(3) : s)
                            .anyMatch(s -> s.equals("norway") || s.equals("no"));
                    if(!inNorway) continue;
                }

                String categoryName = firstOrNull(off.categoriesTags());
                Long categoryId = null;
                if (categoryName != null) {
                    var category = categoryRepository.findByName(categoryName)
                            .orElseGet(() -> categoryRepository.save(
                                    Category.builder().name(categoryName).build()));
                    categoryId = category.getId();
                }

                String countryCode = iso2(firstOrNull(off.countriesTags()));

                var existing = productRepository.findByEan(ean);
                if (existing.isPresent()) {
                    var p = existing.get();
                    p.setName(blankTo(off.productName(), p.getName()));
                    p.setBrand(firstBrand(off.brands()));
                    p.setCountryCode(countryCode != null ? countryCode : p.getCountryCode());
                    p.setCategoryId(categoryId != null ? categoryId : p.getCategoryId());
                    p.setImageUrl(off.imageUrl());
                    p.setQuantity(off.quantity());
                    p.setRaw(writeJson(off));
                    p.setLastSyncedAt(OffsetDateTime.now());
                    productRepository.save(p);
                    updated++;
                } else {
                    var p = Product.builder()
                            .ean(ean)
                            .name(blankTo(off.productName(), "Unknown"))
                            .brand(firstBrand(off.brands()))
                            .countryCode(countryCode)
                            .categoryId(categoryId)
                            .imageUrl(off.imageUrl())
                            .quantity(off.quantity())
                            .raw(writeJson(off))
                            .lastSyncedAt(OffsetDateTime.now())
                            .build();
                    productRepository.save(p);
                    imported++;
                }
            }

            try { Thread.sleep(250); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

        }

        var progress = importProgressRepository.findByCountryCode(country)
                .orElseGet(() -> ImportProgress.builder()
                        .countryCode(country)
                        .lastPage(0)
                        .totalImported(0)
                        .totalUpdated(0)
                        .build());

        progress.setLastPage(page);
        progress.setTotalImported(progress.getTotalImported() + imported);
        progress.setTotalUpdated(progress.getTotalUpdated() + updated);
        progress.setLastRunAt(OffsetDateTime.now());
        progress.setLastRunStatus("OK");

        importProgressRepository.save(progress);

        int lastPage = (pagesProcessed == 0) ? startPage : (startPage + pagesProcessed - 1);

         return Map.of(
                 "country", country,
                 "pageProcessed", pagesProcessed,
                 "productsProcessed", productsProcessed,
                 "imported", imported,
                 "updated", updated,
                 "lastPage", lastPage
         );

    }





    private static String firstBrand(String brandsCsv) { if(brandsCsv == null || brandsCsv.isBlank()) return null; var a=brandsCsv.split(","); return a.length>0?a[0].trim():null;}
    private static String firstOrNull(List<String> tags) { return (tags == null || tags.isEmpty()) ? null : tags.get(0); }
    private static String iso2(String t){if (t == null) return null; return switch (t) { case "norway" -> "NO"; case "sweden" -> "SE"; case "denmark" -> "DK"; default -> t; };}
    private static String blankTo(String s, String def) { return (s == null || s.isBlank()) ? def : s; }


    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }
}
