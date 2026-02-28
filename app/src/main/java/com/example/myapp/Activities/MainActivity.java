package com.example.myapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.myapp.R;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.Workers.CartBadgeWorker;
import com.example.myapp.adapters.ProductAdapter;
import com.example.myapp.databinding.ActivityMainBinding;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.PageResponse;
import com.example.myapp.models.response.ProductListResponse;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ProductAdapter adapter;
    private String currentSort = "asc";
    private String currentKeyword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPrefsManager.init(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // 1. Dọn dẹp Worker spam (Chạy 1 lần rồi có thể xóa dòng này)
        WorkManager.getInstance(this).cancelAllWork();

        checkNotificationPermission();
        setupRecyclerView();
        setupSearchAndSort();
        loadProductsFromBE();
        setupUniqueCartWorker();

        // 2. Logic Chat thông minh (Đã xóa đoạn thừa ở hàm search)
        binding.fabChat.setOnClickListener(v -> {
            String role = SharedPrefsManager.getUserRole();
            Log.d("CHAT_FLOW", "Mở Chat với quyền: " + role);
            if ("Admin".equalsIgnoreCase(role)) {
                startActivity(new Intent(this, AdminChatListActivity.class));
            } else {
                startActivity(new Intent(this, ChatActivity.class));
            }
        });
    }

    private void setupUniqueCartWorker() {
        PeriodicWorkRequest cartWork = new PeriodicWorkRequest.Builder(
                CartBadgeWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "UniqueCartBadgeCheck", ExistingPeriodicWorkPolicy.KEEP, cartWork);
        WorkManager.getInstance(this).enqueue(new OneTimeWorkRequest.Builder(CartBadgeWorker.class).build());
    }

    private void setupSearchAndSort() {
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentKeyword = binding.etSearch.getText().toString().trim();
                loadProductsFromBE();
                return true;
            }
            return false;
        });

        binding.btnSortPrice.setOnClickListener(v -> {
            currentSort = currentSort.equals("asc") ? "desc" : "asc";
            binding.btnSortPrice.setText("Giá " + (currentSort.equals("asc") ? "↑" : "↓"));
            loadProductsFromBE();
        });
    }

    private void performLogout() {
        SharedPrefsManager.clearAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.main_menu, menu); return true; }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_map) startActivity(new Intent(this, MapActivity.class));
        else if (item.getItemId() == R.id.action_logout) performLogout();
        return super.onOptionsItemSelected(item);
    }
    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);
    }
    private void loadProductsFromBE() {
        RetrofitClient.getApiService().listProducts(null, null, null, null, currentKeyword, currentSort, 0, 20)
                .enqueue(new Callback<ApiResponse<PageResponse<ProductListResponse>>>() {
                    @Override public void onResponse(Call<ApiResponse<PageResponse<ProductListResponse>>> call, Response<ApiResponse<PageResponse<ProductListResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) adapter.setData(response.body().getData().getContent());
                    }
                    @Override public void onFailure(Call<ApiResponse<PageResponse<ProductListResponse>>> call, Throwable t) { Log.e("API_ERROR", t.getMessage()); }
                });
    }
}