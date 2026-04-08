package com.ecommerce.backend.service.payment;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.repository.OrderRepository;
import com.razorpay.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayService implements PaymentService {

    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Override
    public String createPayment(Order order) {

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            // 🔥 Razorpay uses paise
            int amount = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();

            JSONObject options = new JSONObject();
            options.put("amount", amount);
            options.put("currency", "INR");
            options.put("receipt", "order_" + order.getId());

            // 🔥 CREATE ORDER IN RAZORPAY
            com.razorpay.Order razorpayOrder = client.orders.create(options);

            String razorpayOrderId = razorpayOrder.get("id");

            log.info("✅ Razorpay order created: {}", razorpayOrderId);

            // 💾 Save in DB
            order.setRazorpayOrderId(razorpayOrderId);
            orderRepository.save(order);

            // 🔥 RETURN orderId (NOT URL like Stripe)
            return razorpayOrderId;

        } catch (Exception e) {
            log.error("❌ Razorpay error: {}", e.getMessage());
            throw new RuntimeException("Razorpay failed: " + e.getMessage());
        }
    }

    @Override
    public String getPaymentMethod() {
        return "RAZORPAY";
    }
}