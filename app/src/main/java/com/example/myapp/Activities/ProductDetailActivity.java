package com.example.myapp.Activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.RetrofitClient;
import com.example.myapp.adapters.ImageSliderAdapter;
import com.example.myapp.databinding.ActivityProductDetailBinding;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.ProductDetailResponse;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private ActivityProductDetailBinding binding;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập Toolbar để hiện nút quay lại
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Để trống để nhường chỗ cho ảnh
        }

        // Nhận ID từ Adapter
        int productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId != -1) {
            loadProductDetail(productId);
        }

        setupQuantityActions();
    }

    private void setupQuantityActions() {
        binding.btnPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
        });

        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> {
            // Yêu cầu: Cho phép thêm vào giỏ hàng với số lượng đã chọn
            Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProductDetail(int id) {
        RetrofitClient.getApiService().getProductDetail(id).enqueue(new Callback<ApiResponse<ProductDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductDetailResponse>> call, Response<ApiResponse<ProductDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductDetailResponse detail = response.body().getData();
                    updateUI(detail);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductDetailResponse>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(ProductDetailResponse detail) {
        binding.tvDetailName.setText(detail.getProductName());
        binding.tvDetailPrice.setText(String.format("%,.0f VNĐ", detail.getPrice().doubleValue()));
        binding.tvFullDescription.setText(detail.getFullDescription());

        // Hiển thị nhiều ảnh bằng ViewPager2
        List<String> images = new ArrayList<>();
        if (detail.getPrimaryImageUrl() != null) images.add(detail.getPrimaryImageUrl());
        if (detail.getAdditionalImageUrls() != null) images.addAll(detail.getAdditionalImageUrls());

        binding.vpProductImages.setAdapter(new ImageSliderAdapter(images));

        // Kết nối các dấu chấm chỉ báo (Dots indicator)
        new TabLayoutMediator(binding.tabIndicator, binding.vpProductImages, (tab, pos) -> {}).attach();

        // Hiển thị Technical Specs từ Map JSON
        StringBuilder specs = new StringBuilder();
        if (detail.getTechnicalSpecifications() != null) {
            for (Map.Entry<String, Object> entry : detail.getTechnicalSpecifications().entrySet()) {
                specs.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        binding.tvTechnicalSpecs.setText(specs.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}