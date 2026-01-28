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

        // Khởi tạo bộ nhớ bảo mật trước khi dùng
        SharedPrefsManager.init(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        // Bước CALL API thực tế
        RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Kiểm tra mã thành công 200 từ Backend
                if (response.isSuccessful() && response.body() != null && response.body().getStatusCode() == 200) {

                    // Lấy Token từ object 'value'
                    String accessToken = response.body().getValue().getAccessToken();
                    String refreshToken = response.body().getValue().getRefreshToken();


                    // Cất Token vào "két sắt" EncryptedSharedPreferences
                    SharedPrefsManager.saveTokens(accessToken, refreshToken);

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Vào màn hình chính
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Xử lý khi Backend ném ra UnauthorizedAccessException hoặc BadRequestException
                    Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Lỗi này thường do IP 10.0.2.2 hoặc Port 8080 chưa chuẩn
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(LoginActivity.this, "Không thể kết nối Server!", Toast.LENGTH_LONG).show();
            }
        });
    }
}