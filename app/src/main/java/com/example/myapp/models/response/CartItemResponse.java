package com.example.myapp.models.response;

import java.math.BigDecimal;

public class CartItemResponse {
    private Integer id;
    private Integer cartId;
    private Integer productId;
    private String productName;
    private String productImage;
    private int quantity;
    private BigDecimal price;

    // Getters
    public Integer getId() { return id; }
    public Integer getCartId() { return cartId; }
    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductImage() { return productImage; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
}