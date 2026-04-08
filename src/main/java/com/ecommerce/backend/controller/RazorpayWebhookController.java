package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@RestController
@RequestMapping("/api/payment/webhook")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final OrderRepository orderRepository;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {

        try {
            // ✅ Step 1: Verify signature
            boolean isValid = verifyWebhookSignature(payload, signature);

            if (!isValid) {
                log.error("❌ Invalid Razorpay webhook signature");
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            log.info("📩 Razorpay webhook received");

            // ✅ Step 2: Check event type manually
            if (payload.contains("\"event\":\"payment.captured\"")) {

                String razorpayOrderId = extractValue(payload, "order_id");

                log.info("✅ Payment captured for order: {}", razorpayOrderId);

                Order order = orderRepository
                        .findByRazorpayOrderId(razorpayOrderId)
                        .orElse(null);

                if (order == null) {
                    log.warn("⚠️ Order not found for razorpayOrderId: {}", razorpayOrderId);
                    return ResponseEntity.ok("Order not found");
                }

                order.setPaymentStatus(PaymentStatus.SUCCESS);
                order.setStatus("CONFIRMED");

                orderRepository.save(order);

                log.info("✅ Order {} marked CONFIRMED", order.getId());
            }

            if (payload.contains("\"event\":\"payment.failed\"")) {

                String razorpayOrderId = extractValue(payload, "order_id");

                log.info("❌ Payment failed for order: {}", razorpayOrderId);

                Order order = orderRepository
                        .findByRazorpayOrderId(razorpayOrderId)
                        .orElse(null);

                if (order == null) {
                    log.warn("⚠️ Order not found for razorpayOrderId: {}", razorpayOrderId);
                    return ResponseEntity.ok("Order not found");
                }

                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setStatus("CANCELLED");

                orderRepository.save(order);

                log.info("❌ Order {} marked CANCELLED", order.getId());
            }

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            log.error("❌ Razorpay webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook error");
        }
    }

    // 🔐 Signature verification
    private boolean verifyWebhookSignature(String payload, String actualSignature) {
        try {
            String expectedSignature = hmacSHA256(payload, webhookSecret);
            return expectedSignature.equals(actualSignature);
        } catch (Exception e) {
            return false;
        }
    }

    private String hmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));

        byte[] rawHmac = mac.doFinal(data.getBytes());

        StringBuilder hex = new StringBuilder();
        for (byte b : rawHmac) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    // 🧠 Simple JSON extraction (no dependency)
    private String extractValue(String payload, String key) {
        String search = "\"" + key + "\":\"";
        int start = payload.indexOf(search) + search.length();
        int end = payload.indexOf("\"", start);
        return payload.substring(start, end);
    }
}