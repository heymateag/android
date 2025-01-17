package works.heymate.celo;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import org.celo.contractkit.ContractKit;
import org.celo.contractkit.ContractKitOptions;
import org.celo.contractkit.Utils;
import org.celo.contractkit.wrapper.AttestationsWrapper;
import org.celo.contractkit.wrapper.BaseWrapper;
import org.celo.contractkit.wrapper.ExchangeWrapper;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java8.util.Optional;
import jnr.ffi.annotations.In;

public class CeloSDK {

    private static final String TAG = "CeloSDK";

    private static final int MESSAGE_GET_CONTRACT_KIT = 0;
    private static final int MESSAGE_LOOKUP_PHONE_NUMBER = 2;
    private static final int MESSAGE_LOOKUP_PHONE_NUMBER_OWNERSHIP = 3;
    private static final int MESSAGE_GET_BALANCE = 4;

    // https://discord.com/channels/600834479145353243/786788580944642058/823669465988333578
    // Asked in Discord from nambrot#9524 Nambrot|cLabs server admin & Protocol Engineer
    // Q: Do you have to pay for getting phone number hash from ODIS? And won't it be much when you have to query multiple numbers (I know about caching the hashes)?
    // A: You don't have to explicitly pay for making queries against ODIS, but the key used needs to have some balance/tx history or needs to have attestations. Read more here https://docs.celo.org/developer-guide/contractkit/odis
    // Q: I want to check if my new user that I have both their phone number and their private-key, has their phone number verified or not. The first thing I have to do is get the phone number salt. Can I conclude that if I get a 403 odisQuotaError or a 401 odisAuthError error from /getBlindedMessageSig it means the account has no transaction history/balance and so definitely not attested with any phone number?
    // A: 403 - yes, 401 might mean that the signature from the key you are trying to use couldn't be validated
    public static boolean isSaltHasFailedBecauseOfBrandNewAccount(CeloException exception) {
        Throwable actualCause = exception.getMainCause().getCause();

        return (actualCause != null && (actualCause.getMessage().contains(ODISSaltUtil.ERROR_ODIS_QUOTA) || (actualCause.getMessage().contains(ODISSaltUtil.ERROR_ODIS_AUTH))));
    }

    public static boolean isErrorCausedByInsufficientFunds(Exception exception) {
        if (exception instanceof TransactionException) {
            Optional<TransactionReceipt> optionalReceipt = ((TransactionException) exception).getTransactionReceipt();

            if (optionalReceipt.isPresent()) {
                TransactionReceipt receipt = optionalReceipt.get();

                if (receipt.getRevertReason() != null && receipt.getRevertReason().toLowerCase(Locale.US).contains("transfer value exceeded balance of sender")) {
                    return true;
                }
            }
        }

        return exception.getMessage().toLowerCase(Locale.US).contains("insufficient funds");
    }

    private static Looper newLooper() {
        HandlerThread thread = new HandlerThread(TAG + "-" + Math.round(Math.random() * 100));
        thread.start();
        return thread.getLooper();
    }

    private final Context mContext;
    private final LocalHandler mLocalHandler;

    private final CeloContext mCeloContext;

    private final Credentials mAccount;

    private final List<ContractKitCallback> mContractKitCallbacks = new ArrayList<>();
    private final Map<String, List<PhoneNumberLookupCallback>> mPhoneNumberLookupCallbacks = new Hashtable<>(2);
    private final List<PhoneNumberOwnershipLookupCallback> mPhoneNumberOwnershipLookupCallbacks = new ArrayList<>(1);
    private final List<BalanceCallback> mBalanceCallbacks = new ArrayList<>(1);

    private ContractKit mContractKit;

    public CeloSDK(Context context, CeloContext celoContext, CeloAccount account) {
        this(context, celoContext, account, newLooper());
    }

    public CeloSDK(Context context, CeloContext celoContext, CeloAccount account, Looper looper) {
        mContext = context.getApplicationContext();
        mLocalHandler = new LocalHandler(looper);

        mCeloContext = celoContext;

        mAccount = Credentials.create(account.privateKey, account.publicKey);
    }

    public Looper getLooper() {
        return mLocalHandler.getLooper();
    }

    /**
     * callback is called on the looper thread.
     * @param callback
     */
    public void getContractKit(ContractKitCallback callback) {
        synchronized (mContractKitCallbacks) {
            mContractKitCallbacks.add(callback);

            if (mContractKitCallbacks.size() > 1) {
                return;
            }
        }

        mLocalHandler.sendEmptyMessage(MESSAGE_GET_CONTRACT_KIT);
    }

