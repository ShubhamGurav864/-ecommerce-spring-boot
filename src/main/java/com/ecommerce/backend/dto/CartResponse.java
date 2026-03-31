package com.ecommerce.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {

    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}