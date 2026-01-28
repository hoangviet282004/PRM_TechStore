package com.example.myapp.api;

import com.example.myapp.models.SignUpRequest;
import com.example.myapp.models.SignUpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

}
