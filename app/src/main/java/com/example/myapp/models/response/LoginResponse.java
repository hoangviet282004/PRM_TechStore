package com.example.myapp.models.response;

public class LoginResponse {
    private int statusCode;
    private SignInValue value; // Object chứa token trả về từ ResponseMapper

    public int getStatusCode() { return statusCode; }
    public SignInValue getValue() { return value; }

    public static class SignInValue {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}
