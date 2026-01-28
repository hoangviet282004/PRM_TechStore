package com.example.myapp.Activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.RetrofitClient;
import com.example.myapp.databinding.ActivityRegisterBinding;
import com.example.myapp.models.request.SignUpRequest;
import com.example.myapp.models.response.SignUpResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> {
            // Lấy dữ liệu từ các ID bạn đã đặt trong XML
            String user = binding.etRegUsername.getText().toString().trim();
            String pass = binding.etRegPassword.getText().toString().trim();
            String confirmPass = binding.etRegConfirmPassword.getText().toString().trim();
            String email = binding.etRegEmail.getText().toString().trim();
            String phone = binding.etRegPhone.getText().toString().trim();
            String address = binding.etRegAddress.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty() || email.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            } else {
                handleRegister(user, pass, confirmPass, email, phone, address);
            }
        });

        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister(String user, String pass, String confirm, String email, String phone, String addr) {
        // Tạo request object theo đúng tài liệu API của bạn
        SignUpRequest request = new SignUpRequest(user, pass, confirm, email, phone, addr);

        // Gọi API thực tế
        RetrofitClient.getApiService().signUp(request).enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                // Kiểm tra status code 201 như trong hình image_f827f1.png bạn gửi
                if (response.isSuccessful() && response.body() != null && response.body().getStatusCode() == 201) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại Login
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại, hãy kiểm tra lại thông tin!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}