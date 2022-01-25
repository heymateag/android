package works.heymate.core.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.android.exoplayer2.util.Log;

import org.celo.contractkit.CeloContract;
import org.celo.contractkit.ContractKit;
import org.celo.contractkit.wrapper.StableTokenWrapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.HeymateConfig;

import works.heymate.api.APIObject;
import works.heymate.celo.AmountCallback;
import works.heymate.celo.ContractKitCallback;
import works.heymate.core.Currency;
import works.heymate.core.Money;

import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import works.heymate.celo.CeloAccount;
import works.heymate.celo.CeloContext;
import works.heymate.celo.CeloError;
import works.heymate.celo.CeloException;
import works.heymate.celo.CeloOffer;
import works.heymate.celo.CeloSDK;
import works.heymate.celo.CurrencyUtil;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Utils;
import works.heymate.core.offer.OfferUtils;
import works.heymate.model.Pricing;
import works.heymate.walletconnect.WalletConnection;

public class Wallet {

    private static final String TAG = "Wallet";

    private static final String OFFERS_ON_ALFAJORES = "0x54fB1CE9Fb9148860a07A2160b2F250D915930Fd";
    private static final String OFFERS_ON_MAINNET = "0xe71eEb0791A6F3E4705759fcD0bB95d15A5A6be1";

    public static final CeloContext CELO_CONTEXT = HeymateConfig.MAIN_NET ? CeloContext.MAIN_NET : CeloContext.ALFAJORES;
    private static final String OFFER_ADDRESS = HeymateConfig.MAIN_NET ? OFFERS_ON_MAINNET : OFFERS_ON_ALFAJORES;

    private static final String PREFERENCES = "heymate_celo";
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

    private WalletConnection mConnection = null;

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

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public WalletConnection getConnection() {
        if (mConnection == null) {
            mConnection = new WalletConnection(this);
        }

        return mConnection;
    }

