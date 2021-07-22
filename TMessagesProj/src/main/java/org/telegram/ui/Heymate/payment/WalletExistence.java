package org.telegram.ui.Heymate.payment;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.log.LogToGroup;

import works.heymate.core.HeymateEvents;
import works.heymate.core.wallet.Security;
import works.heymate.core.wallet.Wallet;

public class WalletExistence {

    public static final String RAMP_SCHEME = "heymate-ramp";
    private static final String RAMP_RETURN_URL = RAMP_SCHEME + "://result/";

    private static final KeeperObserver sObserver = new KeeperObserver();

    public static void ensure(Runnable runnable) {
        Wallet wallet = Wallet.get(ApplicationLoader.applicationContext, TG2HM.getCurrentPhoneNumber());

        if (wallet.isCreated()) {
            runnable.run();
        }
        else {
            LoadingUtil.onLoadingStarted();
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
