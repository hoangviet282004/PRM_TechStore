package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapp.RetrofitClient;
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
    private int currentCartId = -1; // Biến lưu trữ cartId để truyền sang Checkout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();

        // Xử lý nút THANH TOÁN (10% điểm Billing)
        binding.btnCheckout.setOnClickListener(v -> {
            if (currentCartId != -1) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("CART_ID", currentCartId); // Truyền cartId sang Checkout
                startActivity(intent);
            } else {
                Toast.makeText(this, "Giỏ hàng hiện đang trống!", Toast.LENGTH_SHORT).show();
            }
        });

        fetchCart();
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

                    // Lưu lại ID giỏ hàng để dùng cho Checkout
                    currentCartId = data.getId();
                    Log.d("CART_DEBUG", "Nhận được Cart ID: " + currentCartId);

                    // Hiển thị danh sách sản phẩm (Cart Overview)
                    adapter.setData(data.getItems() != null ? data.getItems() : new ArrayList<>());

                    // Hiển thị tổng tiền (Cart Total) - Fix Null Safety
                    if (data.getTotalPrice() != null) {
                        binding.tvCartTotal.setText(String.format("%,.0f VNĐ", data.getTotalPrice().doubleValue()));
                    } else {
                        binding.tvCartTotal.setText("0 VNĐ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                Log.e("CART_DEBUG", "API Error: " + t.getMessage());
                Toast.makeText(CartActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantity(int productId, int qty) {
        // PATCH: Cập nhật số lượng theo đúng logic BE
        ManageProductToCartRequest req = new ManageProductToCartRequest(productId, qty);
        RetrofitClient.getApiService().adjustQuantity(req).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> c, Response<ApiResponse<CartResponse>> r) {
                if (r.isSuccessful()) fetchCart(); // Làm mới giỏ hàng sau khi sửa
            }
            @Override public void onFailure(Call<ApiResponse<CartResponse>> c, Throwable t) {}
        });
    }

    private void removeItem(int cartItemId) {
        // DELETE: Xóa món hàng dựa trên ID bản ghi trong giỏ
        RetrofitClient.getApiService().removeItem(cartItemId).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> c, Response<ApiResponse<CartResponse>> r) {
                if (r.isSuccessful()) fetchCart(); // Làm mới giỏ hàng sau khi xóa
            }
            @Override public void onFailure(Call<ApiResponse<CartResponse>> c, Throwable t) {}
        });
    }
}