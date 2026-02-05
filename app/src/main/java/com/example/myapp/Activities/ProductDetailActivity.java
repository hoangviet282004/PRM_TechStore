package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.RetrofitClient;
import com.example.myapp.adapters.ImageSliderAdapter;
import com.example.myapp.databinding.ActivityProductDetailBinding;
import com.example.myapp.models.request.ManageProductToCartRequest;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.CartResponse;
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
    private int productId;
    private ImageSliderAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Adapter rỗng sớm
        imageAdapter = new ImageSliderAdapter(new ArrayList<>());
        binding.vpProductImages.setAdapter(imageAdapter);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
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
            // Hiện ProgressBar đã khai báo trong XML
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnAddToCart.setEnabled(false);

            ManageProductToCartRequest req = new ManageProductToCartRequest(productId, quantity);
            RetrofitClient.getApiService().addProduct(req).enqueue(new Callback<ApiResponse<CartResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<CartResponse>> call, Response<ApiResponse<CartResponse>> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAddToCart.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAddToCart.setEnabled(true);
                    Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadProductDetail(int id) {
        RetrofitClient.getApiService().getProductDetail(id).enqueue(new Callback<ApiResponse<ProductDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductDetailResponse>> call, Response<ApiResponse<ProductDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body().getData());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ProductDetailResponse>> call, Throwable t) {}
        });
    }

    private void updateUI(ProductDetailResponse detail) {
        binding.tvDetailName.setText(detail.getProductName());
        binding.tvDetailPrice.setText(String.format("%,.0f VNĐ", detail.getPrice().doubleValue()));
        binding.tvFullDescription.setText(detail.getFullDescription());

        List<String> images = new ArrayList<>();
        if (detail.getPrimaryImageUrl() != null) images.add(detail.getPrimaryImageUrl());
        if (detail.getAdditionalImageUrls() != null) images.addAll(detail.getAdditionalImageUrls());

        imageAdapter.setData(images);
        new TabLayoutMediator(binding.tabIndicator, binding.vpProductImages, (tab, pos) -> {}).attach();

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