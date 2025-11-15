package org.example.catalogservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.catalogservice.dto.ProductDto;
import org.springframework.transaction.annotation.Transactional;
import org.example.catalogservice.entity.Category;
import lombok.RequiredArgsConstructor;
import org.example.catalogservice.client.off.OffClient;
import org.example.catalogservice.entity.Product;
import org.example.catalogservice.repo.CategoryRepository;
import org.example.catalogservice.repo.ProductRepository;
import org.flywaydb.core.internal.util.JsonUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OffClient offClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public Optional<ProductDto> findOrFetchByEan(String ean) {
        var existing = productRepository.findByEan(ean);
        if (existing.isPresent()) {
            return existing.map(this::toDto);
        }


        var offOpt = offClient.fetchByEan(ean);
        if (offOpt.isEmpty()) {
            return Optional.empty();
        }

        var off = offOpt.get();
        var categoryName = firstOrNull(off.categoriesTags());
        Long categoryId = null;
        if (categoryName != null) {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(
                            Category.builder().name(categoryName).build()));
            categoryId = category.getId();
        }

        String country = iso2(firstOrNull(off.countriesTags()));
        if (country != null && country.length() != 2) {
            country = null;
        }

        String rawJson;
        try {
            rawJson = objectMapper.writeValueAsString(off);
        } catch (JsonProcessingException e) {
            rawJson = null;
        }

        Product saved = productRepository.save(
                Product.builder()
                        .ean(ean)
                        .name(blankTo(off.productName(), "Unknown"))
                        .brand(firstBrand(off.brands()))
                        .countryCode(country)
                        .categoryId(categoryId)
                        .imageUrl(off.imageUrl())
                        .quantity(off.quantity())
                        .raw(JsonUtils.toJson(off))
                        .lastSyncedAt(OffsetDateTime.now())
                        .build()
        );

        return Optional.of(toDto(saved));
    }

    private ProductDto toDto(Product p) {
        String categoryName = null;
        if (p.getCategoryId() != null) {
            categoryName = categoryRepository.findById(p.getCategoryId())
                    .map(Category::getName)
                    .orElse(null);
        }

        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getEan(),
                p.getBrand(),
                p.getSize(),
                categoryName,
                p.getImageUrl()
        );
    }
    
    private static String firstBrand(String brandsCsv) {
        if(brandsCsv == null || brandsCsv.isBlank()) return null;
        var parts = brandsCsv.split(",");
        return parts.length > 0 ? parts[0].trim() : null;
    }
    
    private static String firstOrNull(List<String> tags) {
        return (tags == null || tags.isEmpty()) ? null : tags.get(0);
    }
    
    private static String iso2(String offCountryTag) {
        if (offCountryTag == null) {
            return null;
        }
        return switch (offCountryTag) {
            case "norway" -> "NO";
            case "sweden" -> "SE";
            case "denmark" -> "DK";
            default -> offCountryTag;
        };

    }
    
    private static String blankTo(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }






}
