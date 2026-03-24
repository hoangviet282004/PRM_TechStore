package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.myapp.Services.TechExpressMessagingService;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.databinding.ActivityLoginBinding;
import com.example.myapp.models.request.LoginRequest;
import com.example.myapp.models.response.LoginResponse;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Snackbar.make(binding.getRoot(), "Đăng ký thành công! Vui lòng đăng nhập.", Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String user = binding.etUsername.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Snackbar.make(binding.getRoot(), "Vui lòng nhập đủ thông tin", Snackbar.LENGTH_SHORT).show();
            } else {
                performLogin(user, pass);
            }
        });

        binding.tvRegister.setOnClickListener(v ->
                registerLauncher.launch(new Intent(this, RegisterActivity.class)));
    }

    private void performLogin(String user, String pass) {
        RetrofitClient.getApiService().login(new LoginRequest(user, pass)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatusCode() == 200) {
                    String access = response.body().getValue().getAccessToken();
                    String refresh = response.body().getValue().getRefreshToken();
                    String username = getUsernameFromJWT(access); // Thêm hàm này

                    // BƯỚC QUAN TRỌNG: Giải mã Role từ chuỗi Token
                    String role = getRoleFromJWT(access);
                    Log.d("LOGIN_DEBUG", "Đã bóc tách được Role: " + role);

                    SharedPrefsManager.saveTokens(access, refresh);
                    SharedPrefsManager.saveUserRole(role);

                    SharedPrefsManager.saveUsername(username);
                    // Register FCM token with backend
                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(token -> {
                                if (token != null)
                                    TechExpressMessagingService.registerToken(token);
                            });
                    Intent result = new Intent();
                    result.putExtra("username", username);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Snackbar.make(binding.getRoot(), "Sai tài khoản hoặc mật khẩu", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }


    // Thêm hàm bóc tách Username
    private String getUsernameFromJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] decodedBytes = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE);
            org.json.JSONObject jsonObject = new org.json.JSONObject(new String(decodedBytes, "UTF-8"));
            return jsonObject.optString("username", "");
        } catch (Exception e) { return ""; }
    }

    // Hàm bóc tách Role từ JWT Payload
    private String getRoleFromJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "Customer";
            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE);
            String payload = new String(decodedBytes, "UTF-8");
            JSONObject jsonObject = new JSONObject(payload);
            return jsonObject.optString("role", "Customer");
        } catch (Exception e) {
            return "Customer";
        }
    }

}