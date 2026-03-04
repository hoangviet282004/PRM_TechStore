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
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // Vietnamese phone: 10 digits starting with 03x, 05x, 07x, 08x, 09x
    private static final Pattern VN_PHONE = Pattern.compile("^(0[35789])[0-9]{8}$");

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupFocusListeners();

        binding.btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                performRegister();
            }
        });

        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void setupFocusListeners() {
        // Username: required, show error on focus-out if empty
        binding.etRegUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (binding.etRegUsername.getText().toString().trim().isEmpty()) {
                    binding.tilRegUsername.setError("Bắt buộc nhập tên đăng nhập");
                } else {
                    binding.tilRegUsername.setError(null);
                }
            } else {
                binding.tilRegUsername.setError(null);
            }
        });

        // Password: required, min 6 chars
        binding.etRegPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String pass = binding.etRegPassword.getText().toString();
                if (pass.isEmpty()) {
                    binding.tilRegPassword.setError("Vui lòng nhập mật khẩu");
                } else if (pass.length() < 6) {
                    binding.tilRegPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
                } else {
                    binding.tilRegPassword.setError(null);
                }
            } else {
                binding.tilRegPassword.setError(null);
            }
        });

        // Confirm password: must match
        binding.etRegConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String confirm = binding.etRegConfirmPassword.getText().toString();
                String pass = binding.etRegPassword.getText().toString();
                if (confirm.isEmpty()) {
                    binding.tilRegConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
                } else if (!confirm.equals(pass)) {
                    binding.tilRegConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                } else {
                    binding.tilRegConfirmPassword.setError(null);
                }
            } else {
                binding.tilRegConfirmPassword.setError(null);
            }
        });

        // Email: required, valid format
        binding.etRegEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = binding.etRegEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    binding.tilRegEmail.setError("Vui lòng nhập Email");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tilRegEmail.setError("Email không đúng định dạng");
                } else {
                    binding.tilRegEmail.setError(null);
                }
            } else {
                binding.tilRegEmail.setError(null);
            }
        });

        // Phone: optional, but must be valid Vietnamese number if entered
        binding.etRegPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String phone = binding.etRegPhone.getText().toString().trim();
                if (!phone.isEmpty() && !VN_PHONE.matcher(phone).matches()) {
                    binding.tilRegPhone.setError("Số điện thoại không hợp lệ (VD: 0912345678)");
                } else {
                    binding.tilRegPhone.setError(null);
                }
            } else {
                binding.tilRegPhone.setError(null);
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Reset errors
        binding.tilRegUsername.setError(null);
        binding.tilRegPassword.setError(null);
        binding.tilRegConfirmPassword.setError(null);
        binding.tilRegEmail.setError(null);
        binding.tilRegPhone.setError(null);

        String user = binding.etRegUsername.getText().toString().trim();
        String pass = binding.etRegPassword.getText().toString();
        String confirm = binding.etRegConfirmPassword.getText().toString();
        String email = binding.etRegEmail.getText().toString().trim();
        String phone = binding.etRegPhone.getText().toString().trim();

        if (user.isEmpty()) {
            binding.tilRegUsername.setError("Bắt buộc nhập tên đăng nhập");
            isValid = false;
        }

        if (pass.isEmpty()) {
            binding.tilRegPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (pass.length() < 6) {
            binding.tilRegPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            isValid = false;
        }

        if (confirm.isEmpty()) {
            binding.tilRegConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!confirm.equals(pass)) {
            binding.tilRegConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.tilRegEmail.setError("Vui lòng nhập Email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegEmail.setError("Email không đúng định dạng");
            isValid = false;
        }

        if (!phone.isEmpty() && !VN_PHONE.matcher(phone).matches()) {
            binding.tilRegPhone.setError("Số điện thoại không hợp lệ (VD: 0912345678)");
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
                binding.etRegFullName.getText().toString().trim(),
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
