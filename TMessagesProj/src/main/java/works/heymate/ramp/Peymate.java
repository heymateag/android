package works.heymate.ramp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.telegram.ui.Heymate.HeymateConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import works.heymate.core.APICallback;
import works.heymate.core.Currency;
import works.heymate.core.Money;

public class Peymate {

    private static final String TAG = "Peymate";

    private static final long DEFAULT_CHECK_PERIOD = 2000;
    private static final long FAILURE_CHECK_PERIOD = 2000;

    private static final String KEY_CONTRACT_ID = "contract_id";

    public interface PaymentCheckListener {

        /**
         *
         * @param success
         * @return true if the result is consumed.
         */
        boolean onPaymentCheckResult(boolean success, String status);

    }

    private static Peymate mInstance = null;

    public static Peymate get(Context context) {
        if (mInstance == null) {
            mInstance = new Peymate(context.getApplicationContext());
        }

        return mInstance;
    }

    private final Context mContext;
    private final SharedPreferences mPreferences;

    private final List<WeakReference<PaymentCheckListener>> mListeners = new LinkedList<>();

    private Peymate(Context context) {
        mContext = context;
        mPreferences = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public void initiate(Money money, String address, APICallback<HTLC> callback) {
        Nimiq.createRequest(money.getCents() / 100d, Nimiq.ASSET_EUR, Currency.getNimiqCurrency(money.getCurrency()), address, (success, result, exception) -> {
            if (success) {
                mPreferences.edit()
                        .putString(KEY_CONTRACT_ID, result.id)
                        .apply();

                scheduleCheck(DEFAULT_CHECK_PERIOD);

                callback.onAPICallResult(success, result, exception);
            }
            else {
                callback.onAPICallResult(success, result, exception);
            }
        });
    }

    public boolean hasPayment() {
        return mPreferences.getString(KEY_CONTRACT_ID, null) != null;
    }

    public void addListener(PaymentCheckListener listener) {
        removeListener(listener);
        mListeners.add(new WeakReference<>(listener));
    }

    public void removeListener(PaymentCheckListener listener) {
        ListIterator<WeakReference<PaymentCheckListener>> iterator = mListeners.listIterator();

        while (iterator.hasNext()) {
            WeakReference<PaymentCheckListener> reference = iterator.next();

            if (reference.get() == null || reference.get().equals(listener)) {
                iterator.remove();
            }
        }
    }

    public void check() {
        String contractId = mPreferences.getString(KEY_CONTRACT_ID, null);

        if (contractId == null) {
            return;
        }

        Nimiq.checkStatus(contractId, (success, result, exception) -> {
            if (!hasPayment()) {
                // Someone cleared
                return;
            }

            List<PaymentCheckListener> listeners = new ArrayList<>(mListeners.size());

            for (WeakReference<PaymentCheckListener> reference: mListeners) {
                if (reference.get() != null) {
                    listeners.add(reference.get());
                }
            }

            boolean canRelease = success && result != null && HTLC.STATUS_SETTLED.equals(result.status);
            boolean releaseRequested = false;

            for (PaymentCheckListener listener: listeners) {
                releaseRequested |= listener.onPaymentCheckResult(success, result == null ? null : result.status);
            }

            if (canRelease && releaseRequested) {
                mPreferences.edit().clear().apply();
            }
            else {
                scheduleCheck(success ? DEFAULT_CHECK_PERIOD : FAILURE_CHECK_PERIOD);
            }
        });
    }

    public void testClear() {
        if (HeymateConfig.MAIN_NET) {
            return;
        }

        String contractId = mPreferences.getString(KEY_CONTRACT_ID, null);

        if (contractId == null) {
            return;
        }

        Nimiq.testClear(contractId, (success, result, exception) -> {
            if (success) {
                Log.i(TAG, "Called clear on mock.");
            }
            else {
                Log.e(TAG, "Failed to call clear on mock.", exception);
            }
        });
    }

    public void clear() {
        mPreferences.edit().clear().apply();

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent task = PendingIntent.getService(mContext, 0, PeymateService.getIntent(mContext), PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(task);
    }

    private void scheduleCheck(long delay) {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent task = PendingIntent.getService(mContext, 0, PeymateService.getIntent(mContext), PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + delay, task);
    }

}
