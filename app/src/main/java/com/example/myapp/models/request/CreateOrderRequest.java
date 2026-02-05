package com.example.myapp.models.request;

public class CreateOrderRequest {
    private Integer cartId;
    private String paymentMethod;
    private String billingAddress;

    public CreateOrderRequest(Integer cartId, String paymentMethod, String billingAddress) {
        this.cartId = cartId;
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
    }
}