package com.ecommerce.backend.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentFactory {

    private final List<PaymentService> paymentServices;

    public PaymentService getPaymentService(String method) {
        return paymentServices.stream()
                .filter(service -> service.getPaymentMethod().equalsIgnoreCase(method))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid payment method"));
    }
}