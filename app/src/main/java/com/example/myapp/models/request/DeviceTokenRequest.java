package com.example.myapp.models.request;

public class DeviceTokenRequest {
    private String token;

    public DeviceTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}
