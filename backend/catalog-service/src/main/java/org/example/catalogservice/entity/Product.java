package org.example.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ean;
    private String name;
    private String brand;
    private String size;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "image_url")
    private String imageUrl;

    private String quantity;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @Column(columnDefinition = "text")
    private String raw;

    @Column(name = "quantity_value", precision = 12, scale = 3)
    private BigDecimal quantityValue;

    @Column(name = "quantity_unit", length = 8)
    private String quantityUnit;


}
