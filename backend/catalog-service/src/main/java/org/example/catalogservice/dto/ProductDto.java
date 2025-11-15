package org.example.catalogservice.dto;

public record ProductDto (
        Long id,
        String name,
        String ean,
        String brand,
        String size,
        String category,
        String imageUrl
){

}
