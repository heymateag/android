package org.telegram.ui.Heymate.payment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.PurchasedPlan;
import com.amplifyframework.datastore.generated.model.Referral;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.amplifyframework.datastore.generated.model.TimeSlot;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Heymate.ActivityMonitor;
import org.telegram.ui.Heymate.Constants;
import org.telegram.ui.Heymate.HtAmplify;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.TimeSlotSelectionActivity;

import java.util.Date;
import java.util.List;

import works.heymate.celo.CeloError;
import works.heymate.celo.CeloException;
import works.heymate.celo.CeloSDK;
import works.heymate.core.APICallback;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Texts;
import works.heymate.core.offer.PurchasePlanTypes;
import works.heymate.core.wallet.Wallet;

public class PaymentController {

    private static final String TAG = "PaymentController";

    private static PaymentController mInstance = null;

    public static PaymentController get(Context context) {
        if (mInstance == null) {
            mInstance = new PaymentController(context.getApplicationContext());
        }

        return mInstance;
    }

    private Context mContext;
    private SharedPreferences mPreferences;

    private PaymentController(Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public void initPayment(String offerId, String purchasedPlanType, String referralId) {
        if (PurchasePlanTypes.SINGLE.equals(purchasedPlanType)) {
            purchaseTimeSlot(offerId, null, referralId);
        }
        else {
            getOffer(offerId, offer -> getBalance((wallet, balance) -> {
                long price = PurchasePlanTypes.getPurchasedPlanPrice(offer, purchasedPlanType) * 100;

                if (price <= balance) {
                    getReferral(referralId, referral -> initPlanPurchasePayment(offer, purchasedPlanType, referral, wallet));
                }
                else {
                    mPreferences.edit()
                            .putString(Constants.OFFER_ID, offerId)
                            .putString(Constants.PURCHASED_PLAN_TYPE, purchasedPlanType)
                            .putString(Constants.REFERRAL_ID, referralId)
                            .apply();

                    ActivityMonitor.get().getCurrentActivity().startActivity(PaymentMethodSelectionActivity.getIntent(mContext, Money.create((int) (price - balance + 10), Currency.EUR)));
                }
            }));
        }
    }

    private void initPlanPurchasePayment(Offer offer, String purchasedPlanType, Referral referral, Wallet wallet) {
        PurchasedPlan purchasedPlan = PurchasedPlan.builder()
                .planType(purchasedPlanType)
                .offerId(offer.getId())
                .serviceProviderId(offer.getUserId())
                .consumerId(String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId))
                .finishedReservationsCount(0)
                .pendingReservationsCount(0)
                .totalReservationsCount(0)
                .purchaseTime(new Temporal.Date(new Date(System.currentTimeMillis())))
                .reservationIds("[]")
                .build();

        List<String> referrers = HeymatePayment.getReferrersFromReferral(referral);

        LoadingUtil.onLoadingStarted(mContext);

        wallet.createPaymentPlan(offer, purchasedPlan, referrers, (success, errorCause) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {
                LoadingUtil.onLoadingStarted(mContext);

                HtAmplify.getInstance(mContext).createPurchasedPlan(purchasedPlan, (success1, result, exception) -> {
                    LoadingUtil.onLoadingFinished();

                    if (success1) {
                        new AlertDialog.Builder(mContext) // TODO Text resource
                                .setTitle(PurchasePlanTypes.BUNDLE.equals(purchasedPlan.getPlanType()) ? "Bundle purchased" : "subscription purchased")
                                .setMessage("You can check the state of your purchase in My Schedule.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                    else {
                        Log.e("TAG", "Failed to create bundle on the back-end.", exception);
                        Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Log.e(TAG, "Failed to create bundle on blockchain", errorCause);

                handleBlockChainError(errorCause);
            }
        });
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

    public void resumeTimeSlotPurchase(TimeSlot timeSlot) {
        String offerId = mPreferences.getString(Constants.OFFER_ID, null);
        String purchasedPlanType = mPreferences.getString(Constants.PURCHASED_PLAN_TYPE, null);
        String purchasedPlanId = mPreferences.getString(Constants.PURCHASED_PLAN_ID, null);
        String referralId = mPreferences.getString(Constants.REFERRAL_ID, null);

        if (offerId == null || purchasedPlanType == null) {
            return;
        }

        getOffer(offerId, offer -> getBalance((wallet, balance) -> {
            long price = PurchasePlanTypes.getPurchasedPlanTimeSlotPrice(offer, purchasedPlanType) * 100L;

            if (price <= balance) {
                purchaseTimeSlot(offer, purchasedPlanId, referralId, timeSlot);
            }
            else {
                mPreferences.edit()
                        .putString(Constants.TIMESLOT_ID, timeSlot.getId())
                        .apply();

                ActivityMonitor.get().getCurrentActivity().startActivity(PaymentMethodSelectionActivity.getIntent(mContext, Money.create((int) (price - balance + 10), Currency.EUR)));
            }
        }));
    }

    private void purchaseTimeSlot(Offer offer, String purchasedPlanId, String referralId, TimeSlot timeSlot) {
        getReferral(referralId, referral -> {
            if (purchasedPlanId == null) {
                purchaseTimeSlot(offer, null, referral, timeSlot);
            }
            else {
                getPurchasedPlan(purchasedPlanId, purchasedPlan -> purchaseTimeSlot(offer, purchasedPlan, referral, timeSlot));
            }
        });
    }

    private void purchaseTimeSlot(Offer offer, PurchasedPlan purchasedPlan, Referral referral, TimeSlot timeSlot) {
        LoadingUtil.onLoadingStarted(mContext);

        List<String> referrers = HeymatePayment.getReferrersFromReferral(referral);

        Reservation reservation = HtAmplify.getInstance(mContext).createReservation(timeSlot, purchasedPlan, referral);

        Wallet wallet = Wallet.get(mContext, TG2HM.getCurrentPhoneNumber());

        wallet.createAcceptedOffer(offer, reservation, purchasedPlan, referrers, (success1, errorCause) -> {
            LoadingUtil.onLoadingFinished();

            if (success1) {
                LoadingUtil.onLoadingStarted(mContext);

                HtAmplify.getInstance(mContext).bookTimeSlot(reservation, timeSlot, (success, result, exception) -> {
                    LoadingUtil.onLoadingFinished();

                    if (success) {
                        if (purchasedPlan != null) {
                            PurchasedPlan.BuildStep modifiedPurchasedPlan = purchasedPlan.copyOfBuilder();

                            try {
                                JSONArray reservationIds = new JSONArray(purchasedPlan.getReservationIds());
                                reservationIds.put(reservation.getId());
                                modifiedPurchasedPlan.reservationIds(reservationIds.toString());
                            } catch (JSONException e) { }

                            modifiedPurchasedPlan.totalReservationsCount(purchasedPlan.getTotalReservationsCount() + 1);
                            modifiedPurchasedPlan.pendingReservationsCount(purchasedPlan.getPendingReservationsCount() + 1);

                            HtAmplify.getInstance(mContext).createOrUpdatePurchasedPlan(modifiedPurchasedPlan.build(), null);
                        }

                        new AlertDialog.Builder(ActivityMonitor.get().getCurrentActivity()) // TODO Text resource
                                .setTitle("Offer accepted")
                                .setMessage("You can check the state of your offer in My Schedule.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                    else {
                        Log.e("TAG", "Failed to book time slot on the back-end.", exception);
                        Toast.makeText(mContext, Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Log.e(TAG, "Failed to create offer on blockchain", errorCause);

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
                LoadingUtil.onLoadingStarted(mContext);

                HtAmplify.getInstance(mContext).getTimeSlot(timeSlotId, (success, timeSlot, exception) -> {
                    LoadingUtil.onLoadingFinished();

                    if (success) {
                        purchaseTimeSlot(offer, purchasedPlanId, referralId, timeSlot);
                    }
                    else {
                        Log.e(TAG, "Failed to get time slot", exception);

                        Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }

    private void getBalance(GetBalanceCallback callback) {
        LoadingUtil.onLoadingStarted(mContext);

        HeymatePayment.ensureWalletExistence(mContext, () -> {
            String phoneNumber = TG2HM.getCurrentPhoneNumber();
            Wallet wallet = Wallet.get(mContext, phoneNumber);

            wallet.getBalance((success, cents, errorCause) -> {
                LoadingUtil.onLoadingFinished();

                if (success || CeloSDK.isErrorCausedByInsufficientFunds(errorCause)) {
                    callback.onBalanceReceived(wallet, cents);
                }
                else {
                    Log.e(TAG, "Failed to check balance", errorCause);

                    Toast.makeText(mContext, Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void getOffer(String offerId, GetOfferCallback callback) {
        LoadingUtil.onLoadingStarted(mContext);

        HtAmplify amplify = HtAmplify.getInstance(mContext);

        amplify.getOffer(offerId, ((success, offer, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (offer != null) {
                callback.onOfferReady(offer);
            }
            else {
                Log.e(TAG, "Failed to get offer with id " + offerId, exception);

                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void getReferral(String referralId, GetReferralCallback callback) {
        if (referralId == null) {
            callback.onReferralReady(null);
            return;
        }

        LoadingUtil.onLoadingStarted(mContext);

        HtAmplify amplify = HtAmplify.getInstance(mContext);

        amplify.getReferralInfo(referralId, ((success, referral, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {
                callback.onReferralReady(referral);
            }
            else {
                Log.e(TAG, "Failed to get referral with id " + referralId, exception);

                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void getPurchasedPlan(String purchasedPlanId, GetPurchasedPlanCallback callback) {
        LoadingUtil.onLoadingStarted(mContext);

        HtAmplify amplify = HtAmplify.getInstance(mContext);

        amplify.getPurchasedPlan(purchasedPlanId, ((success, purchasedPlan, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {
                callback.onPurchasedPlanReady(purchasedPlan);
            }
            else {
                Log.e(TAG, "Failed to get purchased plan with id " + purchasedPlanId, exception);

                Toast.makeText(mContext, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }
        }));
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

        void onBalanceReceived(Wallet wallet, long cents);

    }

    private interface GetOfferCallback {

        void onOfferReady(Offer offer);

    }

    private interface GetReferralCallback {

        void onReferralReady(Referral referral);

    }

    private interface GetPurchasedPlanCallback {

        void onPurchasedPlanReady(PurchasedPlan purchasedPlan);

    }

}
