package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    // 🧾 Get checkout summary
    @GetMapping
    public ResponseEntity<CartResponse> checkout() {
        CartResponse response = checkoutService.checkout();
        return ResponseEntity.ok(response);
    }
}