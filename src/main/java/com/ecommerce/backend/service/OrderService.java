package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    // 📦 Place Order (Transactional - very important)
    @Transactional
    public Order placeOrder() {

        // 1️⃣ Get logged-in user
        User user = getLoggedInUser();

        // 2️⃣ Get cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 3️⃣ Validate cart
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 4️⃣ Create Order
        Order order = Order.builder()
                .user(user)
                .status("PLACED")
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // 5️⃣ Convert CartItems → OrderItems
        for (CartItem cartItem : cart.getItems()) {

            BigDecimal itemTotal = cartItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

             OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .productName(cartItem.getProduct().getName()) // ✅ REQUIRED
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .totalPrice(
                        cartItem.getPrice().multiply(
                                BigDecimal.valueOf(cartItem.getQuantity())
                        )
                ) // ✅ Good practice
                .build();

            order.getItems().add(orderItem);

            total = total.add(itemTotal);
        }

        // 6️⃣ Set total amount
        order.setTotalAmount(total);

        // 7️⃣ Save order
        Order savedOrder = orderRepository.save(order);

        // 8️⃣ Clear cart (important)
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    // 📜 Get all orders of logged-in user
    public List<Order> getUserOrders() {
        return orderRepository.findByUser(getLoggedInUser());
    }

    // 🔍 Get single order by ID
    public Order getOrderById(Long orderId) {

        User user = getLoggedInUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🔐 Security check
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        return order;
    }

    // 🔐 Common method to fetch logged-in user
    private User getLoggedInUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}