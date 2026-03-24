package com.example.myapp.Services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.myapp.RetrofitClient;
import com.example.myapp.SharedPrefsManager;
import com.example.myapp.Utils.NotificationHelper;
import com.example.myapp.models.request.DeviceTokenRequest;
import com.example.myapp.models.response.ApiResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class TechExpressMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);
        // Register with backend if user is already logged in
        if (SharedPrefsManager.getAccessToken() != null) {
            registerToken(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title, body;
        if (remoteMessage.getNotification() != null) {
            // Notification message (works in foreground only when app is active)
            title = remoteMessage.getNotification().getTitle();
            body  = remoteMessage.getNotification().getBody();
        } else {
            // Data-only message (onMessageReceived always called, even in background)
            title = remoteMessage.getData().getOrDefault("title", "Tin nhắn mới");
            body  = remoteMessage.getData().getOrDefault("body", "");
        }
        // Pass roomId and clientName to ChatActivity if present in data payload (admin flow)
        String roomIdStr = remoteMessage.getData().get("roomId");
        String clientName = remoteMessage.getData().get("clientName");
        int roomId = -1;
        try { if (roomIdStr != null) roomId = Integer.parseInt(roomIdStr); } catch (NumberFormatException ignored) {}
        NotificationHelper.showChatNotification(this, title != null ? title : "Tin nhắn mới", body != null ? body : "", roomId, clientName);
    }

    public static void registerToken(String token) {
        RetrofitClient.getApiService()
                .registerDeviceToken(new DeviceTokenRequest(token))
                .enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<ApiResponse<Void>> call,
                                           @NonNull retrofit2.Response<ApiResponse<Void>> response) {
                        Log.d("FCM", "Token registered: " + response.code());
                    }
                    @Override
                    public void onFailure(@NonNull retrofit2.Call<ApiResponse<Void>> call,
                                         @NonNull Throwable t) {
                        Log.e("FCM", "Token register failed: " + t.getMessage());
                    }
                });
    }

    public static void removeToken(String token) {
        RetrofitClient.getApiService()
                .removeDeviceToken(token)
                .enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<ApiResponse<Void>> call,
                                           @NonNull retrofit2.Response<ApiResponse<Void>> response) {
                        Log.d("FCM", "Token removed: " + response.code());
                    }
                    @Override
                    public void onFailure(@NonNull retrofit2.Call<ApiResponse<Void>> call,
                                         @NonNull Throwable t) {
                        Log.e("FCM", "Token remove failed: " + t.getMessage());
                    }
                });
    }
}
