package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
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
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 🧾 Order status
    private String status;

    // 💳 Payment status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    // 💳 Payment method
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    // 💳 Gateway details
    @Column(columnDefinition = "TEXT")
    private String paymentId;  // Stripe/Razorpay ID
    
    @Column(columnDefinition = "TEXT")
    private String paymentUrl;  // redirect URL

    private String razorpayOrderId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}