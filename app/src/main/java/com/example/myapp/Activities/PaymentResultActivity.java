package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.R;

public class PaymentResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        TextView txtStatus = findViewById(R.id.txtStatus);
        TextView txtOrderInfo = findViewById(R.id.txtOrderInfo);
        ImageView imgStatus = findViewById(R.id.imgStatus);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        // 1. Luôn cho phép quay về Home dù có dữ liệu link hay không
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 2. Xử lý dữ liệu từ Deep Link
        if (getIntent().getData() != null) {
            String status = getIntent().getData().getQueryParameter("status");
            String orderCode = getIntent().getData().getQueryParameter("orderCode");

            // Kiểm tra đúng giá trị status mà Backend gửi về (thường là PAID hoặc SUCCESS)
            if ("PAID".equalsIgnoreCase(status) || "Processing".equalsIgnoreCase(status)) {
                txtStatus.setText("Thanh Toán Thành Công!");
                imgStatus.setImageResource(R.drawable.ic_plus); // Nhớ đổi thành icon tick xanh

                // Hiển thị mã đơn hàng để khách yên tâm
                txtOrderInfo.setText("Mã đơn hàng của bạn: #" + (orderCode != null ? orderCode : "N/A") +
                        "\nCảm ơn bạn đã tin tưởng TechExpress!");
            } else {
                // Trường hợp thanh toán thất bại hoặc khách bấm Hủy
                txtStatus.setText("Thanh Toán Thất Bại");
                imgStatus.setImageResource(android.R.drawable.ic_dialog_alert);
                txtOrderInfo.setText("Giao dịch không thành công hoặc đã bị hủy.");
            }
        }
    }
}