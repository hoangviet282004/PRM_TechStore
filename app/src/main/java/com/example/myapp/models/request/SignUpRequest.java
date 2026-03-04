package com.example.myapp.models.request;


public class SignUpRequest {
    private String username, password, confirmPassword, email, fullName, phoneNumber, address;

    public SignUpRequest(String username, String password, String confirmPassword, String email, String fullName, String phoneNumber, String address) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
