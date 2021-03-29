package org.telegram.ui.Heymate;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.amplifyframework.api.graphql.PaginatedResult;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Heymate.AmplifyModels.Offer;
import org.telegram.ui.Heymate.AmplifyModels.TimeSlot;

import java.util.ArrayList;
import java.util.TimeZone;

import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;
import works.heymate.core.Utils;
import works.heymate.core.wallet.Security;
import works.heymate.core.wallet.VerifiedStatus;
import works.heymate.core.wallet.Wallet;

public class HeymatePayment {

    private static final String TAG = "HeymatePayment";

    private static final KeeperObserver sObserver = new KeeperObserver();

    private static int getStateTries = 3;

    public static void initPayment(BaseFragment fragment, String offerId) {
        HtAmplify amplify = HtAmplify.getInstance(fragment.getParentActivity());

        amplify.getOffer(offerId, ((success, offer, exception) -> {
            Utils.runOnUIThread(() -> {
                if (!success) {
                    if (exception != null) {
                        Log.e(TAG, "Failed to get offer with id " + offerId, exception);
                    }

                    Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                    return;
                }

                initPayment(fragment, (Offer) offer);
            });
        }));
    }

    private static void initPayment(BaseFragment fragment, Offer offer) {
        ArrayList<TimeSlot> timeSlots = HtAmplify.getInstance(fragment.getParentActivity()).getAvailableTimeSlots(offer.getId());

        JSONObject availabilitySlot;

        try {
            availabilitySlot = new JSONObject(offer.getAvailabilitySlot());
        } catch (Throwable t) {
            // TODO Wrong error
            Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            return;
        }

        if (timeSlots == null || timeSlots.isEmpty()) {
            try {
                JSONArray times = availabilitySlot.getJSONArray("times");

                if (timeSlots == null) {
                    timeSlots = new ArrayList<>();
                }

                for (int i = 0; i < times.length(); i++) {
                    String timeSlotStr = times.getString(i);
                    int index = timeSlotStr.indexOf(" - ");

                    if (index < 0) {
                        continue;
                    }

                    timeSlots.add(new TimeSlot.Builder()
                            .startTime((int) (Long.parseLong(timeSlotStr.substring(0, index)) / 1000))
                            .endTime((int) (Long.parseLong(timeSlotStr.substring(index + 3)) / 1000))
//                            .id("NO ID")
                            .status(HtTimeSlotStatus.AVAILABLE.ordinal())
                            .build()
                    );
                }
            } catch (Throwable t) { }
        }

        if (timeSlots == null) {
            Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            return;
        }

        TimeZone timeZone;

        try {
            timeZone = TimeZone.getTimeZone(availabilitySlot.getString("timeZone"));
        } catch (Throwable t) {
            Toast.makeText(fragment.getParentActivity(), "Bad TimeZone from offer: " + offer.getAvailabilitySlot(), Toast.LENGTH_LONG).show();
            return;
        }

        fragment.presentFragment(new TimeSlotSelectionActivity(timeZone, timeSlots, timeSlot -> {
            initPayment(fragment, offer, timeSlot);
        }));
    }

    private static void initPayment(BaseFragment fragment, Offer offer, TimeSlot timeSlot) {
        String phoneNumber = TG2HM.getCurrentPhoneNumber();

        Wallet wallet = Wallet.get(fragment.getParentActivity(), phoneNumber);

        if (!wallet.isCreated()) {
            fragment.presentFragment(new WalletActivity(() -> initPayment(fragment, offer, timeSlot)));
            return;
        }

        VerifiedStatus verifiedStatus = wallet.getVerifiedStatus();

        if (verifiedStatus == null) {
            if (getStateTries == 0) {
                getStateTries = 3;

                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                return;
            }

            getStateTries--;

            wallet.updateVerifiedStatus();

            sObserver.task = () -> initPayment(fragment, offer, timeSlot);
            HeymateEvents.register(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, sObserver);
            return;
        }

        if (!verifiedStatus.verified) {
            fragment.presentFragment(new AttestationActivity(() -> initPayment(fragment, offer, timeSlot)));
            return;
        }

        boolean needsSecuritySettings = !Security.ensureSecurity(
                (FragmentActivity) fragment.getParentActivity(),
                wallet,
                Texts.get(Texts.AUTHENTICATION),
                Texts.get(Texts.AUTHENTICATION_DESCRIPTION),
                new IntentLauncherFragment(fragment),
                () -> initPreparedPayment(fragment, offer, timeSlot));

        if (needsSecuritySettings) {
            fragment.presentFragment(new SecureWalletActivity(() -> initPayment(fragment, offer, timeSlot)));
        }
    }

    private static void initPreparedPayment(BaseFragment fragment, Offer offer, TimeSlot timeSlot) {
        // TODO
        new AlertDialog.Builder(fragment.getParentActivity())
                .setTitle("End Of Demo")
                .setMessage("Come back later?")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
//        String userId = String.valueOf(UserConfig.getInstance(fragment.getCurrentAccount()).clientUserId);
//        HtAmplify.getInstance(fragment.getParentActivity()).bookTimeSlot(timeSlot.getId(), userId);
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
