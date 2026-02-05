package com.example.myapp.models.response;

import java.math.BigDecimal;

public class CartItemResponse {
    private Integer id;
    private Integer cartId;
    private String productName;
    private String productImage; // KHỚP VỚI BE
    private int quantity;
    private BigDecimal price;    // KHỚP VỚI BE

    // Getters
    public String getProductImage() { return productImage; }
    public BigDecimal getPrice() { return price; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Integer getId() { return id; }
    public Integer getProductId() { return id; } // Giả định ID item là ID sản phẩm
}