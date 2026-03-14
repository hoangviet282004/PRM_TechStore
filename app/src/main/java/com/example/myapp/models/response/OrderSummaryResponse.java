package com.example.myapp.models.response;

import java.util.List;

public class OrderSummaryResponse {
    private OrderResponse order;
    private List<CartItemResponse> cartItems;
    private PaymentResponse payment;

    public OrderResponse getOrder() { return order; }
    public List<CartItemResponse> getCartItems() { return cartItems; }
    public PaymentResponse getPayment() { return payment; }
}
