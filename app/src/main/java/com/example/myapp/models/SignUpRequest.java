package com.example.myapp.models;


public class SignUpRequest {
    private String username, password, confirmPassword, email, phoneNumber, address;

    public SignUpRequest(String username, String password, String confirmPassword, String email, String phoneNumber, String address) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
