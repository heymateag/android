package org.telegram.ui.Heymate.payment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.LaunchActivity;

import works.heymate.beta.R;

public class AwaitSettlement {

    static final String CHANNEL_ID = "OnRamp Settlement";
    private static final int NOTIFICATION_ID = 12320;

    private static final String KEY_MONITOR_SINCE = "monitor_since";
    private static final String KEY_TOKEN_ADDRESS = "token_address";
    private static final String KEY_ACTION = "action";
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";

    public static void initiate(Context context, String tokenAddress, Uri action, String title, String message) {
        HeymateConfig config = HeymateConfig.getGeneral();

        config.set(KEY_MONITOR_SINCE, String.valueOf(System.currentTimeMillis()));
        config.set(KEY_TOKEN_ADDRESS, tokenAddress);
        config.set(KEY_ACTION, action.toString());
        config.set(KEY_TITLE, title);
        config.set(KEY_MESSAGE, message);

        context.startService(new Intent(context, AwaitSettlementService.class));
    }

    public static long getMonitorSince() {
        String monitorSince = HeymateConfig.getGeneral().get(KEY_MONITOR_SINCE);

        return monitorSince == null ? 0 : Long.parseLong(monitorSince);
    }

    public static String getTokenAddress() {
        return HeymateConfig.getGeneral().get(KEY_TOKEN_ADDRESS);
    }

    public static void doActionAndClear(Context context) {
        HeymateConfig config = HeymateConfig.getGeneral();

        Uri action = Uri.parse(config.get(KEY_ACTION));
        String title = config.get(KEY_TITLE);
        String message = config.get(KEY_MESSAGE);

        config.set(KEY_MONITOR_SINCE, null);
        config.set(KEY_TOKEN_ADDRESS, null);
        config.set(KEY_ACTION, null);
        config.set(KEY_TITLE, null);
        config.set(KEY_MESSAGE, null);

        Intent intent = new Intent(context, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(action);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
