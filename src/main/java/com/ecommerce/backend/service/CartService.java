package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ==============================
    // 🛒 ADD TO CART
    // ==============================
    public void addToCart(Long productId, Integer quantity) {

        // 1️⃣ Get logged-in user from JWT
        User user = getLoggedInUser();

        // 2️⃣ Get or create cart
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));

        // 3️⃣ Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 4️⃣ Check if product already exists
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {
            // ✅ Increase quantity
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // 🆕 Create new item
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build();

            cart.getItems().add(cartItem);
        }

        // 5️⃣ Save
        cartItemRepository.save(cartItem);
    }
    // GET CART
    // ==============================
    public CartResponse getCart() {

        // 1️⃣ Get logged-in user
        User user = getLoggedInUser();

        // 2️⃣ Get cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 3️⃣ Convert items to DTO
        List<CartItemResponse> items = cart.getItems().stream().map(item -> {

            BigDecimal totalPrice = item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            return CartItemResponse.builder()
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalPrice(totalPrice)
                    .build();

        }).toList();

        // 4️⃣ Calculate total cart amount
        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5️⃣ Return response
        return CartResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
   
    // ❌ REMOVE / DECREASE ITEM
    // ==============================
    public void removeFromCart(Long productId, Integer quantity) {

        // 1️⃣ Get logged-in user
        User user = getLoggedInUser();

        // 2️⃣ Get cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 3️⃣ Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 4️⃣ Find cart item
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Item not in cart"));

        // 5️⃣ Logic
        if (quantity >= cartItem.getQuantity()) {
            // ❌ Remove completely
            cart.getItems().remove(cartItem); // maintain relation
            cartItemRepository.delete(cartItem);
        } else {
            // ➖ Decrease quantity
            cartItem.setQuantity(cartItem.getQuantity() - quantity);
            cartItemRepository.save(cartItem);
        }
    }

    // ==============================
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}