    public String getAddress() {
        return mAccount.getAddress();
    }

    public void lookupPhoneNumber(String phoneNumber, PhoneNumberLookupCallback callback) {
        if (!Utils.E164_REGEX.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }

        List<PhoneNumberLookupCallback> callbacks = mPhoneNumberLookupCallbacks.get(phoneNumber);

        if (callbacks == null) {
            callbacks = new ArrayList<>(1);

            mPhoneNumberLookupCallbacks.put(phoneNumber, callbacks);
        }

        callbacks.add(callback);

        if (callbacks.size() > 1) {
            return;
        }

        Message message = Message.obtain(mLocalHandler, MESSAGE_LOOKUP_PHONE_NUMBER, phoneNumber);
        mLocalHandler.sendMessage(message);
    }

    public void lookupPhoneNumberOwnership(String phoneNumber, PhoneNumberOwnershipLookupCallback callback) {
        if (!Utils.E164_REGEX.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }

        mPhoneNumberOwnershipLookupCallbacks.add(callback);

        if (mPhoneNumberOwnershipLookupCallbacks.size() > 1) {
            return;
        }

        mLocalHandler.sendMessage(Message.obtain(mLocalHandler, MESSAGE_LOOKUP_PHONE_NUMBER_OWNERSHIP, phoneNumber));
    }

    public void requestAttestationsForPhoneNumber(String phoneNumber, AttestationRequestCallback callback) {
        if (!Utils.E164_REGEX.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }

        getContractKit((success, contractKit, errorCause) -> {
            if (errorCause != null) {
                InternalUtils.runOnMainThread(() -> callback.onAttestationRequestResult(false, 0, 0, 0, new CeloException(CeloError.CONTRACT_KIT_ERROR, errorCause)));
                return;
            }

            String salt;

            try {
                salt = ODISSaltUtil.getSalt(mContext, contractKit, mCeloContext.odisURL, mCeloContext.odisPublicKey, phoneNumber);
            } catch (CeloException e) {
                CeloException exception;

                if (isSaltHasFailedBecauseOfBrandNewAccount(e)) {
                    exception = new CeloException(CeloError.INSUFFICIENT_BALANCE, null);
                }
                else {
                    exception = e;
                }

                InternalUtils.runOnMainThread(() -> callback.onAttestationRequestResult(false, 0, 0, 0, new CeloException(CeloError.SALTING_ERROR, exception)));
                return;
            }

            AttestationRequester.AttestationResult result = AttestationRequester.requestAttestations(contractKit, phoneNumber, salt);

            InternalUtils.runOnMainThread(() -> callback.onAttestationRequestResult(result.countsAreReliable, result.newAttestations, result.totalAttestations, result.completedAttestations, result.errorCause));
        });
    }

    public void completeAttestationForPhoneNumber(String phoneNumber, String code, AttestationCompletionCallback callback) {
        if (!Utils.E164_REGEX.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }

        getContractKit((success, contractKit, errorCause) -> {
            if (errorCause != null) {
                InternalUtils.runOnMainThread(() -> callback.onAttestationCompletionResult(false, 0, 0, 0, errorCause));
                return;
            }

            String salt;

            try {
                salt = ODISSaltUtil.getSalt(mContext, contractKit, mCeloContext.odisURL, mCeloContext.odisPublicKey, phoneNumber);
            } catch (CeloException e) {
                InternalUtils.runOnMainThread(() -> callback.onAttestationCompletionResult(false, 0, 0, 0, new CeloException(CeloError.SALTING_ERROR, e)));
                return;
            }

            try {
                AttestationCompleter.completeAttestation(mContext, contractKit, phoneNumber, salt, code);
            } catch (CeloException e) {
                InternalUtils.runOnMainThread(() -> callback.onAttestationCompletionResult(false, 0, 0, 0, e));
                return;
            }

            try {
                AttestationsWrapper.AttestationsStatus status = lookupPhoneNumberVerificationStatus(phoneNumber);

                InternalUtils.runOnMainThread(() -> callback.onAttestationCompletionResult(true, status.completed, status.total, status.numAttestationsRemaining, null));
            } catch (CeloException e) {
                InternalUtils.runOnMainThread(() -> callback.onAttestationCompletionResult(true, 0, 0, 0, e));
            }
        });
    }

    public void getBalance(BalanceCallback callback) {
        mBalanceCallbacks.add(callback);

        if (mBalanceCallbacks.size() > 1) {
            return;
        }

        mLocalHandler.sendEmptyMessage(MESSAGE_GET_BALANCE);
    }

