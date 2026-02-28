package com.example.myapp.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.myapp.Activities.ChatActivity; // Thêm import này
import com.example.myapp.R;

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
                .setContentTitle("TechExpress Shopping")
                .setContentText("Bạn đang có " + itemCount + " sản phẩm trong giỏ hàng.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setNumber(itemCount)
                .setAutoCancel(true);

        manager.notify(NOTI_ID, builder.build());
    }

    // --- THÊM HÀM MỚI THEO YÊU CẦU ---
    public static void showChatNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHAT_CHANNEL_ID, "Tin nhắn hỗ trợ", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // Khi bấm vào thông báo thì mở màn hình Chat
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
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

    // --- GIỮ NGUYÊN HÀM XÓA CỦA ÔNG ---
    public static void clearNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTI_ID); // Xóa cart
            manager.cancel(CHAT_NOTI_ID); // Xóa chat
        }
    }
}