package com.example.myapp.models.response;

import java.math.BigDecimal;

public class PaymentResponse {
    private Integer id;
    private Integer orderId;
    private BigDecimal amount;
    private String paymentDate;
    private String paymentStatus;

    public Integer getId() { return id; }
    public Integer getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentStatus() { return paymentStatus; }
}
