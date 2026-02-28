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
import com.example.myapp.models.response.ChatRoomResponse;
import com.example.myapp.models.response.PageResponse;
import com.example.myapp.models.response.ProductListResponse;

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
    private Handler badgeHandler = new Handler();

    private Runnable badgeRunnable = new Runnable() {

        @Override
        public void run() {
            updateChatBadge();
            badgeHandler.postDelayed(this, 10000); // 10 giây cập nhật 1 lần
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        badgeHandler.post(badgeRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        badgeHandler.removeCallbacks(badgeRunnable);
    }

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

        // Khi nhấn nút Chat để vào hỗ trợ
        binding.fabChat.setOnClickListener(v -> {
            binding.tvChatBadge.setVisibility(android.view.View.GONE); // Xóa dấu ! khi đã vào xem

            String role = SharedPrefsManager.getUserRole();
            if ("Admin".equalsIgnoreCase(role)) {
                startActivity(new Intent(this, AdminChatListActivity.class));
            } else {
                startActivity(new Intent(this, ChatActivity.class));
            }
        });
    }

// Hàm để gọi từ WebSocket khi có tin nhắn mới nổ về
        public void triggerNewMessageBadge() {
            runOnUiThread(() -> binding.tvChatBadge.setVisibility(android.view.View.VISIBLE));
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

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi quay lại màn hình chính, cập nhật lại số tin nhắn chưa đọc
        updateChatBadge();
    }

    public void updateChatBadge() {
        String token = SharedPrefsManager.getAccessToken();
        if (token == null) return;
        String bearerToken = "Bearer " + token;
        String role = SharedPrefsManager.getUserRole();

        if ("Admin".equalsIgnoreCase(role)) {
            // ADMIN: Lấy danh sách phòng và cộng dồn unreadCount
            RetrofitClient.getApiService().getAdminRooms(bearerToken, 0, 100).enqueue(new Callback<ApiResponse<PageResponse<ChatRoomResponse>>>() {
                @Override
                public void onResponse(Call<ApiResponse<PageResponse<ChatRoomResponse>>> call, Response<ApiResponse<PageResponse<ChatRoomResponse>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        long totalUnread = 0;
                        List<ChatRoomResponse> rooms = response.body().getData().getContent();
                        for (ChatRoomResponse room : rooms) {
                            totalUnread += room.getUnreadCount();
                        }
                        showNumberOnBadge(totalUnread);
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<PageResponse<ChatRoomResponse>>> call, Throwable t) {}
            });
        } else {
            // CUSTOMER: Lấy unreadCount từ phòng duy nhất của mình
            RetrofitClient.getApiService().getMyRoom(bearerToken).enqueue(new Callback<ApiResponse<ChatRoomResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ChatRoomResponse>> call, Response<ApiResponse<ChatRoomResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        long unread = response.body().getData().getUnreadCount();
                        showNumberOnBadge(unread);
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<ChatRoomResponse>> call, Throwable t) {}
            });
        }
    }

    private void showNumberOnBadge(long count) {
        runOnUiThread(() -> {
            if (count > 0) {
                binding.tvChatBadge.setVisibility(View.VISIBLE);
                // Nếu nhiều quá thì hiện 9+, không thì hiện số
                binding.tvChatBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            } else {
                binding.tvChatBadge.setVisibility(View.GONE);
            }
        });
    }
}