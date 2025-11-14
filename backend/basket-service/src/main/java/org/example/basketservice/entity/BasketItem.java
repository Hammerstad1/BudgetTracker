package org.example.basketservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "basket_item")
public class BasketItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    @Column(nullable = false)
    private String ean;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String imageUrl;

    @Column
    private Double price;

    @Column(nullable = false)
    private int qty;

    public BasketItem(Long id, Basket basket, String ean, String name, String imageUrl, int qty) {
        this.id = id;
        this.basket = basket;
        this.ean = ean;
        this.name = name;
        this.imageUrl = imageUrl;
        this.qty = qty;
    }



}
