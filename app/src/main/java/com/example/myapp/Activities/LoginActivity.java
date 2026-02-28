package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.databinding.ActivityLoginBinding;
import com.example.myapp.models.request.LoginRequest;
import com.example.myapp.models.response.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPrefsManager.init(this);

        // Kiểm tra tự động đăng nhập
        if (SharedPrefsManager.getAccessToken() != null) {
            Log.d("DEBUG_ROLE", "Role đang lưu trong máy: " + SharedPrefsManager.getUserRole());
            navigateToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String loginInput = binding.etUsername.getText().toString().trim();
            String passwordInput = binding.etPassword.getText().toString().trim();
            if (loginInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(loginInput, passwordInput);
            }
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performLogin(String loginInput, String passwordInput) {
        LoginRequest request = new LoginRequest(loginInput, passwordInput);
        RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatusCode() == 200) {

                    String accessToken = response.body().getValue().getAccessToken();
                    String refreshToken = response.body().getValue().getRefreshToken();

                    // 1. TỰ GIẢI MÃ ROLE TỪ TOKEN
                    String role = getRoleFromJWT(accessToken);
                    Log.d("LOGIN_DEBUG", "Role giải mã được: " + role);

                    // 2. Lưu vào máy
                    SharedPrefsManager.saveTokens(accessToken, refreshToken);
                    SharedPrefsManager.saveUserRole(role);

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private String getRoleFromJWT(String token) {
        try {
            // JWT có cấu trúc: Header.Payload.Signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "Customer";

            // Giải mã phần Payload (Base64)
            byte[] decodedBytes = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE);
            String payload = new String(decodedBytes, "UTF-8");

            // Chuyển chuỗi Payload sang JSON để lấy trường "role"
            org.json.JSONObject jsonObject = new org.json.JSONObject(payload);
            return jsonObject.optString("role", "Customer");
        } catch (Exception e) {
            Log.e("JWT_DECODE", "Lỗi giải mã token: " + e.getMessage());
            return "Customer"; // Mặc định nếu lỗi
        }
    }
}