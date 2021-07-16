package org.telegram.ui.Heymate.payment;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.amplifyframework.datastore.generated.model.Referral;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.ReferralUtils;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.log.LogToGroup;

import java.util.ArrayList;
import java.util.List;

import works.heymate.core.HeymateEvents;
import works.heymate.core.wallet.Security;
import works.heymate.core.wallet.Wallet;

public class HeymatePayment {

    private static final String TAG = "HeymatePayment";

    public static final String RAMP_SCHEME = "heymate-ramp";
    private static final String RAMP_RETURN_URL = RAMP_SCHEME + "://result/";

    private static final KeeperObserver sObserver = new KeeperObserver();

    public static List<String> getReferrersFromReferral(Referral referral) {
        List<String> referrers = new ArrayList<>();

        if (referral != null) {
            String sReferrers = referral.getReferrers();

            if (sReferrers != null) {
                try {
                    JSONArray jReferrers = new JSONArray(sReferrers);

                    for (int i = 0; i < jReferrers.length(); i++) {
                        ReferralUtils.Referrer referrer = new ReferralUtils.Referrer(jReferrers.getJSONObject(i));

                        if (referrer.walletAddress != null) {
                            referrers.add(referrer.walletAddress);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to read the referrers from the referral.", e);
                }
            }
        }

        return referrers;
    }

    public static void ensureWalletExistence(Context context, Runnable runnable) {
        Wallet wallet = Wallet.get(ApplicationLoader.applicationContext, TG2HM.getCurrentPhoneNumber());

        if (wallet.isCreated()) {
            runnable.run();
        }
        else {
            LoadingUtil.onLoadingStarted(context);
            sObserver.task = () -> {
                LogToGroup.announceWallet(wallet);

                LoadingUtil.onLoadingFinished();
                runnable.run();
            };
            HeymateEvents.register(HeymateEvents.WALLET_CREATED, sObserver);
            wallet.createNew();
        }
    }

    private static class IntentLauncherFragment extends BaseFragment implements Security.IntentLauncher {

        private BaseFragment parent;

        private Intent intent;
        private Security.IntentResultReceiver resultReceiver;

        private IntentLauncherFragment(BaseFragment parent) {
            this.parent = parent;
        }

        @Override
        public View createView(Context context) {
            return new View(context);
        }

        @Override
        public void startIntentForResult(Intent intent, Security.IntentResultReceiver resultReceiver) {
            this.intent = intent;
            this.resultReceiver = resultReceiver;

            parent.presentFragment(this, false, true);
        }

        @Override
        public void onResume() {
            super.onResume();
            startActivityForResult(intent, 0);
        }

        @Override
        public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
            resultReceiver.onIntentResult(resultCode);

            finishFragment(false);
        }

    }

    private static class KeeperObserver implements HeymateEvents.HeymateEventObserver {

        private Runnable task = null;

        @Override
        public void onHeymateEvent(int event, Object... args) {
            HeymateEvents.unregister(event, this);

            if (task != null) {
                Runnable currentTask = task;
                task = null;

                currentTask.run();
            }
        }

    }

}
