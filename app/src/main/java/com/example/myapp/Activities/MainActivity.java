package com.example.myapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapp.R;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.Workers.CartBadgeWorker;
import com.example.myapp.adapters.ProductAdapter;
import com.example.myapp.databinding.ActivityMainBinding;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.BrandResponse; // Đảm bảo đã tạo model này
import com.example.myapp.models.response.CategoryResponse;
import com.example.myapp.models.response.ChatRoomResponse;
import com.example.myapp.models.response.PageResponse;
import com.example.myapp.models.response.ProductListResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ProductAdapter adapter;
    private String currentSort = "asc";
    private String currentKeyword = null;

    // Logic cho Bộ lọc
    private Integer selectedCategoryId = null;
    private Integer selectedBrandId = null;
    private List<CategoryResponse> categoryList = new ArrayList<>();
    private List<BrandResponse> brandList = new ArrayList<>();

    // Polling Badge Chat (3 giây)
    private final Handler badgeHandler = new Handler();
    private final Runnable badgeRunnable = new Runnable() {
        @Override
        public void run() {
            updateChatBadge();
            badgeHandler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPrefsManager.init(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        WorkManager.getInstance(this).cancelAllWork();
        checkNotificationPermission();
        setupRecyclerView();
        setupSearchAndFilters();

        // Tải dữ liệu ban đầu từ Backend
        loadCategoriesFromServer();
        loadBrandsFromServer();
        loadProductsFromBE();

        setupUniqueCartWorker();

        binding.fabChat.setOnClickListener(v -> {
            binding.tvChatBadge.setVisibility(View.GONE);
            String role = SharedPrefsManager.getUserRole();
            if ("Admin".equalsIgnoreCase(role)) {
                startActivity(new Intent(this, AdminChatListActivity.class));
            } else {
                startActivity(new Intent(this, ChatActivity.class));
            }
        });
    }

    private void setupSearchAndFilters() {
        // Tìm kiếm theo từ khóa
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentKeyword = binding.etSearch.getText().toString().trim();
                loadProductsFromBE();
                return true;
            }
            return false;
        });

        // Sắp xếp giá
        binding.btnSortPrice.setOnClickListener(v -> {
            currentSort = currentSort.equals("asc") ? "desc" : "asc";
            binding.btnSortPrice.setText("Giá " + (currentSort.equals("asc") ? "↑" : "↓"));
            loadProductsFromBE();
        });

        // Nút Lọc tổng hợp
        binding.btnApplyFilter.setOnClickListener(v -> {
            // Lấy ID từ Category Spinner
            int catePos = binding.spCategories.getSelectedItemPosition();
            selectedCategoryId = (catePos > 0) ? categoryList.get(catePos - 1).getId() : null;

            // Lấy ID từ Brand Spinner (Nếu ông đã thêm spBrands vào XML)
            // int brandPos = binding.spBrands.getSelectedItemPosition();
            // selectedBrandId = (brandPos > 0) ? brandList.get(brandPos - 1).getId() : null;

            loadProductsFromBE();
        });
    }

    private void loadCategoriesFromServer() {
        RetrofitClient.getApiService().getAllCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CategoryResponse>>> call, @NonNull Response<ApiResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body().getData();
                    setupSpinner(binding.spCategories, categoryList, "danh mục");
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<CategoryResponse>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadBrandsFromServer() {
        RetrofitClient.getApiService().getAllBrands().enqueue(new Callback<ApiResponse<List<BrandResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<BrandResponse>>> call, @NonNull Response<ApiResponse<List<BrandResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    brandList = response.body().getData();
                    // setupSpinner(binding.spBrands, brandList, "thương hiệu");
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<BrandResponse>>> call, @NonNull Throwable t) {}
        });
    }

    // Hàm tiện ích để đổ dữ liệu vào Spinner bất kỳ
    private void setupSpinner(android.widget.Spinner spinner, List<?> dataList, String type) {
        List<String> names = new ArrayList<>();
        names.add("--- Tất cả " + type + " ---");
        for (Object item : dataList) names.add(item.toString());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void loadProductsFromBE() {
        BigDecimal min = null, max = null;
        try {
            String minStr = binding.etMinPrice.getText().toString().trim();
            String maxStr = binding.etMaxPrice.getText().toString().trim();
            if (!minStr.isEmpty()) min = new BigDecimal(minStr);
            if (!maxStr.isEmpty()) max = new BigDecimal(maxStr);
        } catch (Exception e) {
            Log.e("FILTER_ERROR", "Giá không hợp lệ");
        }

        // Chuyển BrandId đơn lẻ thành List để khớp với API
        List<Integer> brandIds = selectedBrandId != null ? Collections.singletonList(selectedBrandId) : null;

        RetrofitClient.getApiService().listProducts(
                brandIds,           // Truyền danh sách ID thương hiệu
                selectedCategoryId, // Truyền ID danh mục
                min, max, currentKeyword, currentSort, 0, 20
        ).enqueue(new Callback<ApiResponse<PageResponse<ProductListResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PageResponse<ProductListResponse>>> call, @NonNull Response<ApiResponse<PageResponse<ProductListResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    adapter.setData(response.body().getData().getContent());
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<PageResponse<ProductListResponse>>> call, @NonNull Throwable t) {}
        });
    }

    // Logic cập nhật Badge Chat giữ nguyên (đã tối ưu 3s)
    public void updateChatBadge() {
        String token = SharedPrefsManager.getAccessToken();
        if (token == null) return;
        String bearerToken = "Bearer " + token;
        String role = SharedPrefsManager.getUserRole();

        if ("Admin".equalsIgnoreCase(role)) {
            RetrofitClient.getApiService().getAdminRooms(bearerToken, 0, 100).enqueue(new Callback<ApiResponse<PageResponse<ChatRoomResponse>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<PageResponse<ChatRoomResponse>>> call, @NonNull Response<ApiResponse<PageResponse<ChatRoomResponse>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        long totalUnread = 0;
                        for (ChatRoomResponse room : response.body().getData().getContent()) {
                            totalUnread += room.getUnreadCount();
                        }
                        showNumberOnBadge(totalUnread);
                    }
                }
                @Override public void onFailure(@NonNull Call<ApiResponse<PageResponse<ChatRoomResponse>>> call, @NonNull Throwable t) {}
            });
        } else {
            RetrofitClient.getApiService().getMyRoom(bearerToken).enqueue(new Callback<ApiResponse<ChatRoomResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<ChatRoomResponse>> call, @NonNull Response<ApiResponse<ChatRoomResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        showNumberOnBadge(response.body().getData().getUnreadCount());
                    }
                }
                @Override public void onFailure(@NonNull Call<ApiResponse<ChatRoomResponse>> call, @NonNull Throwable t) {}
            });
        }
    }

    private void showNumberOnBadge(long count) {
        runOnUiThread(() -> {
            if (count > 0) {
                binding.tvChatBadge.setVisibility(View.VISIBLE);
                binding.tvChatBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            } else {
                binding.tvChatBadge.setVisibility(View.GONE);
            }
        });
    }

    // Các hàm khởi tạo cơ bản
    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupUniqueCartWorker() {
        PeriodicWorkRequest cartWork = new PeriodicWorkRequest.Builder(CartBadgeWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("UniqueCartBadgeCheck", ExistingPeriodicWorkPolicy.KEEP, cartWork);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }

    private void performLogout() {
        SharedPrefsManager.clearAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.main_menu, menu); return true; }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_map) startActivity(new Intent(this, MapActivity.class));
        else if (item.getItemId() == R.id.action_logout) performLogout();
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onStart() { super.onStart(); badgeHandler.post(badgeRunnable); }
    @Override protected void onStop() { super.onStop(); badgeHandler.removeCallbacks(badgeRunnable); }
    @Override protected void onResume() { super.onResume(); updateChatBadge(); }
}