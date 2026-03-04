package com.example.myapp.models.response;

public class BrandResponse {
    private Integer id;
    private String brandName;
    private String brandImageUrl; // Nếu backend có trả về ảnh

    public Integer getId() { return id; }
    public String getBrandName() { return brandName; }

    @Override
    public String toString() {
        return brandName; // Để Spinner hiển thị đúng tên
    }
}