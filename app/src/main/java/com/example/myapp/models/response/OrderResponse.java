package com.example.myapp.models.response;

public class OrderResponse {
    private Integer id;
    private Integer cartId;
    private String paymentMethod;
    private String billingAddress;
    private String orderStatus;
    private String orderDate; // BE trả về Instant

    public Integer getId() { return id; }
    public Integer getCartId() { return cartId; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getBillingAddress() { return billingAddress; }
    public String getOrderStatus() { return orderStatus; }
    public String getOrderDate() { return orderDate; }
}