    public boolean isCreated() {
        if (mPreferences.contains(KEY_PRIVATE_KEY)) {
            return true;
        }

        try {
            CeloAccount account = WalletSafe.restoreAccount(mPhoneNumber, mContext);

            mPreferences.edit()
                    .putString(KEY_PRIVATE_KEY, account.privateKey)
                    .putString(KEY_PUBLIC_KEY, account.publicKey)
                    .apply();

            return true;
        } catch (IOException e) {
            Log.w(TAG, "Failed to restore account.", e);
        }

        return false;
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

    public void calculatePrice(Money money, Currency targetCurrency, AmountCallback callback) {
        ensureCeloSDK();

        mCeloSDK.howMuchToBuy(works.heymate.celo.Money.get(money.getCents(), money.getCurrency().celoCurrency()), targetCurrency.celoCurrency(), callback);
    }

    public void signOffer(Pricing pricing, APIObject paymentTerms, SignatureCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.createOfferSignature(pricing, paymentTerms);

                    if (pricing.getBundleCount() > 0) {
                        mCeloOffer.createBundleSignature(pricing, 0); // TODO promotion
                    }

                    if (pricing.getSubscriptionPeriod() != null) {
                        mCeloOffer.createSubscriptionSignature(pricing, 0); // TODO promotion
                    }

                    Utils.runOnUIThread(() -> callback.onSignResult(true, null));
                } catch (Exception e) {
                    Utils.runOnUIThread(() -> callback.onSignResult(false, e));
                }
            }
            else {
                callback.onSignResult(false, errorCause);
            }
        });
    }

    public void createPaymentPlan(APIObject offer, APIObject purchasedPlan, List<String> referrers, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.createPaymentPlan(offer, purchasedPlan, referrers);

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                } catch (JSONException e) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
            else {
                Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, errorCause));
            }
        });
    }

    public void createAcceptedOffer(APIObject offer, APIObject timeSlot, String tradeId, APIObject purchasedPlan,
                                    List<String> referrers, Currency nativeCurrency,
                                    OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.create(offer, timeSlot, tradeId, purchasedPlan, referrers, nativeCurrency);

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                } catch (JSONException e) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
            else {
                Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, errorCause));
            }
        });
    }

    public void startOffer(APIObject offer, APIObject purchasedPlan, APIObject reservation, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.startService(offer, purchasedPlan, reservation, getAddress());

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                } catch (JSONException e) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
            else {
                Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, errorCause));
            }
        });
    }

    public void finishOffer(APIObject offer, APIObject purchasedPlan, APIObject reservation, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.finishService(offer, purchasedPlan, reservation, getAddress());

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                } catch (JSONException e) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
            else {
                Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, errorCause));
            }
        });
    }

    public void cancelOffer(APIObject offer, APIObject purchasedPlan, APIObject reservation, boolean consumerCancelled, OfferOperationCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                if (mCeloOffer == null) {
                    mCeloOffer = new CeloOffer(OFFER_ADDRESS, contractKit);
                }

                try {
                    mCeloOffer.cancelService(offer, purchasedPlan, reservation, getAddress(), consumerCancelled);

                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(true, null));
                } catch (CeloException exception) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, exception));
                } catch (JSONException e) {
                    Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
            else {
                Utils.runOnUIThread(() -> callback.onOfferOperationResult(false, errorCause));
            }
        });
    }

    public void getBalance(BalanceCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getBalance((success, rawCUSD, rawCEUR, rawCREAL, cUSDCents, cEURCents, cREALCents, errorCause) -> {
            if (success) {
                callback.onBalanceQueryResult(true, Money.create(cUSDCents, Currency.USD), Money.create(cEURCents, Currency.EUR), Money.create(cREALCents, Currency.REAL), null);
            }
            else {
                callback.onBalanceQueryResult(false, null, null, null, errorCause);
            }
        });
    }

    public interface TempCallback {
        void onResult(boolean success, String error);
    }

    public void transfer(Money amount, String destination, TempCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit((success, contractKit, errorCause) -> {
            if (!success) {
                callback.onResult(false, errorCause.getError().getMessage());
                return;
            }

            BigInteger value = CurrencyUtil.centsToBlockChainValue(amount.getCents());

            new Handler(mCeloSDK.getLooper()).post(() -> {
                try {
                    StableTokenWrapper contract;

                    if (amount.getCurrency().equals(Currency.USD)) {
                        contract = contractKit.contracts.getStableToken();
                        contractKit.setFeeCurrency(CeloContract.StableToken);
                    }
                    else {
                        contract = contractKit.contracts.getStableTokenEUR();
                        contractKit.setFeeCurrency(CeloContract.StableTokenEUR);
                    }

                    TransactionReceipt receipt = contract.transfer(destination, value).send();

                    Utils.runOnUIThread(() -> {
                        if (receipt.isStatusOK()) {
                            callback.onResult(true, null);
                        }
                        else {
                            callback.onResult(false, receipt.getRevertReason());
                        }
                    });
                } catch (Exception e) {
                    callback.onResult(false, e.getMessage());
                }
            });
        });
    }

    public void getContractKit(ContractKitCallback callback) {
        ensureCeloSDK();

        mCeloSDK.getContractKit(callback);
    }

    private void ensureCeloSDK() {
        if (mCeloSDK == null) {
            mCeloSDK = new CeloSDK(mContext, CELO_CONTEXT, getAccount());
        }
    }

    public CeloAccount getAccount() {
        if (!isCreated()) {
            throw new IllegalStateException("Wallet does not exist.");
        }

        String privateKey = mPreferences.getString(KEY_PRIVATE_KEY, null);
        String publicKey = mPreferences.getString(KEY_PUBLIC_KEY, null);

        CeloAccount account = new CeloAccount(privateKey, publicKey);

        try {
            WalletSafe.secureAccount(account, mPhoneNumber, mContext);
        } catch (IOException e) {
            Log.e(TAG, "Failed to secure account", e);
        }

        return account;
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
