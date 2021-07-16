package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.myschedule.MyScheduleActivity;
import org.telegram.ui.Heymate.payment.BankTransferInformationActivity;
import org.telegram.ui.Heymate.payment.BankTransferResultActivity;
import org.telegram.ui.Heymate.payment.HeymatePayment;
import org.telegram.ui.Heymate.payment.PaymentMethodSelectionActivity;
import org.telegram.ui.LaunchActivity;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import works.heymate.core.Utils;

public class HeymateRouter {

    private static final String SCHEME = "heymate";

    private static final Map<String, Class<? extends BaseFragment>> HOST_MAP = new HashMap<>();

    static {
        HOST_MAP.put(OffersActivity.HOST, OffersActivity.class);
        HOST_MAP.put(MyScheduleActivity.HOST, MyScheduleActivity.class);
        HOST_MAP.put(TimeSlotSelectionActivity.HOST, TimeSlotSelectionActivity.class);
        HOST_MAP.put(PaymentMethodSelectionActivity.HOST, PaymentMethodSelectionActivity.class);
        HOST_MAP.put(BankTransferInformationActivity.HOST, BankTransferInformationActivity.class);
        HOST_MAP.put(BankTransferResultActivity.HOST, BankTransferResultActivity.class);
    }

    public static Intent createIntent(Context context, String host, Bundle args) {
        Intent intent = new Intent(context, LaunchActivity.class);

        Uri data = Uri.parse(SCHEME + "://" + host + "/");
        intent.setData(data);

        if (args != null) {
            intent.putExtras(args);
        }

        return intent;
    }

    public static boolean handleIntent(LaunchActivity activity, Intent intent) {
        if (intent != null && intent.getData() != null) {
            if ("celo".equalsIgnoreCase(intent.getData().getScheme())) {
                activity.presentFragment(new AttestationActivity(intent.getData().toString()));
                return true;
            }

            if (SCHEME.equalsIgnoreCase(intent.getData().getScheme())) {
                String host = intent.getData().getHost();

                Class<? extends BaseFragment> clazz = HOST_MAP.get(host);
                Bundle args = intent.getExtras();

                BaseFragment fragment;

                if (args == null || args.isEmpty()) {
                    fragment = newFragmentInstance(clazz);
                }
                else {
                    fragment = newFragmentInstance(clazz, args);
                }

                if (fragment != null) {
                    Utils.postOnUIThread(() -> activity.presentFragment(fragment, false, true));
                }

                return true;
            }

            if (HeymatePayment.RAMP_SCHEME.equalsIgnoreCase(intent.getData().getScheme())) {
                return true;
            }
        }

        return false;
    }

    private static BaseFragment newFragmentInstance(Class<? extends BaseFragment> clazz) {
        try {
            return clazz.newInstance();
        } catch (Throwable t) {
            return null;
        }
    }

    private static BaseFragment newFragmentInstance(Class<? extends BaseFragment> clazz, Bundle args) {
        try {
            Constructor<? extends BaseFragment> constructor = clazz.getConstructor(Bundle.class);

            return constructor.newInstance(args);
        } catch (Throwable t) {
            return null;
        }
    }

}
