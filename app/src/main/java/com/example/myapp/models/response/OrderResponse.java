package com.example.myapp.models.response;

public class OrderResponse {
    private Integer id;
    private Integer cartId;
    private String paymentMethod;
    private String billingAddress;
    private String orderStatus;
    private String orderDate; // BE trả về Instant

    public Integer getId() { return id; }
    public String getOrderStatus() { return orderStatus; }
}