package com.example.myapp.models.request;

public class CreateOrderRequest {
    private Integer cartId;
    private String paymentMethod;
    private String billingFullName;
    private String billingPhone;
    private String billingAddress;

    public CreateOrderRequest(Integer cartId, String paymentMethod, String billingFullName, String billingPhone, String billingAddress) {
        this.cartId = cartId;
        this.paymentMethod = paymentMethod;
        this.billingFullName = billingFullName;
        this.billingPhone = billingPhone;
        this.billingAddress = billingAddress;
    }
}