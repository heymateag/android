package org.telegram.ui.Heymate.payment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Log;

import org.telegram.ui.Heymate.ActivityMonitor;
import org.telegram.ui.Heymate.Constants;
import org.telegram.ui.Heymate.HeymateRouter;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.ReferralUtils;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.TimeSlotSelectionActivity;
import org.telegram.ui.Heymate.myschedule.MyScheduleActivity;
import org.telegram.ui.LaunchActivity;

import java.util.List;
import java.util.UUID;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.api.APIs;
import works.heymate.celo.CeloError;
import works.heymate.celo.CeloException;
import works.heymate.celo.CeloSDK;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Texts;
import works.heymate.core.Utils;
import works.heymate.core.offer.PurchasePlanTypes;
import works.heymate.core.wallet.Wallet;
import works.heymate.model.Offer;
import works.heymate.model.PurchasedPlan;
import works.heymate.model.TSReservation;
import works.heymate.model.TimeSlot;
import works.heymate.model.User;
import works.heymate.model.Users;

public class PaymentController {

    private static final String TAG = "PaymentController";

    public static final int GAS_ADJUST_CENTS = 30;

    private static PaymentController mInstance = null;

    public static PaymentController get(Context context) {
        if (mInstance == null) {
            mInstance = new PaymentController(context.getApplicationContext());
        }

        return mInstance;
    }

    private Context mContext;
    private SharedPreferences mPreferences;

    private boolean mDoingPayment = false;

