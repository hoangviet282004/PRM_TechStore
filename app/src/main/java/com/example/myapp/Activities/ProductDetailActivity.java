package com.example.myapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.RetrofitClient;
import com.example.myapp.Utils.NotificationHelper;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.adapters.ImageSliderAdapter;
import com.example.myapp.databinding.ActivityProductDetailBinding;
import com.example.myapp.models.request.ManageProductToCartRequest;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.CartResponse;
import com.example.myapp.models.response.ProductDetailResponse;
import androidx.viewpager2.widget.ViewPager2;

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
        setupImageNavigation();
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
            if (SharedPrefsManager.getAccessToken() == null) {
                Snackbar.make(binding.getRoot(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Snackbar.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnAddToCart.setEnabled(false);

            ManageProductToCartRequest req = new ManageProductToCartRequest(productId, quantity);
            RetrofitClient.getApiService().addProduct(req).enqueue(new Callback<ApiResponse<CartResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<CartResponse>> call, Response<ApiResponse<CartResponse>> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAddToCart.setEnabled(true);

                    if (response.isSuccessful()) {
                        Snackbar.make(binding.getRoot(), "Đã thêm vào giỏ!", Snackbar.LENGTH_SHORT).show();
                        NotificationHelper.scheduleCartNotification(ProductDetailActivity.this);
                        startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<CartResponse>> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAddToCart.setEnabled(true);
                    Snackbar.make(binding.getRoot(), "Lỗi kết nối!", Snackbar.LENGTH_SHORT).show();
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
        updateArrowVisibility(0, images.size());

        StringBuilder specs = new StringBuilder();
        if (detail.getTechnicalSpecifications() != null) {
            for (Map.Entry<String, Object> entry : detail.getTechnicalSpecifications().entrySet()) {
                specs.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        binding.tvTechnicalSpecs.setText(specs.toString());
    }

    private void setupImageNavigation() {
        binding.btnPrevImage.setOnClickListener(v -> {
            int current = binding.vpProductImages.getCurrentItem();
            if (current > 0) binding.vpProductImages.setCurrentItem(current - 1, true);
        });
        binding.btnNextImage.setOnClickListener(v -> {
            int current = binding.vpProductImages.getCurrentItem();
            int last = imageAdapter.getItemCount() - 1;
            if (current < last) binding.vpProductImages.setCurrentItem(current + 1, true);
        });
        binding.vpProductImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateArrowVisibility(position, imageAdapter.getItemCount());
            }
        });
    }

    private void updateArrowVisibility(int position, int total) {
        binding.btnPrevImage.setVisibility(total > 1 && position > 0 ? View.VISIBLE : View.GONE);
        binding.btnNextImage.setVisibility(total > 1 && position < total - 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}