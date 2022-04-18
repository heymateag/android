package org.telegram.ui.Heymate.payment;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.TG2HM;

import java.math.BigInteger;

import works.heymate.beta.R;
import works.heymate.core.wallet.AwaitBalance;
import works.heymate.core.wallet.Wallet;

public class AwaitSettlementService extends Service {

    private static final long BALANCE_CHECK_INTERVAL = 3L * 60L * 60L * 1000L; // TODO Make sure it is up to 2 days

    private static final String CHANNEL_ID = AwaitSettlement.CHANNEL_ID;
    private static final int NOTIFICATION_ID = 12309;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Wallet wallet = TG2HM.getWallet();

        if (wallet == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        long monitorSince = AwaitSettlement.getMonitorSince();
        String tokenAddress = AwaitSettlement.getTokenAddress();

        if (monitorSince == 0 || tokenAddress == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Checking balance")
                .setContentText("Checking for on ramp settlement.")
                .setAutoCancel(false)
                .setDefaults(0)
                .setNotificationSilent()
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);

        startForeground(NOTIFICATION_ID, notification);

        AwaitBalance.getLatestTransactions(wallet.getAddress(), jTransactions -> {
            notificationManager.cancel(NOTIFICATION_ID);
            stopSelf();

            if (jTransactions != null && jTransactions.length() > 0) {
                for (int i = 0; i < jTransactions.length(); i++) {
                    try {
                        JSONObject jTransaction = jTransactions.getJSONObject(i);

                        String from = jTransaction.getString("from");
                        String to = jTransaction.getString("to");
                        long timestamp = Long.parseLong(jTransaction.getString("timeStamp")) * 1000L;
                        BigInteger value = new BigInteger(jTransaction.getString("value"));
                        String contract = jTransaction.getString("contractAddress");

                        if (timestamp > monitorSince && tokenAddress.equals(contract) && wallet.getAddress().equals(to)) {
                            AwaitSettlement.doActionAndClear(getApplicationContext());
                            return;
                        }
                    } catch (JSONException e) { }
                }
            }

            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            am.set(
                    AlarmManager.RTC,
                    System.currentTimeMillis() + BALANCE_CHECK_INTERVAL,
                    PendingIntent.getBroadcast(
                            this,
                            1,
                            new Intent(this, AwaitSettlementReceiver.class),
                            PendingIntent.FLAG_UPDATE_CURRENT)
            );
        });

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
