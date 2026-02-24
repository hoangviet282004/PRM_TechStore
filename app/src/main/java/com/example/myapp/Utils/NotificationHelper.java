package com.example.myapp.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.myapp.Activities.MainActivity;
import com.example.myapp.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "cart_notification_channel";
    private static final int NOTI_ID = 1; // ID này phải khớp giữa lúc hiện và lúc xóa

    public static void showCartBadgeNotification(Context context, int itemCount) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Giỏ hàng", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        // SỬA TẠI ĐÂY: Đổi MainActivity.class thành CartActivity.class
        Intent intent = new Intent(context, com.example.myapp.Activities.CartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("TechExpress Shopping")
                .setContentText("Bạn đang có " + itemCount + " sản phẩm trong giỏ hàng.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Gắn intent mới đã trỏ vào CartActivity
                .setNumber(itemCount)
                .setAutoCancel(true);

        manager.notify(NOTI_ID, builder.build());
    }

    // HÀM ÔNG ĐANG THIẾU ĐÂY: Dùng để xóa cái chấm khi vào App
    public static void clearNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTI_ID); // Xóa thông báo theo ID
        }
    }
}