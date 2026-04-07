package com.ecommerce.backend.service.payment;

import com.ecommerce.backend.entity.Order;

public interface PaymentService {

    String createPayment(Order order);

    String getPaymentMethod(); // STRIPE / RAZORPAY
}