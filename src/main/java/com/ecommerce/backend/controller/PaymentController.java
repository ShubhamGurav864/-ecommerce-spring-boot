package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.service.payment.PaymentFactory;
import com.ecommerce.backend.service.payment.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;
    private final PaymentFactory paymentFactory;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<String> createPayment(
            @PathVariable Long orderId,
            @RequestParam PaymentMethod method
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ✅ Save selected payment method
        order.setPaymentMethod(method);
        orderRepository.save(order);

        PaymentService paymentService =
                paymentFactory.getPaymentService(method.name());

        String paymentUrl = paymentService.createPayment(order);

        return ResponseEntity.ok(paymentUrl);
    }
}