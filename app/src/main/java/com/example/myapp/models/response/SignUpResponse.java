package com.example.myapp.models.response;

public class SignUpResponse {
    private int statusCode;
    private UserValue value;

    public int getStatusCode() { return statusCode; }
    public UserValue getValue() { return value; }

    public static class UserValue {
        private int id;
        private String username;
        private String email;
        private String phoneNumber;
        private String address;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getAddress() { return address; }

    }
}
