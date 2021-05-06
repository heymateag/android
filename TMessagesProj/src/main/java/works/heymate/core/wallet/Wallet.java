package works.heymate.core.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.AmplifyModels.Offer;
import org.telegram.ui.Heymate.AmplifyModels.TimeSlot;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import works.heymate.celo.BuildConfig;
import works.heymate.celo.CeloAccount;
import works.heymate.celo.CeloContext;
import works.heymate.celo.CeloError;
import works.heymate.celo.CeloException;
import works.heymate.celo.CeloOffer;
import works.heymate.celo.CeloSDK;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Utils;

public class Wallet {

    private static final String TAG = "Wallet";

    private static final String OFFERS_ON_ALFAJORES = "0xaFA23794A154F8f0fB00067bD85DED04349FbAd0";
    private static final String OFFERS_ON_MAINNET = "0x13A9E83E2e4367B453F806824531f174b02095Fe";

    private static final CeloContext CELO_CONTEXT = BuildConfig.DEBUG ? CeloContext.ALFAJORES : CeloContext.MAIN_NET;
    private static final String OFFER_ADDRESS = BuildConfig.DEBUG ? OFFERS_ON_ALFAJORES : OFFERS_ON_MAINNET;

    private static final String PREFERENCES = "heymate_celo_1"; // TODO clear preferences name
    private static final String KEY_PUBLIC_KEY = "public_key";
    private static final String KEY_PRIVATE_KEY = "private_key";

    private static final Handler mHandler;

    static {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    private static Map<String, Wallet> mWallets = new Hashtable<>();

    public static Wallet get(Context context, String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + phoneNumber;
        }

        Wallet wallet = mWallets.get(phoneNumber);

        if (wallet == null) {
            wallet = new Wallet(context.getApplicationContext(), phoneNumber);

            mWallets.put(phoneNumber, wallet);
        }

        return mWallets.get(phoneNumber);
    }

    private final Context mContext;
    private final SharedPreferences mPreferences;
    private final String mPhoneNumber;

    private boolean mCreating = false;

    private CeloSDK mCeloSDK = null;
    private CeloOffer mCeloOffer = null;

    private boolean mCheckingVerifiedStatus = false;
    private VerifiedStatus mVerifiedStatus = null;

    private Wallet(Context context, String phoneNumber) {
        mContext = context;
        mPreferences = context.getSharedPreferences(PREFERENCES + keyFromPhoneNumber(phoneNumber), Context.MODE_PRIVATE);

        mPhoneNumber = phoneNumber;
    }

    SharedPreferences getPreferences() {
        return mPreferences;
    }

    public boolean isCreated() {
        return mPreferences.contains(KEY_PRIVATE_KEY);
    }

    public boolean isCreating() {
        return mCreating;
    }

    public boolean isCreatedOrCreating() {
        return isCreated() || mCreating;
    }

    public void createNew() {
        if (isCreated()) {
            throw new IllegalStateException("Wallet already created.");
        }

        if (mCreating) {
            throw new IllegalStateException("Wallet is already being created");
        }

        mCreating = true;

        mHandler.post(() -> {
            CeloAccount account = CeloAccount.randomAccount();

            mPreferences.edit()
                    .putString(KEY_PRIVATE_KEY, account.privateKey)
                    .putString(KEY_PUBLIC_KEY, account.publicKey)
                    .apply();

            Utils.runOnUIThread(() -> {
                mCreating = false;
                HeymateEvents.notify(HeymateEvents.WALLET_CREATED, Wallet.this);
            });
        });
    }

    public boolean createFromMnemonic(String mnemonic) {
        if (isCreated()) {
            throw new IllegalStateException("Wallet already created.");
        }

        if (mCreating) {
            throw new IllegalStateException("Wallet is already being created");
        }

        mCreating = true;

        CeloAccount account = CeloAccount.fromMnemonic(mnemonic);

        if (account != null) {
            mPreferences.edit()
                    .putString(KEY_PRIVATE_KEY, account.privateKey)
                    .putString(KEY_PUBLIC_KEY, account.publicKey)
                    .apply();
        }

        mCreating = false;

        HeymateEvents.notify(HeymateEvents.WALLET_CREATED, Wallet.this);

        return account != null;
    }

    public boolean isCheckingVerifiedStatus() {
        return mCheckingVerifiedStatus;
    }

    public VerifiedStatus getVerifiedStatus() {
        return mVerifiedStatus;
    }

