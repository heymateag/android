package org.telegram.ui.Heymate.payment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import works.heymate.beta.R;

public class PaymentNotifierService extends Service {

    private static final String CHANNEL_ID = "PaymentNotifier";
    private static final int NOTIFICATION_ID = 12392;

    public static void start(Context context) {
        context.startService(getIntent(context));
    }

    public static void stop(Context context) {
        context.stopService(getIntent(context));
    }

    private static Intent getIntent(Context context) {
        return new Intent(context, PaymentNotifierService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pending payment")
                .setContentText("Your payment is being processed.")
                .setAutoCancel(false)
                .setDefaults(0)
                .setNotificationSilent()
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.cancel(NOTIFICATION_ID);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
