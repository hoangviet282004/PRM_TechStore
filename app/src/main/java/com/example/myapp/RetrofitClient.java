package com.example.myapp;

import com.example.myapp.api.ApiService;
import com.example.myapp.models.request.RefreshRequest;
import com.example.myapp.models.response.RefreshResponse;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static ApiService apiService;

    // Separate plain client used only for token refresh — no interceptor, no authenticator
    private static Retrofit refreshRetrofit;
    private static ApiService refreshApiService;

    private static final String BASE_URL = "http://10.0.2.2:8080/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    // Proactively refresh before sending if the access token is already expired.
                    // This bypasses server-side 401 handling, which may return 500 on expired tokens.
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = SharedPrefsManager.getAccessToken();

                        if (token != null) {
                            if (isTokenExpired(token)) {
                                String refreshToken = SharedPrefsManager.getRefreshToken();
                                if (refreshToken != null) {
                                    try {
                                        retrofit2.Response<RefreshResponse> refreshRes =
                                                getRefreshApiService()
                                                        .refreshToken(new RefreshRequest(refreshToken))
                                                        .execute();
                                        if (refreshRes.isSuccessful() && refreshRes.body() != null) {
                                            token = refreshRes.body().getValue();
                                            SharedPrefsManager.saveTokens(token, refreshToken);
                                        } else {
                                            SharedPrefsManager.clearAll();
                                            return chain.proceed(original);
                                        }
                                    } catch (Exception e) {
                                        // Network error during proactive refresh — proceed without token
                                        return chain.proceed(original);
                                    }
                                } else {
                                    SharedPrefsManager.clearAll();
                                    return chain.proceed(original);
                                }
                            }
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(request);
                        }
                        return chain.proceed(original);
                    })
                    // Fallback: reactive refresh on unexpected 401 (token revoked, clock skew, etc.)
                    .authenticator((route, response) -> {
                        if (response.priorResponse() != null) return null;

                        String refreshToken = SharedPrefsManager.getRefreshToken();
                        if (refreshToken == null) return null;

                        retrofit2.Response<RefreshResponse> refreshRes = getRefreshApiService()
                                .refreshToken(new RefreshRequest(refreshToken)).execute();

                        if (refreshRes.isSuccessful() && refreshRes.body() != null) {
                            String newAccess = refreshRes.body().getValue();
                            SharedPrefsManager.saveTokens(newAccess, refreshToken);
                            return response.request().newBuilder()
                                    .header("Authorization", "Bearer " + newAccess)
                                    .build();
                        }

                        SharedPrefsManager.clearAll();
                        return null;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    private static ApiService getRefreshApiService() {
        if (refreshRetrofit == null) {
            OkHttpClient plainClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
            refreshRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(plainClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            refreshApiService = refreshRetrofit.create(ApiService.class);
        }
        return refreshApiService;
    }

    // Decode the JWT payload and compare the exp claim to the current time.
    // Returns true (treat as expired) if the token cannot be parsed.
    private static boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;
            byte[] decoded = android.util.Base64.decode(
                    parts[1], android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING);
            JSONObject payload = new JSONObject(new String(decoded));
            long exp = payload.getLong("exp");
            return System.currentTimeMillis() / 1000 >= exp;
        } catch (Exception e) {
            return true;
        }
    }
}