    public void updateVerifiedStatus() {
        if (mCheckingVerifiedStatus) {
            return;
        }

        mCheckingVerifiedStatus = true;

        ensureCeloSDK();

        mCeloSDK.lookupPhoneNumberOwnership(mPhoneNumber,
                (success, verified, completedAttestations, totalAttestations, remainingAttestations, errorCause) -> {
                    mCheckingVerifiedStatus = false;

                    if (success) {
                        mVerifiedStatus = new VerifiedStatus(verified, completedAttestations, totalAttestations, remainingAttestations);
                    }
                    else {
                        mVerifiedStatus = null;

                        Log.e(TAG, "Failed to get verified status", errorCause);
                    }

                    HeymateEvents.notify(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, Wallet.this, mVerifiedStatus, errorCause);
        });
    }

    public void requestAttestations(AttestationRequestCallback callback) {
        if (mVerifiedStatus != null && mVerifiedStatus.verified) {
            throw new IllegalStateException("Phone number already verified.");
        }

        ensureCeloSDK();

        mCeloSDK.requestAttestationsForPhoneNumber(mPhoneNumber,
                (countsAreReliable, newAttestations, totalAttestations, completedAttestations, errorCause) -> {
            if (countsAreReliable) {
                boolean verified = mVerifiedStatus != null && mVerifiedStatus.verified;
                int remainingAttestations = totalAttestations - completedAttestations - newAttestations;
                mVerifiedStatus = new VerifiedStatus(verified, completedAttestations, totalAttestations, remainingAttestations);

                HeymateEvents.notify(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, Wallet.this, mVerifiedStatus, null);
            }

            boolean requiresMore = !countsAreReliable || mVerifiedStatus.remainingAttestations > 0;

            callback.onAttestationRequestResult(Wallet.this, newAttestations, requiresMore, errorCause);
        });
    }

    public void completeAttestation(String code, AttestationCompletionCallback callback) {
        ensureCeloSDK();

        mCeloSDK.completeAttestationForPhoneNumber(mPhoneNumber, code, (verified, completed, total, remaining, errorCause) -> {
            if (errorCause == null) {
                mVerifiedStatus = new VerifiedStatus(verified, completed, total, remaining);
                HeymateEvents.notify(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, Wallet.this, mVerifiedStatus, null);

                callback.attestationCompletionResult(true, verified, null);
            }
            else {
                callback.attestationCompletionResult(false, mVerifiedStatus != null && mVerifiedStatus.verified, errorCause);
            }
        });
    }

    public void signOffer(String rate, JSONObject termsConfig, SignatureCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    String signature = mCeloOffer.createOfferSignature(mCeloSDK.getAddress(), rate, termsConfig);

                    Utils.runOnUIThread(() -> callback.onSignResult(true, signature, null));
                } catch (Exception e) {
                    Utils.runOnUIThread(() -> callback.onSignResult(false, null, e));
                }
            }
            else {
                callback.onSignResult(false, null, errorCause);
            }
        });
    }

    public void createAcceptedOffer(Offer offer, TimeSlot timeSlot, List<String> referrers, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.create(offer, getAddress(), timeSlot, referrers);

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                } catch (JSONException e) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
        });
    }

    public void startOffer(Offer offer, TimeSlot timeSlot, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.startService(offer, timeSlot, getAddress());

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                }
            }
        });
    }

    public void finishOffer(Offer offer, TimeSlot timeSlot, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.finishService(offer, timeSlot, getAddress());

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                }
            }
        });
    }

    public void cancelOffer(Offer offer, TimeSlot timeSlot, boolean consumerCancelled, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.cancelService(offer, timeSlot, getAddress(), consumerCancelled);

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                }
            }
        });
    }

    public void getBalance(BalanceCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getBalance((success, rawCUSD, rawGold, cUSDCents, gold, errorCause) -> {
            if (success) {
                callback.onBalanceQueryResult(true, cUSDCents, null);
            }
            else {
                callback.onBalanceQueryResult(false, 0, errorCause);
            }
        });
    }

    private void ensureCeloSDK() {
        if (mCeloSDK == null) {
            mCeloSDK = new CeloSDK(mContext, CELO_CONTEXT, getAccount());
        }
    }

    private CeloAccount getAccount() {
        if (!isCreated()) {
            throw new IllegalStateException("Wallet does not exist.");
        }

        String privateKey = mPreferences.getString(KEY_PRIVATE_KEY, null);
        String publicKey = mPreferences.getString(KEY_PUBLIC_KEY, null);

        return new CeloAccount(privateKey, publicKey);
    }

    public String getAddress() {
        ensureCeloSDK();

        return mCeloSDK.getAddress();
    }

    public String getMnemonic() {
        if (isCreating()) {
            return null;
        }

        return getAccount().getMnemonic();
    }

    private static String keyFromPhoneNumber(String phoneNumber) {
        return phoneNumber.substring(1);
    }

}
