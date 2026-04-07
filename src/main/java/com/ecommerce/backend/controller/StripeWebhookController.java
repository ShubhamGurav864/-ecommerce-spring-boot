package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payment/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {

        Event event;

        try {
            // ✅ Verify signature (SECURITY)
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("❌ Invalid signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("📩 Stripe event received: {}", event.getType());

        try {
            // ✅ Parse RAW payload (BEST APPROACH)
            JsonNode root = objectMapper.readTree(payload);

            JsonNode session = root.get("data").get("object");

            switch (event.getType()) {

                case "checkout.session.completed" -> {

                    String orderIdStr = session
                            .get("metadata")
                            .get("orderId")
                            .asText();

                    Long orderId = Long.parseLong(orderIdStr);

                    Order order = orderRepository.findById(orderId).orElse(null);

                    if (order == null) {
                        log.warn("⚠️ Order not found: {}", orderId);
                        return ResponseEntity.ok("Order not found");
                    }

                    order.setPaymentStatus(PaymentStatus.SUCCESS);
                    order.setStatus("CONFIRMED");
                    orderRepository.save(order);

                    log.info("✅ Order {} CONFIRMED", orderId);
                }

                case "checkout.session.expired" -> {

                    String orderIdStr = session
                            .get("metadata")
                            .get("orderId")
                            .asText();

                    Long orderId = Long.parseLong(orderIdStr);

                    Order order = orderRepository.findById(orderId).orElse(null);

                    if (order == null) {
                        log.warn("⚠️ Order not found: {}", orderId);
                        return ResponseEntity.ok("Order not found");
                    }

                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);

                    log.info("❌ Order {} CANCELLED", orderId);
                }

                default -> log.info("ℹ️ Ignored event: {}", event.getType());
            }

        } catch (Exception e) {
            log.error("❌ Webhook processing error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Webhook error");
        }

        return ResponseEntity.ok("Webhook processed");
    }
}