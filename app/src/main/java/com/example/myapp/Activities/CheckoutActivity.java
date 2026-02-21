package com.example.myapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.RetrofitClient;
import com.example.myapp.databinding.ActivityCheckoutBinding;
import com.example.myapp.models.request.CreateOrderRequest;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.OrderResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    private ActivityCheckoutBinding binding;
    private int cartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartId = getIntent().getIntExtra("CART_ID", -1);
        Log.d("CHECKOUT_DEBUG", "Bắt đầu thanh toán PayOS cho Cart ID: " + cartId);

        binding.btnPay.setOnClickListener(v -> createOrder());

        // Nhận kết quả từ Deep Link PayOS
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    // Thêm hàm này vào trong class CheckoutActivity
    private void showSuccessDialog() {
        // Tạo Dialog không viền
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(com.example.myapp.R.layout.dialog_payment_success);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false); // Không cho tắt khi chưa bấm nút

        // Ánh xạ nút bấm trong Dialog
        View btnHome = dialog.findViewById(com.example.myapp.R.id.btnGoHome);
        btnHome.setOnClickListener(v -> {
            dialog.dismiss();
            // Quay về MainActivity và xóa sạch các Activity cũ
            Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    // Cập nhật lại hàm handleIntent cũ của ông
    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "techexpress".equals(data.getScheme())) {
            String status = data.getQueryParameter("status");
            if ("PAID".equalsIgnoreCase(status)) {
                // GỌI DIALOG ĐẸP Ở ĐÂY
                showSuccessDialog();
            } else {
                Toast.makeText(this, "Thanh toán chưa hoàn tất!", Toast.LENGTH_SHORT).show();
                binding.btnPay.setEnabled(true);
            }
        }
    }

    private void createOrder() {
        String address = binding.etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPay.setEnabled(false);
        Log.d("CHECKOUT_DEBUG", "Đang tạo Order PayOS trên Server...");

        // QUAN TRỌNG: Gửi "PayOs" thay vì "VnPay"
        CreateOrderRequest req = new CreateOrderRequest(cartId, "PayOs", address);
        RetrofitClient.getApiService().createOrder(req).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderResponse>> call, Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int orderId = response.body().getData().getId();
                    Log.d("CHECKOUT_DEBUG", "Tạo đơn thành công, đang lấy link thanh toán...");
                    getPayOsPaymentUrl(orderId);
                } else {
                    binding.btnPay.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, "Server từ chối yêu cầu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderResponse>> call, Throwable t) {
                binding.btnPay.setEnabled(true);
                Log.e("CHECKOUT_ERROR", "Lỗi tạo đơn: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPayOsPaymentUrl(int orderId) {
        RetrofitClient.getApiService().getPaymentUrl(orderId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                binding.btnPay.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getData();
                    Log.d("CHECKOUT_DEBUG", "Mở trình duyệt: " + url);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    Toast.makeText(CheckoutActivity.this, "Không thể lấy link thanh toán!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                binding.btnPay.setEnabled(true);
                Log.e("CHECKOUT_ERROR", "Lỗi lấy link: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Quá thời gian phản hồi (Timeout)!", Toast.LENGTH_LONG).show();
            }
        });
    }
}