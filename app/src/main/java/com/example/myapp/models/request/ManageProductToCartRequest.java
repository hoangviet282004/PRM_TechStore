package com.example.myapp.models.request;

public class ManageProductToCartRequest {
    private Integer productId;
    private Integer quantity;

    public ManageProductToCartRequest(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters/Setters
    public Integer getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
}
