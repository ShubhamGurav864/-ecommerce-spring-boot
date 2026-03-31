package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🛒 Many items belong to one cart
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // 📦 Each item is linked to a product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 🔢 Quantity of product
    @Column(nullable = false)
    private Integer quantity;

    // 💰 Price snapshot (important for future price changes)
    @Column(nullable = false)
    private BigDecimal price;
}