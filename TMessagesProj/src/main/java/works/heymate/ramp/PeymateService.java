package works.heymate.ramp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class PeymateService extends Service implements Peymate.PaymentCheckListener {

    public static Intent getIntent(Context context) {
        return new Intent(context, PeymateService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Peymate.get(this).addListener(this);
        Peymate.get(this).check();

        return START_STICKY;
    }

    @Override
    public boolean onPaymentCheckResult(boolean success, String status) {
        Peymate.get(this).removeListener(this);
        stopSelf();
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
