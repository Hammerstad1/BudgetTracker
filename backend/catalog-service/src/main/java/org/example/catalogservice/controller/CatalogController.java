package org.example.catalogservice.controller;

import org.example.catalogservice.dto.ProductDto;
import org.example.catalogservice.repo.ProductRepository;
import org.example.catalogservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("catalog/products")
public class CatalogController {
    private final ProductService productService;

    public CatalogController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("by-barcode/{ean}")
    public ResponseEntity<ProductDto> byEan(@PathVariable String ean) {
        return ResponseEntity.of(productService.findOrFetchByEan(ean));
    }
}
