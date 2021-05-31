package org.telegram.ui.Heymate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.PurchasedPlan;
import com.amplifyframework.datastore.generated.model.Referral;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.amplifyframework.datastore.generated.model.TimeSlot;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import works.heymate.beta.BuildConfig;
import works.heymate.celo.CeloError;
import works.heymate.celo.CeloException;
import works.heymate.celo.CeloSDK;
import works.heymate.celo.CurrencyUtil;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;
import works.heymate.core.Utils;
import works.heymate.core.offer.PurchasePlanInfo;
import works.heymate.core.offer.PurchasePlanTypes;
import works.heymate.core.wallet.Security;
import works.heymate.core.wallet.Wallet;
import works.heymate.ramp.Ramp;

public class HeymatePayment {

    private static final String TAG = "HeymatePayment";

    public static final String RAMP_SCHEME = "heymate-ramp";
    private static final String RAMP_RETURN_URL = RAMP_SCHEME + "://result/";

    private static final KeeperObserver sObserver = new KeeperObserver();

    private static int getStateTries = 3;

    private static Offer savedOffer = null;
    private static PurchasedPlan savedPurchasedPlan = null;
    private static Referral savedReferral = null;
    private static TimeSlot savedTimeSlot = null;

    public static void initPayment(BaseFragment fragment, String offerId, PurchasePlanInfo purchasePlan, String referralId) {
        if (referralId == null) {
            initPayment(fragment, offerId, purchasePlan, (Referral) null);
            return;
        }

        LoadingUtil.onLoadingStarted(fragment.getParentActivity());

        HtAmplify amplify = HtAmplify.getInstance(fragment.getParentActivity());

        amplify.getReferralInfo(referralId, (success, result, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {
                initPayment(fragment, offerId, purchasePlan, result);
            }
            else {
                Log.e(TAG, "Failed to get referral with id " + offerId, exception);
                LogToGroup.log("Failed to get referral with id " + offerId, exception, fragment);

                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }

        });
    }

    private static void initPayment(BaseFragment fragment, String offerId, PurchasePlanInfo purchasePlan, Referral referral) {
        LoadingUtil.onLoadingStarted(fragment.getParentActivity());

        HtAmplify amplify = HtAmplify.getInstance(fragment.getParentActivity());

        amplify.getOffer(offerId, ((success, offer, exception) -> {
            Utils.runOnUIThread(() -> {
                LoadingUtil.onLoadingFinished();

                if (!success) {
                    Log.e(TAG, "Failed to get offer with id " + offerId, exception);
                    LogToGroup.log("Failed to get offer with id " + offerId, exception, fragment);

                    Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                    return;
                }

                initPayment(fragment, offer, purchasePlan, referral);
            });
        }));
    }

    private static void initPayment(BaseFragment fragment, Offer offer, PurchasePlanInfo purchasePlan, Referral referral) {
        PurchasedPlan purchasedPlan = PurchasedPlan.builder()
                .planType(purchasePlan.type)
                .offerId(offer.getId())
                .serviceProviderId(offer.getUserId())
                .consumerId(String.valueOf(UserConfig.getInstance(fragment.getCurrentAccount()).clientUserId))
                .finishedReservationsCount(0)
                .pendingReservationsCount(0)
                .totalReservationsCount(0)
                .purchaseTime(new Temporal.Date(new Date(System.currentTimeMillis())))
                .reservationIds("[]")
                .build();

        if (PurchasePlanTypes.SINGLE.equals(purchasePlan.type)) {
            purchaseTimeSlot(fragment, offer, purchasedPlan, referral);
            return;
        }

        int price = PurchasePlanTypes.getPurchasedPlanTimeSlotPrice(offer, purchasedPlan);

        ensureWalletExistenceWithLoading(fragment, getNetNext(fragment, price, () -> initPlanPurchasePayment(fragment, offer, purchasedPlan, referral)));
    }

