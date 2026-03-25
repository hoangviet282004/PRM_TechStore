package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.adapters.OrderSummaryItemAdapter;
import com.example.myapp.RetrofitClient;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.OrderSummaryResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_CODE = "ORDER_CODE";

    private static final NumberFormat CURRENCY = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        String orderCode = getIntent().getStringExtra(EXTRA_ORDER_CODE);

        TextView tvOrderCode = findViewById(R.id.tvOrderCode);
        tvOrderCode.setText("Mã thanh toán: #" + (orderCode != null ? orderCode : "N/A"));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> goHome());

        if (orderCode != null && !orderCode.isEmpty()) {
            try {
                loadOrderSummary(orderCode);
            } catch (NumberFormatException e) {
                hideLoading();
            }
        } else {
            hideLoading();
        }
    }

    private void loadOrderSummary(String orderCode) {
        RetrofitClient.getApiService().getOrderSummary(orderCode).enqueue(new Callback<ApiResponse<OrderSummaryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderSummaryResponse>> call, Response<ApiResponse<OrderSummaryResponse>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindSummary(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderSummaryResponse>> call, Throwable t) {
                hideLoading();
            }
        });
    }

    private void bindSummary(OrderSummaryResponse summary) {
        // Order details
        if (summary.getOrder() != null) {
            CardView cardOrder = findViewById(R.id.cardOrderDetails);
            cardOrder.setVisibility(View.VISIBLE);

            TextView tvId = findViewById(R.id.tvSummaryOrderId);
            tvId.setText("#" + summary.getOrder().getId());

            TextView tvStatus = findViewById(R.id.tvSummaryOrderStatus);
            tvStatus.setText(summary.getOrder().getOrderStatus());

            TextView tvMethod = findViewById(R.id.tvSummaryPaymentMethod);
            tvMethod.setText(summary.getOrder().getPaymentMethod());

            TextView tvAddress = findViewById(R.id.tvSummaryAddress);
            tvAddress.setText(summary.getOrder().getBillingAddress());

            TextView tvName = findViewById(R.id.tvSummaryBillingName);
            tvName.setText(summary.getOrder().getBillingFullName());

            TextView tvPhone = findViewById(R.id.tvSummaryBillingPhone);
            tvPhone.setText(summary.getOrder().getBillingPhone());
        }

        // Payment details
        if (summary.getPayment() != null) {
            CardView cardPayment = findViewById(R.id.cardPaymentDetails);
            cardPayment.setVisibility(View.VISIBLE);

            TextView tvAmount = findViewById(R.id.tvSummaryAmount);
            BigDecimal amount = summary.getPayment().getAmount();
            tvAmount.setText(amount != null ? CURRENCY.format(amount) : "-");

            TextView tvPayStatus = findViewById(R.id.tvSummaryPaymentStatus);
            tvPayStatus.setText(summary.getPayment().getPaymentStatus());
        }

        // Items
        if (summary.getCartItems() != null && !summary.getCartItems().isEmpty()) {
            CardView cardItems = findViewById(R.id.cardItems);
            cardItems.setVisibility(View.VISIBLE);

            RecyclerView rv = findViewById(R.id.rvOrderItems);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new OrderSummaryItemAdapter(summary.getCartItems()));
        }
    }

    private void hideLoading() {
        ProgressBar pb = findViewById(R.id.progressBar);
        if (pb != null) pb.setVisibility(View.GONE);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
