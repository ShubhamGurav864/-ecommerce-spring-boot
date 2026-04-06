package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🧑 Order belongs to user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 📦 One order has many items
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // 💰 Total price
    @Column(nullable = false)
    private BigDecimal totalAmount;

    // 📅 Order time
    private LocalDateTime createdAt;

    // 🧾 Status (we'll expand later)
    private String status;
}