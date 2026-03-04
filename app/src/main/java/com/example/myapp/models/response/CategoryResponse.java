package com.example.myapp.models.response;

public class CategoryResponse {
    private Integer id;
    private String categoryName;

    public Integer getId() { return id; }
    public String getCategoryName() { return categoryName; }

    @Override
    public String toString() {
        return categoryName; // Hiển thị tên trong Spinner
    }
}