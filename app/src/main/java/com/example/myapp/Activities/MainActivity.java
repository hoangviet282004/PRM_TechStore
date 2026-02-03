package com.example.myapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.adapters.ProductAdapter;
import com.example.myapp.databinding.ActivityMainBinding;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.PageResponse;
import com.example.myapp.models.response.ProductListResponse;
import java.util.List;
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
        SharedPrefsManager.init(this); // Khởi tạo bộ nhớ bảo mật
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearchAndSort();
        loadProductsFromBE();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupSearchAndSort() {
        // Gửi keyword về BE khi nhấn Search (Filtering)
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentKeyword = binding.etSearch.getText().toString().trim();
                loadProductsFromBE();
                return true;
            }
            return false;
        });

        // Đổi kiểu sắp xếp (Sorting)
        binding.btnSortPrice.setOnClickListener(v -> {
            currentSort = currentSort.equals("asc") ? "desc" : "asc";
            binding.btnSortPrice.setText("Giá " + (currentSort.equals("asc") ? "↑" : "↓"));
            loadProductsFromBE();
        });
    }

    private void loadProductsFromBE() {
        // Truyền đúng các tham số mà @GetMapping của BE yêu cầu
        RetrofitClient.getApiService().listProducts(
                null, null, null, null,
                currentKeyword, currentSort, 0, 20
        ).enqueue(new Callback<ApiResponse<PageResponse<ProductListResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<ProductListResponse>>> call,
                                   Response<ApiResponse<PageResponse<ProductListResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Lấy content từ PageResponse
                    adapter.setData(response.body().getData().getContent());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<ProductListResponse>>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}