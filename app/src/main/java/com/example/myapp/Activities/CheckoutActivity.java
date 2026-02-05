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

        // Nhận Cart ID từ trang Giỏ hàng
        cartId = getIntent().getIntExtra("CART_ID", -1);
        Log.d("CHECKOUT_DEBUG", "Bắt đầu thanh toán cho Cart ID: " + cartId);

        binding.btnPay.setOnClickListener(v -> createOrder());

        // Xử lý nếu App được mở từ Deep Link VNPay khi đang chạy nền
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.getScheme().equals("techexpress")) {
            // VNPay trả về link dạng: techexpress://vnpay_return?vnp_ResponseCode=00...
            String responseCode = data.getQueryParameter("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                Toast.makeText(this, "Thanh toán thành công! Đơn hàng đang được xử lý.", Toast.LENGTH_LONG).show();
                // Chuyển về trang chủ hoặc trang lịch sử đơn hàng
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Thanh toán thất bại hoặc đã hủy!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void createOrder() {
        String address = binding.etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnPay.setEnabled(false);
        Log.d("CHECKOUT_DEBUG", "Đang tạo Order trên Server...");

        CreateOrderRequest req = new CreateOrderRequest(cartId, "VnPay", address);
        RetrofitClient.getApiService().createOrder(req).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderResponse>> call, Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int orderId = response.body().getData().getId();
                    getVnPayUrl(orderId);
                } else {
                    binding.btnPay.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, "Lỗi tạo đơn hàng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderResponse>> call, Throwable t) {
                binding.btnPay.setEnabled(true);
                Log.e("CHECKOUT_ERROR", "Tạo đơn thất bại: " + t.getMessage());
            }
        });
    }

    private void getVnPayUrl(int orderId) {
        RetrofitClient.getApiService().getPaymentUrl(orderId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                binding.btnPay.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getData();
                    Log.d("CHECKOUT_DEBUG", "Mở trình duyệt thanh toán: " + url);

                    // Mở trình duyệt để User nhập thẻ
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    // ĐÂY LÀ NƠI VNPay BÁO WEBSITE CHƯA PHÊ DUYỆT
                    Toast.makeText(CheckoutActivity.this, "VNPay từ chối kết nối. Kiểm tra cấu hình Sandbox!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                binding.btnPay.setEnabled(true);
                Log.e("CHECKOUT_ERROR", "Lỗi lấy link VNPay: " + t.getMessage());
            }
        });
    }
}