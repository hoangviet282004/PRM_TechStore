package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.adapters.CartAdapter;
import com.example.myapp.databinding.ActivityCartBinding;
import com.example.myapp.models.request.ManageProductToCartRequest;
import com.example.myapp.models.response.*;

import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;
    private CartAdapter adapter;
    private int currentCartId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPrefsManager.getAccessToken() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // THIẾT LẬP TOOLBAR VÀ NÚT BACK
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setupRecyclerView();

        binding.btnCheckout.setOnClickListener(v -> {
            if (currentCartId != -1) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("CART_ID", currentCartId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Giỏ hàng hiện đang trống!", Toast.LENGTH_SHORT).show();
            }
        });

        fetchCart();
    }

    // FIX CHỖ NÀY: Quay lại thẳng MainActivity bằng cách đóng Activity hiện tại

    @Override
    public boolean onSupportNavigateUp() {
        // Tạo Intent để gọi lại MainActivity
        Intent intent = new Intent(CartActivity.this, MainActivity.class);

        // FLAG_ACTIVITY_CLEAR_TOP: Nếu MainActivity đã tồn tại, nó sẽ xóa sạch các trang nằm trên nó (như Detail)
        // FLAG_ACTIVITY_SINGLE_TOP: Không tạo mới MainActivity mà dùng lại cái cũ đang chạy ngầm
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish(); // Đóng trang Cart lại
        return true;
    }
    private void setupRecyclerView() {
        adapter = new CartAdapter(new CartAdapter.OnCartAction() {
            @Override
            public void onUpdateQty(int productId, int qty) {
                updateQuantity(productId, qty);
            }
            @Override
            public void onDelete(int cartItemId) {
                removeItem(cartItemId);
            }
        });

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(adapter);
    }

    private void fetchCart() {
        Log.d("CART_DEBUG", "Đang tải dữ liệu giỏ hàng...");
        RetrofitClient.getApiService().getUserCart().enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> call, Response<ApiResponse<CartResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    CartResponse data = response.body().getData();
                    currentCartId = data.getId();
                    adapter.setData(data.getItems() != null ? data.getItems() : new ArrayList<>());

                    if (data.getTotalPrice() != null) {
                        binding.tvCartTotal.setText(String.format("%,.0f VNĐ", data.getTotalPrice().doubleValue()));
                    } else {
                        binding.tvCartTotal.setText("0 VNĐ");
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantity(int productId, int qty) {
        ManageProductToCartRequest req = new ManageProductToCartRequest(productId, qty);
        RetrofitClient.getApiService().adjustQuantity(req).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> c, Response<ApiResponse<CartResponse>> r) {
                if (r.isSuccessful()) fetchCart();
            }
            @Override public void onFailure(Call<ApiResponse<CartResponse>> c, Throwable t) {}
        });
    }

    private void removeItem(int cartItemId) {
        RetrofitClient.getApiService().removeItem(cartItemId).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> c, Response<ApiResponse<CartResponse>> r) {
                if (r.isSuccessful()) fetchCart();
            }
            @Override public void onFailure(Call<ApiResponse<CartResponse>> c, Throwable t) {}
        });
    }
}