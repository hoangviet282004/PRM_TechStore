package com.example.myapp.models.response;

public class RefreshResponse {
    private int statusCode;
    private String value; // Đây là Access Token mới dạng chuỗi

    public int getStatusCode() { return statusCode; }
    public String getValue() { return value; }
}
