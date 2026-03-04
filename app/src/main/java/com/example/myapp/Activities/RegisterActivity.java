package com.example.myapp.Activities;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.RetrofitClient;
import com.example.myapp.databinding.ActivityRegisterBinding;
import com.example.myapp.models.request.SignUpRequest;
import com.example.myapp.models.response.SignUpResponse;
import org.json.JSONObject;
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

        // Hết lỗi: btnRegister đã được định nghĩa trong XML ở trên
        binding.btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                performRegister();
            }
        });

        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private boolean validateForm() {
        boolean isValid = true;

        // 1. Reset lỗi cũ
        binding.tilRegUsername.setError(null);
        binding.tilRegPassword.setError(null);
        binding.tilRegConfirmPassword.setError(null);
        binding.tilRegEmail.setError(null);

        String user = binding.etRegUsername.getText().toString().trim();
        String pass = binding.etRegPassword.getText().toString();
        String confirm = binding.etRegConfirmPassword.getText().toString();
        String email = binding.etRegEmail.getText().toString().trim();

        // 2. Kiểm tra Username
        if (user.isEmpty()) {
            binding.tilRegUsername.setError("Bắt buộc nhập tên đăng nhập *");
            isValid = false;
        }

        // 3. Kiểm tra Password
        if (pass.isEmpty()) {
            binding.tilRegPassword.setError("Vui lòng nhập mật khẩu *");
            isValid = false;
        } else if (pass.length() < 6) {
            binding.tilRegPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            isValid = false;
        }

        // 4. Kiểm tra Confirm Password
        if (!confirm.equals(pass)) {
            binding.tilRegConfirmPassword.setError("Mật khẩu xác nhận không khớp *");
            isValid = false;
        }

        // 5. Kiểm tra Email
        if (email.isEmpty()) {
            binding.tilRegEmail.setError("Vui lòng nhập Email *");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegEmail.setError("Email không đúng định dạng *");
            isValid = false;
        }

        return isValid;
    }

    private void performRegister() {
        SignUpRequest request = new SignUpRequest(
                binding.etRegUsername.getText().toString().trim(),
                binding.etRegPassword.getText().toString(),
                binding.etRegConfirmPassword.getText().toString(),
                binding.etRegEmail.getText().toString().trim(),
                binding.etRegPhone.getText().toString().trim(),
                binding.etRegAddress.getText().toString().trim()
        );

        RetrofitClient.getApiService().signUp(request).enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        // Show lỗi chi tiết từ Server
                        String errorJson = response.errorBody().string();
                        JSONObject errorObj = new JSONObject(errorJson);
                        String message = errorObj.optString("message", "Đăng ký thất bại");

                        if (message.toLowerCase().contains("username")) {
                            binding.tilRegUsername.setError(message);
                        } else if (message.toLowerCase().contains("email")) {
                            binding.tilRegEmail.setError(message);
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Lỗi không xác định", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}