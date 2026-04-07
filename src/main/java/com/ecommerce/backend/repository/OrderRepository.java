package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 📦 Get all orders of a user
    List<Order> findByUser(User user);

    // 💳 Find order by Stripe payment/session ID
    Optional<Order> findByPaymentId(String paymentId);
}