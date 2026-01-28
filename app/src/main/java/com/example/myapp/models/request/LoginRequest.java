package com.example.myapp.models.request;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String login; // Khớp với tham số String login trong AuthService
    private String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
