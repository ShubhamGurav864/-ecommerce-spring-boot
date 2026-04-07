package com.ecommerce.backend.service.payment;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService implements PaymentService {

    private final OrderRepository orderRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Override
    public String createPayment(Order order) {
        try {
            Stripe.apiKey = stripeSecretKey;

            long amount = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            log.info("🔄 Creating Stripe session for Order #{} | Amount: {}", order.getId(), amount);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)

                    // ✅🔥 CRITICAL FIX
                    .putMetadata("orderId", String.valueOf(order.getId()))

                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(amount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + order.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            // Save for reference (optional now)
            order.setPaymentId(session.getId());
            order.setPaymentUrl(session.getUrl());
            orderRepository.save(order);

            log.info("✅ Stripe session created: {}", session.getId());

            return session.getUrl();

        } catch (Exception e) {
            log.error("❌ Stripe error: {}", e.getMessage());
            throw new RuntimeException("Stripe payment failed: " + e.getMessage());
        }
    }

    @Override
    public String getPaymentMethod() {
        return "STRIPE";
    }
}