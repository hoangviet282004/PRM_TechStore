package com.example.myapp.Activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> {
            String user = binding.etRegUsername.getText().toString().trim();
            String pass = binding.etRegPassword.getText().toString().trim();
            String email = binding.etRegEmail.getText().toString().trim();
            String phone = binding.etRegPhone.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            } else {
                // Ở đây bạn sẽ call API Register
                handleRegister(user, pass, email, phone);
            }
        });

        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister(String user, String pass, String email, String phone) {
        // Sau này dùng Retrofit gửi data lên server
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        finish(); // Quay lại màn hình Login
    }
}