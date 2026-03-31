package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ==============================
    // 🛒 ADD TO CART
    // ==============================
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ) {
        cartService.addToCart(productId, quantity);
        return ResponseEntity.ok("Product added to cart successfully");
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }
    
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ) {
        cartService.removeFromCart(productId, quantity);
        return ResponseEntity.ok("Item updated/removed successfully");
    }
}