package com.example.myapp;

import com.example.myapp.api.ApiService;
import com.example.myapp.models.request.RefreshRequest;
import com.example.myapp.models.response.RefreshResponse;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            // FIX: Tăng timeout lên 60 giây để BE có đủ thời gian gọi sang PayOS
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    // Gắn Token vào mọi request gửi đi
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = SharedPrefsManager.getAccessToken();
                        if (token != null) {
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(request);
                        }
                        return chain.proceed(original);
                    })
                    // Tự động Refresh khi nhận lỗi 401
                    .authenticator((route, response) -> {
                        String refreshToken = SharedPrefsManager.getRefreshToken();
                        if (refreshToken == null) return null;

                        retrofit2.Response<RefreshResponse> refreshRes = getApiService()
                                .refreshToken(new RefreshRequest(refreshToken)).execute();

                        if (refreshRes.isSuccessful() && refreshRes.body() != null) {
                            String newAccess = refreshRes.body().getValue();
                            SharedPrefsManager.saveTokens(newAccess, refreshToken);

                            return response.request().newBuilder()
                                    .header("Authorization", "Bearer " + newAccess)
                                    .build();
                        }
                        return null;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}