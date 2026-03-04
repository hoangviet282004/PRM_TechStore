package com.example.myapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.RetrofitClient;
import com.example.myapp.databinding.ActivityCheckoutBinding;
import com.example.myapp.models.request.CreateOrderRequest;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.OrderResponse;
import com.example.myapp.models.response.UserResponse;
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

        setSupportActionBar(binding.toolbarCheckout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cartId = getIntent().getIntExtra("CART_ID", -1);
        if (cartId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy giỏ hàng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnPay.setOnClickListener(v -> {
            if (validateForm()) createOrder();
        });

        fetchProfileAndPrefill();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (binding.etReceiverName.getText().toString().trim().isEmpty()) {
            binding.tilReceiverName.setError("Nhập tên người nhận");
            isValid = false;
        } else binding.tilReceiverName.setError(null);

        if (!binding.etReceiverPhone.getText().toString().trim().matches("^0[0-9]{9}$")) {
            binding.tilReceiverPhone.setError("SĐT không hợp lệ (10 chữ số, bắt đầu bằng 0)");
            isValid = false;
        } else binding.tilReceiverPhone.setError(null);

        if (binding.etAddress.getText().toString().trim().isEmpty()) {
            binding.tilAddress.setError("Nhập địa chỉ giao hàng");
            isValid = false;
        } else binding.tilAddress.setError(null);

        return isValid;
    }

    private void fetchProfileAndPrefill() {
        RetrofitClient.getApiService().getProfile().enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Response<ApiResponse<UserResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                UserResponse profile = response.body().getData();
                if (profile == null) return;
                if (profile.getFullName() != null && !profile.getFullName().isEmpty()) {
                    binding.etReceiverName.setText(profile.getFullName());
                }
                if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) {
                    binding.etReceiverPhone.setText(profile.getPhoneNumber());
                }
                if (profile.getAddress() != null && !profile.getAddress().isEmpty()) {
                    binding.etAddress.setText(profile.getAddress());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                // Silently ignore — user can fill in manually
            }
        });
    }

    private void createOrder() {
        showLoading(true);
        binding.btnPay.setEnabled(false);

        String name = binding.etReceiverName.getText().toString().trim();
        String phone = binding.etReceiverPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        CreateOrderRequest req = new CreateOrderRequest(cartId, "PayOs", name, phone, address);

        RetrofitClient.getApiService().createOrder(req).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    getPayOsPaymentUrl(response.body().getData().getId());
                } else {
                    showLoading(false);
                    binding.btnPay.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, "Lỗi tạo đơn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                binding.btnPay.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPayOsPaymentUrl(int orderId) {
        RetrofitClient.getApiService().getPaymentUrl(orderId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                showLoading(false);
                binding.btnPay.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().getData();
                    if (payUrl != null && !payUrl.isEmpty()) {
                        Intent intent = new Intent(CheckoutActivity.this, PaymentWebViewActivity.class);
                        intent.putExtra(PaymentWebViewActivity.EXTRA_URL, payUrl);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CheckoutActivity.this, "Link thanh toán không hợp lệ.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this, "Không thể lấy link thanh toán. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                showLoading(false);
                binding.btnPay.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        goHome();
        return true;
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "techexpress".equals(data.getScheme())) {
            if ("PAID".equalsIgnoreCase(data.getQueryParameter("status"))) {
                showSuccessDialog();
            }
        }
    }

    private void showSuccessDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(com.example.myapp.R.layout.dialog_payment_success);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.findViewById(com.example.myapp.R.id.btnGoHome).setOnClickListener(v -> {
            dialog.dismiss();
            goHome();
        });
        dialog.show();
    }
}
