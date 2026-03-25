package com.example.myapp.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.myapp.Activities.ChatActivity;
import com.example.myapp.R;
import com.example.myapp.Workers.CartBadgeWorker;

import java.util.concurrent.TimeUnit;

public class NotificationHelper {
    // Giữ nguyên ID cũ của ông
    private static final String CHANNEL_ID = "cart_notification_channel";
    private static final int NOTI_ID = 1;

    // THÊM: ID mới cho thông báo Chat để không bị ghi đè thông báo giỏ hàng
    private static final String CHAT_CHANNEL_ID = "chat_notification_channel";
    private static final int CHAT_NOTI_ID = 2;

    // --- GIỮ NGUYÊN HÀM CŨ CỦA ÔNG ---
    public static void showCartBadgeNotification(Context context, int itemCount) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Giỏ hàng", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, com.example.myapp.Activities.CartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("TechStore")
                .setContentText("Bạn đang có " + itemCount + " sản phẩm trong giỏ hàng.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setNumber(itemCount)
                .setAutoCancel(true);
        android.util.Log.d("NOTIF", "Badge count set to: " + itemCount);
        manager.notify(NOTI_ID, builder.build());
    }

    // --- THÊM HÀM MỚI THEO YÊU CẦU ---
    public static void showChatNotification(Context context, String title, String message) {
        showChatNotification(context, title, message, -1, null);
    }

    public static void showChatNotification(Context context, String title, String message, int roomId, String clientName) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHAT_CHANNEL_ID, "Tin nhắn hỗ trợ", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // Khi bấm vào thông báo thì mở màn hình Chat
        // If roomId is provided (admin flow), pass it so the correct room opens
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (roomId != -1) {
            intent.putExtra("ROOM_ID", roomId);
            if (clientName != null) intent.putExtra("CLIENT_NAME", clientName);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, roomId != -1 ? roomId : 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao để hiện banner
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(CHAT_NOTI_ID, builder.build());
    }

    // Schedule a debounced cart notification: waits 5s before firing.
    // If called again within those 5s, the timer resets (REPLACE policy).
    public static void scheduleCartNotification(Context context) {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CartBadgeWorker.class)
                .setInitialDelay(5, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "CartNotificationDebounce",
                ExistingWorkPolicy.REPLACE,
                work
        );
    }

    // Clears only the cart badge notification
    public static void clearCartNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(NOTI_ID);
    }

    // Clears all notifications (cart + chat)
    public static void clearNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTI_ID);
            manager.cancel(CHAT_NOTI_ID);
        }
    }
}