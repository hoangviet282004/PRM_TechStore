package com.example.myapp.Workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.myapp.RetrofitClient;
import com.example.myapp.Utils.NotificationHelper;
import com.example.myapp.models.response.ApiResponse;
import com.example.myapp.models.response.CartResponse;
import retrofit2.Response;

public class CartBadgeWorker extends Worker {
    public CartBadgeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Gọi API lấy giỏ hàng từ Server
            Response<ApiResponse<CartResponse>> response = RetrofitClient.getApiService().getUserCart().execute();

            if (response.isSuccessful() && response.body() != null) {
                int count = response.body().getData().getItems().size();

                if (count > 0) {
                    // Nếu có hàng thì hiện/cập nhật con số trên dot
                    NotificationHelper.showCartBadgeNotification(getApplicationContext(), count);
                } else {
                    // NẾU GIỎ HÀNG TRỐNG: Gọi lệnh xóa thông báo và dot
                    NotificationHelper.clearNotification(getApplicationContext());
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}