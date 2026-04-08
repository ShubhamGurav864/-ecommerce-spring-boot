package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.VerifyPaymentRequest;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.enums.PaymentMethod;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.service.payment.PaymentFactory;
import com.ecommerce.backend.service.payment.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;
    private final PaymentFactory paymentFactory;

    @Value("${razorpay.key.id}")
    private String razorpayKey;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    // 💳 Create Payment (Stripe / Razorpay)
    @PostMapping("/create/{orderId}")
    public ResponseEntity<?> createPayment(
            @PathVariable Long orderId,
            @RequestParam PaymentMethod method
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ✅ Save selected method
        order.setPaymentMethod(method);
        orderRepository.save(order);

        // ✅ Strategy Pattern
        PaymentService paymentService =
                paymentFactory.getPaymentService(method.name());

        String response = paymentService.createPayment(order);

        // 🔥 Razorpay → return orderId + key
        if (method == PaymentMethod.RAZORPAY) {
            return ResponseEntity.ok(
                    Map.of(
                            "orderId", response,
                            "key", razorpayKey
                    )
            );
        }

        // 🔥 Stripe → return redirect URL
        return ResponseEntity.ok(response);
    }

    // ✅ Verify Razorpay Payment
    @PostMapping("/verify/razorpay")
    public ResponseEntity<String> verifyRazorpayPayment(
            @RequestBody VerifyPaymentRequest request
    ) {

        try {
            boolean isValid = verifySignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            if (!isValid) {
                return ResponseEntity.badRequest().body("Invalid payment signature");
            }

            // ✅ Find order
            Order order = orderRepository
                    .findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // ✅ Update DB
            order.setPaymentId(request.getRazorpayPaymentId());
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setStatus("CONFIRMED");

            orderRepository.save(order);

            return ResponseEntity.ok("Payment verified successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Verification failed: " + e.getMessage());
        }
    }

    // 🔐 Signature Verification (CRITICAL)
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpaySecret.getBytes(),
                    "HmacSHA256"
            );

            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes());

            String generatedSignature = new String(Hex.encodeHex(hash));

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed");
        }
    }
}