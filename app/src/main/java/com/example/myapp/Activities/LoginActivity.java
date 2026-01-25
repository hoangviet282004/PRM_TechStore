package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.SharedPrefsManager;
import com.example.myapp.databinding.ActivityLoginBinding; // Nhớ sửa đúng package name của bạn

public class LoginActivity extends AppCompatActivity {

    // Khai báo binding để thay thế findViewById
    private ActivityLoginBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ĐẶT Ở ĐÂY: Khởi tạo khi màn hình bắt đầu chạy
        SharedPrefsManager.init(this);

        // Khởi tạo ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý sự kiện khi nhấn nút Đăng nhập
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
            } else {
                // Gọi hàm xử lý Call API ở đây
                performLogin(username, password);
            }
        });

        // Chuyển sang trang đăng ký (nếu bạn có tạo RegisterActivity)
        binding.tvRegister.setOnClickListener(v -> {
             Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
             startActivity(intent);
        });
    }


    private void performLogin(String user, String pass) {
        // Tạm thời hiển thị thông báo để test UI
        if (user.equals("admin") && pass.equals("123456")) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            // Chuyển sang MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu (Thử admin/123456)", Toast.LENGTH_SHORT).show();
        }

        // Sau này bạn sẽ dùng Retrofit để gọi API thật ở đây
    }
}