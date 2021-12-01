package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.trustwallet.walletconnect.models.session.WCSession;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.myschedule.MyScheduleActivity;
import org.telegram.ui.Heymate.offer.OfferDetailsActivity;
import org.telegram.ui.Heymate.payment.BankTransferInformationActivity;
import org.telegram.ui.Heymate.payment.BankTransferResultActivity;
import org.telegram.ui.Heymate.payment.WalletExistence;
import org.telegram.ui.Heymate.payment.PaymentInvoiceActivity;
import org.telegram.ui.Heymate.payment.PaymentMethodSelectionActivity;
import org.telegram.ui.LaunchActivity;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;
import works.heymate.walletconnect.WalletConnection;

public class HeymateRouter {

    private static final String INTERNAL_SCHEME = "heymate";

    private static final String EXTERNAL_HOST = "heymate.works";

    private static final Map<String, Class<? extends BaseFragment>> HOST_MAP = new HashMap<>();
    private static final Map<String, Class<? extends BaseFragment>> EXTERNAL_PATH_MAP = new HashMap<>();

    static {
        HOST_MAP.put(OffersActivity.HOST, OffersActivity.class);
        HOST_MAP.put(MyScheduleActivity.HOST, MyScheduleActivity.class);
        HOST_MAP.put(TimeSlotSelectionActivity.HOST, TimeSlotSelectionActivity.class);
        HOST_MAP.put(PaymentInvoiceActivity.HOST, PaymentInvoiceActivity.class);
        HOST_MAP.put(PaymentMethodSelectionActivity.HOST, PaymentMethodSelectionActivity.class);
        HOST_MAP.put(BankTransferInformationActivity.HOST, BankTransferInformationActivity.class);
        HOST_MAP.put(BankTransferResultActivity.HOST, BankTransferResultActivity.class);

        EXTERNAL_PATH_MAP.put(OfferDetailsActivity.PATH, OfferDetailsActivity.class);
    }

    public static Intent createIntent(Context context, String host, Bundle args) {
        Intent intent = new Intent(context, LaunchActivity.class);

        Uri data = Uri.parse(INTERNAL_SCHEME + "://" + host + "/");
        intent.setData(data);

        if (args != null) {
            intent.putExtras(args);
        }

        return intent;
    }

    public static boolean handleIntent(LaunchActivity activity, Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri uri = intent.getData();

            WCSession session = WalletConnection.sessionFromUri(uri.toString());

            if (session != null) {
                Wallet.get(activity, TG2HM.getCurrentPhoneNumber()).getConnection().connect(session);
                return true;
            }

            if ("celo".equalsIgnoreCase(uri.getScheme())) {
                activity.presentFragment(new AttestationActivity(uri.toString()));
                return true;
            }

            if (INTERNAL_SCHEME.equalsIgnoreCase(uri.getScheme())) {
                String host = uri.getHost();

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

            if (EXTERNAL_HOST.equalsIgnoreCase(uri.getHost()) && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                List<String> pathSegments = new ArrayList<>(uri.getPathSegments());

                if (!pathSegments.isEmpty()) {
                    String path = pathSegments.remove(0);

                    Class<? extends BaseFragment> clazz = EXTERNAL_PATH_MAP.get(path);

                    BaseFragment fragment = newFragmentInstance(clazz, pathSegments);

                    if (fragment != null) {
                        Utils.postOnUIThread(() -> activity.presentFragment(fragment, false, true));
                    }

                    return true;
                }
            }

            if (WalletExistence.RAMP_SCHEME.equalsIgnoreCase(intent.getData().getScheme())) {
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

    private static BaseFragment newFragmentInstance(Class<? extends BaseFragment> clazz, List<String> pathSegments) {
        try {
            Constructor<? extends BaseFragment> constructor = clazz.getConstructor(List.class);

            return constructor.newInstance(pathSegments);
        } catch (Throwable t) {
            return null;
        }
    }

}
