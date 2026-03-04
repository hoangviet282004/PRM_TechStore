package com.example.myapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarCheckout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Đang chuẩn bị thanh toán...");
        loadingDialog.setCancelable(false);

        cartId = getIntent().getIntExtra("CART_ID", -1);

        binding.btnPay.setOnClickListener(v -> {
            if (validateForm()) {
                createOrder();
            }
        });

        handleIntent(getIntent());
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (binding.etReceiverName.getText().toString().trim().isEmpty()) {
            binding.tilReceiverName.setError("Nhập tên người nhận");
            isValid = false;
        } else binding.tilReceiverName.setError(null);

        if (binding.etReceiverPhone.getText().toString().trim().length() < 10) {
            binding.tilReceiverPhone.setError("SĐT không hợp lệ");
            isValid = false;
        } else binding.tilReceiverPhone.setError(null);

        if (binding.etAddress.getText().toString().trim().isEmpty()) {
            binding.tilAddress.setError("Nhập địa chỉ giao hàng");
            isValid = false;
        } else binding.tilAddress.setError(null);

        return isValid;
    }

    private void createOrder() {
        loadingDialog.show(); // HIỆN LOADING
        binding.btnPay.setEnabled(false);

        String fullAddress = "Người nhận: " + binding.etReceiverName.getText().toString() +
                " | SĐT: " + binding.etReceiverPhone.getText().toString() +
                " | ĐC: " + binding.etAddress.getText().toString();

        CreateOrderRequest req = new CreateOrderRequest(cartId, "PayOs", fullAddress);

        RetrofitClient.getApiService().createOrder(req).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    getPayOsPaymentUrl(response.body().getData().getId());
                } else {
                    loadingDialog.dismiss();
                    binding.btnPay.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, "Lỗi tạo đơn!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                binding.btnPay.setEnabled(true);
            }
        });
    }

    private void getPayOsPaymentUrl(int orderId) {
        RetrofitClient.getApiService().getPaymentUrl(orderId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {
                loadingDialog.dismiss(); // TẮT LOADING
                if (response.isSuccessful() && response.body() != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(response.body().getData())));
                }
                binding.btnPay.setEnabled(true);
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                binding.btnPay.setEnabled(true);
            }
        });
    }

    // Logic về thẳng trang chủ khi nhấn nút Back
    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
        dialog.findViewById(com.example.myapp.R.id.btnGoHome).setOnClickListener(v -> {
            dialog.dismiss();
            onSupportNavigateUp();
        });
        dialog.show();
    }
}