package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 📦 Many items belong to one order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    // 🛍️ Reference to product (optional but useful)
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // 📛 Product name snapshot
    @Column(nullable = false)
    private String productName;

    // 🔢 Quantity
    @Column(nullable = false)
    private Integer quantity;

    // 💰 Price at time of order (VERY IMPORTANT)
    @Column(nullable = false)
    private BigDecimal price;

    // 💵 Total for this item
    @Column(nullable = false)
    private BigDecimal totalPrice;
}