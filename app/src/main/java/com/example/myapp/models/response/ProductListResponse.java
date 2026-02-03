package com.example.myapp.models.response;

import com.google.gson.annotations.SerializedName;

public class ProductListResponse {
    // ID là trường quan trọng nhất để mở trang Chi tiết
    private Integer id;

    private String productName;
    private String primaryImageUrl;
    private Double price;
    private String briefDescription;

    // --- BỔ SUNG HÀM NÀY ĐỂ HẾT BÁO ĐỎ ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    // ------------------------------------

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }
}