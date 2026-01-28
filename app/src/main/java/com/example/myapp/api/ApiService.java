package com.example.myapp.api;

import com.example.myapp.models.request.LoginRequest;
import com.example.myapp.models.request.RefreshRequest;
import com.example.myapp.models.request.SignUpRequest;
import com.example.myapp.models.response.LoginResponse;
import com.example.myapp.models.response.RefreshResponse;
import com.example.myapp.models.response.SignUpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

    // Theo image_f82f1e.png, đường dẫn là api/auth/sign-in
    @POST("api/auth/sign-in")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/refresh")
    Call<RefreshResponse> refreshToken(@Body RefreshRequest request);
}
