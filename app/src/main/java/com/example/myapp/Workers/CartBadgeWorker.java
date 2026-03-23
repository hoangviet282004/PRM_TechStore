package com.example.myapp.Workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.Utils.NotificationHelper;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.CartItemResponse;
import com.example.myapp.models.response.CartResponse;
import java.util.List;
import retrofit2.Response;

public class CartBadgeWorker extends Worker {
    public CartBadgeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Skip if not logged in
        if (SharedPrefsManager.getAccessToken() == null) {
            NotificationHelper.clearCartNotification(getApplicationContext());
            return Result.success();
        }

        try {
            Response<ApiResponse<CartResponse>> response =
                    RetrofitClient.getApiService().getUserCart().execute();

            if (response.isSuccessful() && response.body() != null
                    && response.body().getData() != null) {
                List<CartItemResponse> items = response.body().getData().getItems();
                int count = 0;
                if (items != null) {
                    for (CartItemResponse item : items) {
                        count += item.getQuantity();
                    }
                }

                if (count > 0) {
                    NotificationHelper.showCartBadgeNotification(getApplicationContext(), count);
                } else {
                    NotificationHelper.clearCartNotification(getApplicationContext());
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}