package com.example.myapp.models.response;

public class LoginResponse {
    private int statusCode;
    private SignInValue value; // Object chứa token trả về từ ResponseMapper

    public int getStatusCode() { return statusCode; }
    public SignInValue getValue() { return value; }

    public static class SignInValue {
        private String accessToken;
        private String refreshToken;

        private String role; // THÊM DÒNG NÀY ĐỂ NHẬN ROLE TỪ BACKEND

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }

        // THÊM GETTER ĐỂ LOGINACTIVITY CÓ THỂ LẤY ĐƯỢC ROLE
        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