    private static void initPlanPurchasePayment(BaseFragment fragment, Offer offer, PurchasedPlan purchasedPlan, Referral referral) {
        List<String> referrers = getReferrersFromReferral(referral);

        String phoneNumber = TG2HM.getCurrentPhoneNumber();

        Wallet wallet = Wallet.get(fragment.getParentActivity(), phoneNumber);

        LoadingUtil.onLoadingStarted(fragment.getParentActivity());

        wallet.createPaymentPlan(offer, purchasedPlan, referrers, (success, errorCause) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {
                LoadingUtil.onLoadingStarted(fragment.getParentActivity());

                HtAmplify.getInstance(fragment.getParentActivity()).createPurchasedPlan(purchasedPlan, (success1, result, exception) -> {
                    LoadingUtil.onLoadingFinished();

                    if (success1) {
                        new AlertDialog.Builder(fragment.getParentActivity()) // TODO Text resource
                                .setTitle(PurchasePlanTypes.BUNDLE.equals(purchasedPlan.getPlanType()) ? "Bundle purchased" : "subscription purchased")
                                .setMessage("You can check the state of your purchase in My Schedule.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                    else {
                        Log.e("TAG", "Failed to create bundle on the back-end.", exception);
                        Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Log.e(TAG, "Failed to create bundle on blockchain", errorCause);
                LogToGroup.log("Failed to create bundle on blockchain", errorCause, fragment);

                handleBlockChainError(fragment, errorCause);
            }
        });
    }

    private static void initSubscriptionPurchasePayment(BaseFragment fragment, Offer offer, PurchasedPlan purchasedPlan, Referral referral) {
        // TODO
    }

    public static void purchaseTimeSlot(BaseFragment fragment, Offer offer, PurchasedPlan purchasedPlan, Referral referral) {
        JSONObject availabilitySlot;

        try {
            availabilitySlot = new JSONObject(offer.getAvailabilitySlot());
        } catch (Throwable t) {
            // TODO Wrong error
            Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            return;
        }

        TimeZone timeZone = TimeZone.getDefault();

//        try {
//            timeZone = TimeZone.getTimeZone(availabilitySlot.getString("timeZone"));
//        } catch (Throwable t) {
//            Toast.makeText(fragment.getParentActivity(), "Bad TimeZone from offer: " + offer.getAvailabilitySlot(), Toast.LENGTH_LONG).show();
//            return;
//        }

        LoadingUtil.onLoadingStarted(fragment.getParentActivity());

        HtAmplify.getInstance(fragment.getParentActivity()).getTimeSlots(offer.getId(), (success, result, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {
                fragment.presentFragment(new TimeSlotSelectionActivity(timeZone, result, timeSlot -> {
                    ensureWalletExistenceWithLoading(fragment, () -> purchaseTimeSlot(fragment, offer, purchasedPlan, referral, timeSlot));
                }));
            }
            else {
                Log.e(TAG, "Failed to get time slots", exception);
                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void purchaseTimeSlot(BaseFragment fragment, Offer offer, PurchasedPlan purchasedPlan, Referral referral, TimeSlot timeSlot) {
        String phoneNumber = TG2HM.getCurrentPhoneNumber();

        Wallet wallet = Wallet.get(fragment.getParentActivity(), phoneNumber);

//        VerifiedStatus verifiedStatus = wallet.getVerifiedStatus();
//
//        if (verifiedStatus == null) {
//            if (getStateTries == 0) {
//                getStateTries = 3;
//
//                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
//                return;
//            }
//
//            getStateTries--;
//
//            wallet.updateVerifiedStatus();
//
//            sObserver.task = () -> purchaseTimeSlot(fragment, offer, purchasedPlan, referral, timeSlot);
//            HeymateEvents.register(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, sObserver);
//            return;
//        }
//
//        if (!verifiedStatus.verified) { // TODO uncomment
//            fragment.presentFragment(new AttestationActivity(() -> purchaseTimeSlot(fragment, offer, purchasedPlan, referral, timeSlot)));
//            return;
//        }

        int price = PurchasePlanTypes.getPurchasedPlanTimeSlotPrice(offer, purchasedPlan);

        Runnable next = getNetNext(fragment, price, () -> initTimeSlotPurchasePayment(fragment, offer, purchasedPlan, referral, timeSlot));

        boolean needsSecuritySettings = !Security.ensureSecurity(
                (FragmentActivity) fragment.getParentActivity(),
                wallet,
                Texts.get(Texts.AUTHENTICATION),
                Texts.get(Texts.AUTHENTICATION_DESCRIPTION),
                new IntentLauncherFragment(fragment),
                next);

        if (needsSecuritySettings) {
            fragment.presentFragment(new SecureWalletActivity(() -> purchaseTimeSlot(fragment, offer, purchasedPlan, referral, timeSlot)));
        }
    }

    private static Runnable getNetNext(BaseFragment fragment, int price, Runnable next) {
        if (price == 0) {
            return next;
        }

        return BuildConfig.DEBUG ?
                () -> alfajoresTopUp(fragment, next) :
                () -> checkBalanceBeforePayment(fragment, price, next);
//        return () -> checkBalanceBeforePayment(fragment, price, next);
    }

    private static void alfajoresTopUp(BaseFragment fragment, Runnable next) {
        String phoneNumber = TG2HM.getCurrentPhoneNumber();

        Wallet wallet = Wallet.get(fragment.getParentActivity(), phoneNumber);
        Context context = fragment.getParentActivity();
        TextView addressView = new TextView(context);
        addressView.setText(wallet.getAddress() + "\nhttps://celo.org/developers/faucet");
        addressView.setAutoLinkMask(Linkify.WEB_URLS);
        addressView.setTextIsSelectable(true);
        addressView.setMovementMethod(LinkMovementMethod.getInstance());
        new AlertDialog.Builder(context)
                .setTitle("Initiate payment?")
                .setView(addressView)
                .setPositiveButton("Go", (dialog, which) -> {
                    dialog.dismiss();
                    next.run();
                })
                .show();
    }

    private static void checkBalanceBeforePayment(BaseFragment fragment, int price, Runnable next) {
        org.telegram.ui.ActionBar.AlertDialog loadingDialog = new org.telegram.ui.ActionBar.AlertDialog(fragment.getParentActivity(), 3);
        loadingDialog.setCanCacnel(false);
        loadingDialog.show();

        Wallet wallet = Wallet.get(fragment.getParentActivity(), TG2HM.getCurrentPhoneNumber());

        wallet.getBalance((success, cents, errorCause) -> {
            loadingDialog.dismiss();

            if (success || CeloSDK.isErrorCausedByInsufficientFunds(errorCause)) {
                long amount = (long) (price * 100D);

                if (cents >= amount) {
                    next.run();
                }
                else {
                    long missingCents = amount - cents + 100;

                    new AlertDialog.Builder(fragment.getParentActivity())
                            .setTitle("Top up required")
                            .setMessage("You need to top up " + (missingCents / 100f) + " US Dollars to reserve this offer.")
                            .setCancelable(false)
                            .setPositiveButton("Top up", (dialog, which) -> {
                                dialog.dismiss();

                                // TODO Save no longer works.
                                // Save offer info
//                                savedOffer = offer;
//                                savedPurchasedPlan = purchasedPlan;
//                                savedReferral = referral
//                                savedTimeSlot = timeSlot;

                                String topUpAmount = CurrencyUtil.centsToBlockChainValue(missingCents).toString();

                                Ramp.getDialog(fragment.getParentActivity(), wallet.getAddress(), topUpAmount, () -> {
                                    checkBalanceBeforePayment(fragment, price, next);
                                }).show();
//                                Intent intent = Ramp.getTopUpIntent(wallet.getAddress(), topUpAmount, RAMP_RETURN_URL);
//                                intent = intent.createChooser(intent, "Top Up using");
//                                fragment.getParentActivity().startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            }
            else {
                Log.e(TAG, "Failed to check balance", errorCause);
                LogToGroup.log("Failed to check balance", errorCause, fragment);

                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static boolean resumePayment(BaseFragment fragment) {
        if (savedOffer != null && savedTimeSlot != null) {
            int price = PurchasePlanTypes.getPurchasedPlanTimeSlotPrice(savedOffer, savedPurchasedPlan);
            checkBalanceBeforePayment(fragment, price, () -> initTimeSlotPurchasePayment(fragment, savedOffer, savedPurchasedPlan, savedReferral, savedTimeSlot));
            savedOffer = null;
            savedPurchasedPlan = null;
            savedReferral = null;
            savedTimeSlot = null;
            return true;
        }
        return false;
    }

    private static void initTimeSlotPurchasePayment(BaseFragment fragment, Offer offer, PurchasedPlan purchasedPlan, Referral referral, TimeSlot timeSlot) {
        LoadingUtil.onLoadingStarted(fragment.getParentActivity());

        List<String> referrers = getReferrersFromReferral(referral);

        Reservation reservation = HtAmplify.getInstance(fragment.getParentActivity())
                .createReservation(timeSlot, purchasedPlan, referral);

        Wallet wallet = Wallet.get(fragment.getParentActivity(), TG2HM.getCurrentPhoneNumber());

        wallet.createAcceptedOffer(offer, reservation, purchasedPlan, referrers, (success1, errorCause) -> {
            LoadingUtil.onLoadingFinished();

            if (success1) {
                LoadingUtil.onLoadingStarted(fragment.getParentActivity());

                HtAmplify.getInstance(fragment.getParentActivity()).bookTimeSlot(reservation, timeSlot, (success, result, exception) -> {
                    LoadingUtil.onLoadingFinished();

                    if (success) {
                        PurchasedPlan.BuildStep modifiedPurchasedPlan = purchasedPlan.copyOfBuilder();

                        try {
                            JSONArray reservationIds = new JSONArray(purchasedPlan.getReservationIds());
                            reservationIds.put(reservation.getId());
                            modifiedPurchasedPlan.reservationIds(reservationIds.toString());
                        } catch (JSONException e) { }

                        modifiedPurchasedPlan.totalReservationsCount(purchasedPlan.getTotalReservationsCount() + 1);
                        modifiedPurchasedPlan.pendingReservationsCount(purchasedPlan.getPendingReservationsCount() + 1);

                        HtAmplify.getInstance(fragment.getParentActivity()).createOrUpdatePurchasedPlan(modifiedPurchasedPlan.build(), null);

                        new AlertDialog.Builder(fragment.getParentActivity()) // TODO Text resource
                                .setTitle("Offer accepted")
                                .setMessage("You can check the state of your offer in My Schedule.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                    else {
                        Log.e("TAG", "Failed to book time slot on the back-end.", exception);
                        Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Log.e(TAG, "Failed to create offer on blockchain", errorCause);
                LogToGroup.log("Failed to create offer on blockchain", errorCause, fragment);

                handleBlockChainError(fragment, errorCause);
            }
        });
    }

    private static void handleBlockChainError(BaseFragment fragment, CeloException errorCause) {
        if (errorCause instanceof CeloException) {
            CeloError coreError = errorCause.getMainCause().getError();

            if (coreError == CeloError.INSUFFICIENT_BALANCE) {
                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.INSUFFICIENT_BALANCE), Toast.LENGTH_LONG).show();
            }
            else if (coreError == CeloError.NETWORK_ERROR) {
                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(fragment.getParentActivity(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
        }
    }

    private static List<String> getReferrersFromReferral(Referral referral) {
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

    private static void ensureWalletExistenceWithLoading(BaseFragment fragment, Runnable runnable) {
        String phoneNumber = TG2HM.getCurrentPhoneNumber();

        Wallet wallet = Wallet.get(fragment.getParentActivity(), phoneNumber);

        if (!wallet.isCreated()) {
            LoadingUtil.onLoadingStarted(fragment.getParentActivity());
            ensureWalletExistence(fragment.getParentActivity(), () -> {
                LoadingUtil.onLoadingFinished();
                LogToGroup.announceWallet(fragment, wallet);
                runnable.run();
            });
        }
        else {
            runnable.run();
        }
    }

    public static void ensureWalletExistence(Context context, Runnable runnable) {
        Wallet wallet = Wallet.get(ApplicationLoader.applicationContext, TG2HM.getCurrentPhoneNumber());

        if (wallet.isCreated()) {
            runnable.run();
        }
        else {
            LoadingUtil.onLoadingStarted(context);
            sObserver.task = () -> {
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