    public void howMuchToBuy(Money money, Currency fromCurrency, AmountCallback callback) {
        if (money.currency == fromCurrency) {
            InternalUtils.runOnMainThread(() -> callback.onAmountResult(true, money.amount, null));
            return;
        }

        getContractKit((success, contractKit, errorCause) -> {
            if (success) {
                try {
                    int exchangeTimes = 0;

                    BigInteger value;

                    if (money.currency == Currency.GOLD) {
                        ExchangeWrapper exchange = fromCurrency.getExchange(contractKit);
                        exchangeTimes++;

                        value = exchange.getSellTokenAmount(money.amount.blockchainValue(), false).send();
                    }
                    else {
                        ExchangeWrapper exchange = money.currency.getExchange(contractKit);
                        exchangeTimes++;

                        value = exchange.getSellTokenAmount(money.amount.blockchainValue(), true).send();

                        if (fromCurrency != Currency.GOLD) {
                            exchange = fromCurrency.getExchange(contractKit);
                            exchangeTimes++;

                            value = exchange.getSellTokenAmount(value, false).send();
                        }
                    }

                    BigInteger gasCost = estimateExchangeGasCost(contractKit, fromCurrency, exchangeTimes);

                    Amount amount = Amount.fromBlockchainValue(value.add(gasCost));

                    InternalUtils.runOnMainThread(() -> callback.onAmountResult(true, amount, null));
                } catch (Exception e) {
                    InternalUtils.runOnMainThread(() -> callback.onAmountResult(false, null, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
            else {
                InternalUtils.runOnMainThread(() ->callback.onAmountResult(false, null, errorCause));
            }
        });
    }

    public void howMuchFromSale(Money money, Currency toCurrency, AmountCallback callback) {
        if (money.currency == toCurrency) {
            InternalUtils.runOnMainThread(() -> callback.onAmountResult(true, money.amount, null));
            return;
        }

        getContractKit((success, contractKit, errorCause) -> {
            if (success) {
                try {
                    int exchangeTimes = 0;

                    BigInteger value;

                    if (money.currency == Currency.GOLD) {
                        ExchangeWrapper exchange = toCurrency.getExchange(contractKit);
                        exchangeTimes++;

                        value = exchange.getBuyTokenAmount(money.amount.blockchainValue(), true).send();
                    }
                    else {
                        ExchangeWrapper exchange = money.currency.getExchange(contractKit);
                        exchangeTimes++;

                        value = exchange.getBuyTokenAmount(money.amount.blockchainValue(), false).send();

                        if (toCurrency != Currency.GOLD) {
                            exchange = toCurrency.getExchange(contractKit);
                            exchangeTimes++;

                            value = exchange.getBuyTokenAmount(value, true).send();
                        }
                    }

                    BigInteger gasCost = estimateExchangeGasCost(contractKit, money.currency, exchangeTimes);

                    Amount amount = Amount.fromBlockchainValue(value.add(gasCost));

                    InternalUtils.runOnMainThread(() -> callback.onAmountResult(true, amount, null));
                } catch (Exception e) {
                    InternalUtils.runOnMainThread(() -> callback.onAmountResult(false, null, new CeloException(CeloError.NETWORK_ERROR, e)));
                }
            }
            else {
                InternalUtils.runOnMainThread(() ->callback.onAmountResult(false, null, errorCause));
            }
        });
    }

    private BigInteger estimateExchangeGasCost(ContractKit contractKit, Currency currency, int times) throws Exception {
        BigInteger gasPrice = contractKit.getGasPriceMinimum(((BaseWrapper) currency.getToken(contractKit)).getContractAddress(), BigInteger.ZERO);

        String data = contractKit.contracts.getExchange().getSellTokenAmount(BigInteger.TEN, false).encodeFunctionCall();
        Transaction transaction = Transaction.createEthCallTransaction(null, contractKit.contracts.getExchange().getContractAddress(), data);
        BigInteger gas = contractKit.web3j.ethEstimateGas(transaction).send().getAmountUsed();
        gas = BigDecimal.valueOf(gas.doubleValue() * contractKit.config.gasInflationFactor).toBigInteger();

        return gasPrice.multiply(gas).multiply(BigInteger.valueOf(times));
    }

    private void getBalanceInternal() {
        try {
            BalanceInfo balanceInfo = getBalanceInfo();

            BigInteger one = Convert.toWei(BigDecimal.ONE, Convert.Unit.ETHER).toBigInteger();

            long cUSD = balanceInfo.cUSD.divide(one.divide(BigInteger.valueOf(100L))).longValue();
            long cEUR = balanceInfo.cEUR.divide(one.divide(BigInteger.valueOf(100L))).longValue();
            long cREAL = balanceInfo.cREAL.divide(one.divide(BigInteger.valueOf(100L))).longValue();

            // double gold = balanceInfo.gold.divide(one.divide(BigInteger.valueOf(10_000L))).longValue() / 10_000d;

            List<BalanceCallback> callbacks = new ArrayList<>(mBalanceCallbacks);
            mBalanceCallbacks.clear();

            InternalUtils.runOnMainThread(() -> {
                for (BalanceCallback callback: callbacks) {
                    callback.onBalanceResult(true, balanceInfo.cUSD, balanceInfo.cEUR, balanceInfo.cREAL, cUSD, cEUR, cREAL, null);
                }
            });
        } catch (CeloException e) {
            List<BalanceCallback> callbacks = new ArrayList<>(mBalanceCallbacks);
            mBalanceCallbacks.clear();

            InternalUtils.runOnMainThread(() -> {
                for (BalanceCallback callback: callbacks) {
                    callback.onBalanceResult(false, null, null, null, 0, 0, 0, e);
                }
            });
        }
    }

    private BalanceInfo getBalanceInfo() throws CeloException {
        try {
            ensureContractKit();
        } catch (CeloException e) {
            throw new CeloException(CeloError.CONTRACT_KIT_ERROR, e);
        }

        try {
            BigInteger cUSD = mContractKit.contracts.getStableToken().balanceOf(mContractKit.getAddress()).send();
            BigInteger cEUR = mContractKit.contracts.getStableTokenEUR().balanceOf(mContractKit.getAddress()).send();
            BigInteger cREAL = mContractKit.contracts.getStableTokenBRL().balanceOf(mContractKit.getAddress()).send();
            // BigInteger gold = mContractKit.contracts.getGoldToken().balanceOf(mContractKit.getAddress());

            return new BalanceInfo(cUSD, cEUR, cREAL);
        } catch (Exception e) {
            throw new CeloException(CeloError.NETWORK_ERROR, e);
        }
    }

    private void lookupPhoneNumberOwnershipInternal(String phoneNumber) {
        try {
            AttestationsWrapper.AttestationsStatus status = lookupPhoneNumberVerificationStatus(phoneNumber);

            List<PhoneNumberOwnershipLookupCallback> callbacks = new ArrayList<>(mPhoneNumberOwnershipLookupCallbacks);
            mPhoneNumberOwnershipLookupCallbacks.clear();

            InternalUtils.runOnMainThread(() -> {
                for (PhoneNumberOwnershipLookupCallback callback: callbacks) {
                    callback.onPhoneNumberOwnershipLookupResult(true, status.isVerified, status.completed, status.total, status.numAttestationsRemaining, null);
                }
            });
        } catch (CeloException e) {
            List<PhoneNumberOwnershipLookupCallback> callbacks = new ArrayList<>(mPhoneNumberOwnershipLookupCallbacks);
            mPhoneNumberOwnershipLookupCallbacks.clear();

            if (isSaltHasFailedBecauseOfBrandNewAccount(e)) {
                InternalUtils.runOnMainThread(() -> {
                    for (PhoneNumberOwnershipLookupCallback callback: callbacks) {
                        callback.onPhoneNumberOwnershipLookupResult(true, false, 0, 0, 0, e);
                    }
                });
                return;
            }

            InternalUtils.runOnMainThread(() -> {
                for (PhoneNumberOwnershipLookupCallback callback: callbacks) {
                    callback.onPhoneNumberOwnershipLookupResult(false, false, 0, 0, 0, e);
                }
            });
        }
    }

    private AttestationsWrapper.AttestationsStatus lookupPhoneNumberVerificationStatus(String phoneNumber) throws CeloException {
        try {
            ensureContractKit();
        } catch (CeloException e) {
            throw new CeloException(CeloError.CONTRACT_KIT_ERROR, e);
        }

        String salt;

        try {
            salt = ODISSaltUtil.getSalt(mContext, mContractKit, mCeloContext.odisURL, mCeloContext.odisPublicKey, phoneNumber);
        } catch (CeloException e) {
            if (isSaltHasFailedBecauseOfBrandNewAccount(e)) {
                return new AttestationsWrapper.AttestationsStatus(false, AttestationRequester.NUM_ATTESTATIONS_REQUIRED, 0, 0);
            }

            throw new CeloException(CeloError.SALTING_ERROR, e);
        }

        byte[] identifier = Utils.getPhoneHash(phoneNumber, salt);

        try {
            return mContractKit.contracts.getAttestations().getVerifiedStatus(
                    identifier, mContractKit.getAddress(),
                    AttestationRequester.NUM_ATTESTATIONS_REQUIRED,
                    AttestationRequester.DEFAULT_ATTESTATION_THRESHOLD
            );
        } catch (Exception e) {
            throw new CeloException(CeloError.NETWORK_ERROR, e);
        }
    }

    private void lookupPhoneNumberInternal(String phoneNumber) {
        try {
            List<String> addresses = lookupAddressesForPhoneNumber(phoneNumber);

            InternalUtils.runOnMainThread(() -> {
                List<PhoneNumberLookupCallback> callbacks = mPhoneNumberLookupCallbacks.get(phoneNumber);

                if (callbacks != null) {
                    ArrayList<PhoneNumberLookupCallback> pendingCallbacks = new ArrayList<>(callbacks);
                    callbacks.clear();

                    for (PhoneNumberLookupCallback callback: pendingCallbacks) {
                        callback.onPhoneNumberLookupResult(true, addresses, null);
                    }
                }
            });
        } catch (CeloException e) {
            InternalUtils.runOnMainThread(() -> {
                List<PhoneNumberLookupCallback> callbacks = mPhoneNumberLookupCallbacks.get(phoneNumber);

                if (callbacks != null) {
                    ArrayList<PhoneNumberLookupCallback> pendingCallbacks = new ArrayList<>(callbacks);
                    callbacks.clear();

                    for (PhoneNumberLookupCallback callback: pendingCallbacks) {
                        callback.onPhoneNumberLookupResult(false, null, e);
                    }
                }
            });
        }
    }

    private List<String> lookupAddressesForPhoneNumber(String phoneNumber) throws CeloException {
        try {
            ensureContractKit();
        } catch (CeloException e) {
            throw new CeloException(CeloError.CONTRACT_KIT_ERROR, e);
        }

        String salt;

        try {
            salt = ODISSaltUtil.getSalt(mContext, mContractKit, mCeloContext.odisURL, mCeloContext.odisPublicKey, phoneNumber);
        } catch (CeloException e) {
            throw new CeloException(CeloError.SALTING_ERROR, e);
        }

        byte[] identifier = Utils.getPhoneHash(phoneNumber, salt);

        try {
            return mContractKit.contracts.getAttestations().lookupAccountsForIdentifier(identifier).send();
        } catch (Exception e) {
            throw new CeloException(CeloError.NETWORK_ERROR, e);
        }
    }

    private void getContractKitInternal() {
        try {
            ensureContractKit();
        } catch (CeloException e) {
            List<ContractKitCallback> callbacks;

            synchronized (mContractKitCallbacks) {
                callbacks = new ArrayList<>(mContractKitCallbacks);

                mContractKitCallbacks.clear();
            }

            for (ContractKitCallback callback: callbacks) {
                callback.onContractKitResult(false, null, e);
            }
            return;
        }

        List<ContractKitCallback> callbacks;

        synchronized (mContractKitCallbacks) {
            callbacks = new ArrayList<>(mContractKitCallbacks);

            mContractKitCallbacks.clear();
        }

        for (ContractKitCallback callback: callbacks) {
            callback.onContractKitResult(true, mContractKit, null);
        }
    }

    private void ensureContractKit() throws CeloException {
        if (mContractKit == null) {
            ContractKit contractKit;

            try {
                ContractKitOptions options = new ContractKitOptions.Builder()
                        .setChainId(mCeloContext.chainId)
                        .build();
                contractKit = new ContractKit(Web3j.build(new HttpService(mCeloContext.networkAddress)), options);

                contractKit.addAccount(mAccount);
            } catch (Throwable t) {
                throw new CeloException(CeloError.NETWORK_ERROR, t);
            }

            mContractKit = contractKit;
        }
    }

    private class LocalHandler extends Handler {

        LocalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_CONTRACT_KIT:
                    getContractKitInternal();
                    return;
                case MESSAGE_LOOKUP_PHONE_NUMBER:
                    lookupPhoneNumberInternal((String) msg.obj);
                    return;
                case MESSAGE_LOOKUP_PHONE_NUMBER_OWNERSHIP:
                    lookupPhoneNumberOwnershipInternal((String) msg.obj);
                    return;
                case MESSAGE_GET_BALANCE:
                    getBalanceInternal();
                    return;
            }
        }

    }

    private static class BalanceInfo {

        final BigInteger cUSD;
        final BigInteger cEUR;
        final BigInteger cREAL;

        BalanceInfo(BigInteger cUSD, BigInteger cEUR, BigInteger cREAL) {
            this.cUSD = cUSD;
            this.cEUR = cEUR;
            this.cREAL = cREAL;
        }

    }

}
