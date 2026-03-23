package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.myapp.RetrofitClient;
import com.example.myapp.Utils.NotificationHelper;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.adapters.CartAdapter;
import com.example.myapp.databinding.ActivityCartBinding;
import com.example.myapp.models.request.AdjustProductQuantityInCartRequest;
import com.example.myapp.models.response.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;
    private CartAdapter adapter;
    private int currentCartId = -1;

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (SharedPrefsManager.getAccessToken() == null) {
            Snackbar.make(binding.getRoot(), "Vui lòng đăng nhập để xem giỏ hàng", Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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
                Snackbar.make(binding.getRoot(), "Giỏ hàng hiện đang trống!", Snackbar.LENGTH_SHORT).show();
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
        RecyclerView.ItemAnimator animator = binding.rvCartItems.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
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
                        binding.tvCartTotal.setText(CURRENCY.format(data.getTotalPrice().doubleValue()));
                    } else {
                        binding.tvCartTotal.setText(CURRENCY.format(0.00));
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                Snackbar.make(binding.getRoot(), "Lỗi kết nối server!", Snackbar.LENGTH_LONG)
                        .setAction("Thử lại", v -> fetchCart())
                        .show();
            }
        });
    }

    private void updateQuantity(int cartItemId, int qty) {
        AdjustProductQuantityInCartRequest req = new AdjustProductQuantityInCartRequest(cartItemId, qty);
        RetrofitClient.getApiService().adjustQuantity(req).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartResponse>> c, Response<ApiResponse<CartResponse>> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                    CartResponse updated = r.body().getData();
                    binding.tvCartTotal.setText(updated.getTotalPrice() != null
                            ? CURRENCY.format(updated.getTotalPrice().doubleValue())
                            : CURRENCY.format(0.00));
                    NotificationHelper.scheduleCartNotification(CartActivity.this);
                } else {
                    fetchCart(); // fallback: full refresh if response is unexpected
                }
            }
            @Override public void onFailure(Call<ApiResponse<CartResponse>> c, Throwable t) {
                fetchCart(); // fallback: re-sync on network error to revert optimistic update
            }
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