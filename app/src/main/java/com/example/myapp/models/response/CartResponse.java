package com.example.myapp.models.response;
import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private Integer id;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;

    // THÊM PHƯƠNG THỨC NÀY VÀO
    public Integer getId() {
        return id;
    }

    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}