    private PaymentController(Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public boolean isDoingPayment() {
        return mDoingPayment;
    }

    private void onPaymentStarted() {
        mDoingPayment = true;

        PaymentNotifierService.start(mContext);

        Activity activity = ActivityMonitor.get().getCurrentActivity();

        if (activity instanceof LaunchActivity) {
            ((LaunchActivity) activity).showHeader(new PendingPaymentHeader(mContext));
        }
    }

    private void onPaymentFinished() {
        mDoingPayment = false;

        PaymentNotifierService.stop(mContext);

        Activity activity = ActivityMonitor.get().getCurrentActivity();

        if (activity instanceof LaunchActivity) {
            ((LaunchActivity) activity).hideHeader();
        }
    }

    public void initPayment(String offerId, String purchasedPlanType, String referralId) {
        if (PurchasePlanTypes.SINGLE.equals(purchasedPlanType)) {
            purchaseTimeSlot(offerId, null, referralId);
        }
        else {
            getOffer(offerId, offer -> getPurchasedPlan(offerId, purchasedPlanType, purchasedPlan -> {
                    if (purchasedPlan != null) {
                        if (PurchasePlanTypes.SUBSCRIPTION.equals(purchasedPlan.getString(PurchasedPlan.PLAN_TYPE))) {
                            // TODO Must be checked with the latest subscription of the user not the first one in the server return order.
                            // TODO Must be checked in the first place!
//                            if (System.currentTimeMillis() - purchasedPlan.getPurchaseTime().toDate().getTime() < 30L * 24L * 60L * 60L * 1000L) {
//                                Toast.makeText(mContext, "You already have a subscription plan for this offer", Toast.LENGTH_LONG).show();
//                                return;
//                            }
                        }
                        else if (PurchasePlanTypes.BUNDLE.equals(purchasedPlan.getString(PurchasedPlan.PLAN_TYPE))) {
                            // TODO checks
//                            int doneReservations = purchasedPlan.getPendingReservationsCount() + purchasedPlan.getFinishedReservationsCount();
//
//                            int totalReservations;
//                            try {
//                                totalReservations = new PricingInfo(new JSONObject(offer.getPricingInfo())).bundleCount;
//                            } catch (JSONException e) {
//                                totalReservations = 0;
//                            }
//
//                            int remainingReservations = totalReservations - doneReservations;
//
//                            if (remainingReservations > 0) {
//                                Toast.makeText(mContext, "You already have " + remainingReservations + " bundle reservations remaining for this offer", Toast.LENGTH_LONG).show();
//                                return;
//                            }
                        }

                        getBalance((wallet, usdBalance, eurBalance, realBalance) -> {
                            Money price = PurchasePlanTypes.getPurchasedPlanPrice(offer, purchasedPlanType).plus(GAS_ADJUST_CENTS);
                            Money balance = getBalance(usdBalance, eurBalance, realBalance, price.getCurrency());

                            if (balance.compareTo(price) >= 0) {
                                getReferral(referralId, referral -> initPlanPurchasePayment(offer, purchasedPlanType, referral, wallet));
                            }
                            else {
                                mPreferences.edit()
                                        .putString(Constants.OFFER_ID, offerId)
                                        .putString(Constants.PURCHASED_PLAN_TYPE, purchasedPlanType)
                                        .putString(Constants.REFERRAL_ID, referralId)
                                        .apply();

                                ActivityMonitor.get().getCurrentActivity().startActivity(PaymentInvoiceActivity.getIntent(mContext, offerId, purchasedPlanType, balance));
                            }
                        });
                    }
                }));
        }
    }

    private void initPlanPurchasePayment(APIObject offer, String purchasedPlanType, APIObject referral, Wallet wallet) {
        // TODO purchase plan
//        PurchasedPlan purchasedPlan = PurchasedPlan.builder()
//                .planType(purchasedPlanType)
//                .offerId(offer.getId())
//                .serviceProviderId(offer.getUserId())
//                .consumerId(String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId))
//                .finishedReservationsCount(0)
//                .pendingReservationsCount(0)
//                .totalReservationsCount(0)
//                .purchaseTime(new Temporal.Date(new Date(System.currentTimeMillis())))
//                .reservationIds("[]")
//                .build();
//
//        List<String> referrers = ReferralUtils.getReferrersFromReferral(referral);
//
//        onPaymentStarted();
//
//        wallet.createPaymentPlan(offer, purchasedPlan, referrers, (success, errorCause) -> {
//            if (success) {
//                HtAmplify.getInstance(mContext).createPurchasedPlan(purchasedPlan, (success1, result, exception) -> {
//                    onPaymentFinished();
//
//                    if (success1) {
//                        Toast.makeText(mContext, "Plan purchase successful", Toast.LENGTH_LONG).show();
////                        new AlertDialog.Builder(mContext) // TODO Text resource
////                                .setTitle(PurchasePlanTypes.BUNDLE.equals(purchasedPlan.getPlanType()) ? "Bundle purchased" : "subscription purchased")
////                                .setMessage("You can check the state of your purchase in My Schedule.")
////                                .setCancelable(false)
////                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
////                                .show();
//                    }
//                    else {
//                        Log.e("TAG", "Failed to create bundle on the back-end.", exception);
//
//                        Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//            else {
//                Log.e(TAG, "Failed to create bundle on blockchain", errorCause);
//
//                onPaymentFinished();
//
//                handleBlockChainError(errorCause);
//            }
//        });
    }

    public void purchaseTimeSlot(String offerId, String purchasedPlanId, String referralId) {
        mPreferences.edit()
                .putString(Constants.OFFER_ID, offerId)
                .putString(Constants.PURCHASED_PLAN_TYPE, PurchasePlanTypes.SINGLE)
                .putString(Constants.PURCHASED_PLAN_ID, purchasedPlanId)
                .putString(Constants.REFERRAL_ID, referralId)
                .apply();

        ActivityMonitor.get().getCurrentActivity().startActivity(TimeSlotSelectionActivity.getIntent(mContext, offerId));
    }

    public void resumeTimeSlotPurchase(APIObject timeSlot) {
        String offerId = mPreferences.getString(Constants.OFFER_ID, null);
        String purchasedPlanType = mPreferences.getString(Constants.PURCHASED_PLAN_TYPE, null);
        String purchasedPlanId = mPreferences.getString(Constants.PURCHASED_PLAN_ID, null);
        String referralId = mPreferences.getString(Constants.REFERRAL_ID, null);

        if (offerId == null || purchasedPlanType == null) {
            return;
        }

        getReservation(offerId, timeSlot.getString(TimeSlot.ID), reservation -> {
            if (reservation != null) {
                Toast.makeText(mContext, "You have already reserved this time slot.", Toast.LENGTH_LONG).show();
                return;
            }

            getOffer(offerId, offer -> getBalance((wallet, usd, eur, real) -> {
                Money price = PurchasePlanTypes.getPurchasedPlanTimeSlotPrice(offer, purchasedPlanType).plus(GAS_ADJUST_CENTS);
                Money balance = getBalance(usd, eur, real, price.getCurrency());

                if (balance.compareTo(price) >= 0) {
                    purchaseTimeSlot(offer, purchasedPlanId, referralId, timeSlot);
                }
                else {
                    mPreferences.edit()
                            .putString(Constants.TIMESLOT_ID, timeSlot.getString(TimeSlot.ID))
                            .apply();

                    ActivityMonitor.get().getCurrentActivity().startActivity(PaymentInvoiceActivity.getIntent(mContext, offerId, purchasedPlanType, balance));
                }
            }));
        });
    }

    private void purchaseTimeSlot(APIObject offer, String purchasedPlanId, String referralId, APIObject timeSlot) {
        getReferral(referralId, referral -> {
            if (purchasedPlanId == null) {
                purchaseTimeSlot(offer, null, referral, timeSlot);
            }
            else {
                getPurchasedPlan(purchasedPlanId, purchasedPlan -> purchaseTimeSlot(offer, purchasedPlan, referral, timeSlot));
            }
        });
    }

    private void purchaseTimeSlot(APIObject offer, APIObject purchasedPlan, APIObject referral, APIObject timeSlot) {
        onPaymentStarted();

        List<String> referrers = ReferralUtils.getReferrersFromReferral(referral);

        String tradeId = "0x" + UUID.randomUUID().toString().replaceAll("-", "");

        Wallet wallet = Wallet.get(mContext, TG2HM.getCurrentPhoneNumber());

        wallet.createAcceptedOffer(offer, timeSlot, tradeId, purchasedPlan, referrers, (success1, errorCause) -> {
            if (success1) {
                APIs.get().createReservation(offer.getString(Offer.ID), offer.getString(Offer.USER_ID), timeSlot.getString(TimeSlot.ID), tradeId, result -> {
                    onPaymentFinished();

                    if (result.success) {
                        if (purchasedPlan != null) { // TODO purchased plan
//                            PurchasedPlan.BuildStep modifiedPurchasedPlan = purchasedPlan.copyOfBuilder();
//
//                            try {
//                                JSONArray reservationIds = new JSONArray(purchasedPlan.getReservationIds());
//                                reservationIds.put(reservation.getId());
//                                modifiedPurchasedPlan.reservationIds(reservationIds.toString());
//                            } catch (JSONException e) { }
//
//                            modifiedPurchasedPlan.totalReservationsCount(purchasedPlan.getTotalReservationsCount() + 1);
//                            modifiedPurchasedPlan.pendingReservationsCount(purchasedPlan.getPendingReservationsCount() + 1);
//
//                            HtAmplify.getInstance(mContext).createOrUpdatePurchasedPlan(modifiedPurchasedPlan.build(), null);
                        }

                        Toast.makeText(mContext, "Offer purchase successful", Toast.LENGTH_LONG).show();

                        if (ActivityMonitor.get().getCurrentActivity() != null) {
                            Intent intent = HeymateRouter.createIntent(mContext, MyScheduleActivity.HOST, MyScheduleActivity.createBundle(MyScheduleActivity.MY_ORDERS));
                            ActivityMonitor.get().getCurrentActivity().startActivity(intent);
                        }
                    }
                    else {
                        Log.e(TAG, "Failed to book time slot on the back-end.", result.error);
                        Toast.makeText(mContext, Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Log.e(TAG, "Failed to create offer on blockchain", errorCause);

                onPaymentFinished();

                handleBlockChainError(errorCause);
            }
        });
    }

    public void resumePayment() {
        String offerId = mPreferences.getString(Constants.OFFER_ID, null);
        String purchasedPlanType = mPreferences.getString(Constants.PURCHASED_PLAN_TYPE, null);
        String purchasedPlanId = mPreferences.getString(Constants.PURCHASED_PLAN_ID, null);
        String referralId = mPreferences.getString(Constants.REFERRAL_ID, null);
        String timeSlotId = mPreferences.getString(Constants.TIMESLOT_ID, null);

        if (offerId == null || purchasedPlanType == null) {
            return;
        }

        mPreferences.edit().clear().apply();

        if (timeSlotId == null) {
            initPayment(offerId, purchasedPlanType, referralId);
        }
        else {
            getOffer(offerId, offer -> {
                LoadingUtil.onLoadingStarted();

                APIs.get().getTimeSlot(timeSlotId, result -> {
                    LoadingUtil.onLoadingFinished();

                    if (result.success) {
                        purchaseTimeSlot(offer, purchasedPlanId, referralId, result.response);
                    }
                    else {
                        Log.e(TAG, "Failed to get time slot", result.error);

                        Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }

    private void getBalance(GetBalanceCallback callback) {
        LoadingUtil.onLoadingStarted();

        WalletExistence.ensure(() -> {
            String phoneNumber = TG2HM.getCurrentPhoneNumber();
            Wallet wallet = Wallet.get(mContext, phoneNumber);

            wallet.getBalance((success, usdBalance, eurBalance, realBalance, errorCause) -> {
                LoadingUtil.onLoadingFinished();

                if (success || CeloSDK.isErrorCausedByInsufficientFunds(errorCause)) {
                    callback.onBalanceReceived(wallet, usdBalance, eurBalance, realBalance);
                }
                else {
                    Log.e(TAG, "Failed to check balance", errorCause);

                    Toast.makeText(mContext, Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private Money getBalance(Money usd, Money eur, Money real, Currency currency) {
        if (Currency.USD.equals(currency)) {
            return usd;
        }

        if (Currency.EUR.equals(currency)) {
            return eur;
        }

        if (Currency.REAL.equals(currency)) {
            return real;
        }

        return Money.create(0, currency);
    }

    private void getOffer(String offerId, GetOfferCallback callback) {
        LoadingUtil.onLoadingStarted();

        APIs.get().getOffer(offerId, result -> {
            LoadingUtil.onLoadingFinished();

            if (result.response != null) {
                callback.onOfferReady(result.response);
            }
            else {
                Log.e(TAG, "Failed to get offer with id " + offerId, result.error);

                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getReferral(String referralId, GetReferralCallback callback) {
//        if (referralId == null) { TODO implement referral!
            callback.onReferralReady(null);
//            return;
//        }
//
//        LoadingUtil.onLoadingStarted();
//
//        HtAmplify amplify = HtAmplify.getInstance(mContext);
//
//        amplify.getReferralInfo(referralId, ((success, referral, exception) -> {
//            LoadingUtil.onLoadingFinished();
//
//            if (success) {
//                callback.onReferralReady(referral);
//            }
//            else {
//                Log.e(TAG, "Failed to get referral with id " + referralId, exception);
//
//                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
//            }
//        }));
    }

    private void getPurchasedPlan(String purchasedPlanId, GetPurchasedPlanCallback callback) {
        LoadingUtil.onLoadingStarted();

        APIs.get().getPurchasedPlan(purchasedPlanId, result -> {
            LoadingUtil.onLoadingFinished();

            if (result.success) {
                callback.onPurchasedPlanReady(result.response);
            }
            else {
                Log.e(TAG, "Failed to get purchased plan with id " + purchasedPlanId, result.error);

                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPurchasedPlan(String offerId, String type, GetPurchasedPlanCallback callback) {
        // TODO really look for purchased plan
        Utils.postOnUIThread(() -> callback.onPurchasedPlanReady(null));
//        LoadingUtil.onLoadingStarted();
//
//        HtAmplify amplify = HtAmplify.getInstance(mContext);
//
//        amplify.getPurchasedPlans(offerId, (success, result, exception) -> {
//            LoadingUtil.onLoadingFinished();
//
//            if (result != null) {
//                PurchasedPlan target = null;
//
//                for (PurchasedPlan purchasedPlan: result) {
//                    if (type.equals(purchasedPlan.getPlanType())) {
//                        target = purchasedPlan;
//                        break;
//                    }
//                }
//
//                callback.onPurchasedPlanReady(target);
//            }
//            else {
//                Log.e(TAG, "Failed to get purchased plans for offer id " + offerId, exception);
//
//                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
//            }
//        });
    }

    private void getReservation(String offerId, String timeSlotId, GetReservationCallback callback) {
        LoadingUtil.onLoadingStarted();

        APIs.get().getTimeSlotReservations(timeSlotId, result -> {
            LoadingUtil.onLoadingFinished();

            if (result.success) {
                String userId = Users.currentUser == null ? null : Users.currentUser.getString(User.ID);

                APIArray reservations = result.response.getArray("data");

                APIObject reservation = null;

                for (int i = 0; i < reservations.size(); i++) {
                    APIObject tsReservation = reservations.getObject(i);

                    if (userId.equals(tsReservation.getString(TSReservation.USER_ID))) {
                        reservation = tsReservation;
                        break;
                    }
                }

                // Practice caution here. TSReservation is different from Reservation.
                callback.onReservationReady(reservation);
            }
            else {
                Log.e(TAG, "Failed to query specific reservation", result.error);

                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }


        });
    }

    private void handleBlockChainError(CeloException errorCause) {
        if (errorCause != null) {
            CeloError coreError = errorCause.getMainCause().getError();

            if (coreError == CeloError.INSUFFICIENT_BALANCE) {
                Toast.makeText(mContext, Texts.get(Texts.INSUFFICIENT_BALANCE), Toast.LENGTH_LONG).show();
            }
            else if (coreError == CeloError.NETWORK_ERROR) {
                Toast.makeText(mContext, Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(mContext, Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(mContext, Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
        }
    }

    private interface GetBalanceCallback {

        void onBalanceReceived(Wallet wallet, Money usd, Money eur, Money real);

    }

    private interface GetOfferCallback {

        void onOfferReady(APIObject offer);

    }

    private interface GetReferralCallback {

        void onReferralReady(APIObject referral);

    }

    private interface GetPurchasedPlanCallback {

        void onPurchasedPlanReady(APIObject purchasedPlan);

    }

    private interface GetReservationCallback {

        void onReservationReady(APIObject reservation);

    }

}
