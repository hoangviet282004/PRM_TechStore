package com.example.myapp.models.request;

public class AdjustProductQuantityInCartRequest {

    private int cartItemId;

    private int quantity;

    public AdjustProductQuantityInCartRequest(int cartItemId, int quantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
