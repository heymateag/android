package works.heymate.celo;

import org.celo.contractkit.CeloContract;
import org.celo.contractkit.ContractKit;
import org.celo.contractkit.wrapper.StableTokenWrapper;
import org.json.JSONException;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.core.Currency;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import works.heymate.celo.contract.Offer;
import works.heymate.core.offer.PurchasePlanTypes;
import works.heymate.model.Pricing;
import works.heymate.model.PurchasedPlan;
import works.heymate.model.Reservation;
import works.heymate.model.TimeSlot;

public class CeloOffer {

    private static final BigInteger SIXTY = BigInteger.valueOf(60L);

    private final ContractKit mContractKit;
    private final Offer mContract;

    public CeloOffer(String address, ContractKit contractKit) {
        mContractKit = contractKit;
        mContract = Offer.load(address, contractKit.web3j, contractKit.transactionManager, new DefaultGasProvider());
    }

    public void createOfferSignature(Pricing pricing, APIObject paymentTerms) throws Exception {
        byte[] serviceProviderAddress = Numeric.hexStringToByteArray(mContractKit.getAddress());

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (pricing.getPrice() * 100));

        BigInteger initialDeposit = BigInteger.valueOf(paymentTerms.getLong(works.heymate.model.Offer.PaymentTerms.DEPOSIT));
        List<BigInteger> config = getConfig(paymentTerms, pricing);

        pricing.setSignature(sign(serviceProviderAddress, amount, initialDeposit, config));
    }

    public void createBundleSignature(Pricing pricing, int promotionPercent) {
        byte[] address = Numeric.hexStringToByteArray(mContractKit.getAddress());
        List<BigInteger> config = getBundleConfig(pricing, promotionPercent);
        pricing.setBundleSignature(sign(address, config));
    }

    public void createSubscriptionSignature(Pricing pricing, int promotionPercent) {
        byte[] address = Numeric.hexStringToByteArray(mContractKit.getAddress());
        List<BigInteger> config = getSubscriptionConfig(pricing, promotionPercent);
        pricing.setSubscriptionSignature(sign(address, config));
    }

    public void createPaymentPlan(APIObject offer,
                                  APIObject purchasePlan, List<String> referrers) throws JSONException, CeloException {
        Pricing pricing = new Pricing(offer.getObject(works.heymate.model.Offer.PRICING).asJSON());
        Currency currency = Currency.forName(pricing.getCurrency());

        int promotionPercent = 0; // TODO

        boolean isBundle = PurchasePlanTypes.BUNDLE.equals(purchasePlan.getString(PurchasedPlan.PLAN_TYPE));

        byte[] planId = Numeric.hexStringToByteArray(purchasePlan.getString(PurchasedPlan.ID).replaceAll("-", ""));
        BigInteger planType = isBundle ? BigInteger.ONE : BigInteger.valueOf(2L);
        List<BigInteger> config = isBundle ? getBundleConfig(pricing, promotionPercent) : getSubscriptionConfig(pricing, promotionPercent);
        List<String> userAddresses = Arrays.asList(
                offer.getString(works.heymate.model.Offer.WALLET_ADDRESS),
                mContractKit.getAddress(),
                getCurrencyAddress(currency)
                );

        long cents =  isBundle ? pricing.getBundleTotalPrice() * 100L : pricing.getSubscriptionPrice() * 100L;

        adjustGasPayment(currency);
        transfer(cents, currency);

        try {
            mContract.createPlan(
                    planId,
                    planType,
                    config,
                    userAddresses,
                    Numeric.hexStringToByteArray(isBundle ?
                            pricing.getString(Pricing.BUNDLE + "." + Pricing.Bundle.SIGNATURE) :
                            pricing.getString(Pricing.SUBSCRIPTION + "." + Pricing.Subscription.SIGNATURE))
            ).send();
        } catch (Exception e) {
            if (e instanceof TransactionException) {
                throw new CeloException(null, e);
            }
            else {
                throw new CeloException(CeloError.NETWORK_ERROR, e);
            }
        }
    }

    /*
    Offer needs:

    What is the format of rate? DAAAAAAH

    initialDeposit: long
    firstCancellationMinutes: integer
    firstCancellationPercent: float (0-100)
    secondCancellationMinutes: integer
    secondCancellationPercent: float (0-100)
    delayMinutes: integer
    delayPercentage: float (0-100)

    serviceProviderAddress: string
    serviceProviderSignature: string
     */
    public void create(APIObject offer, APIObject timeSlot, String sTradeId,
                       APIObject purchasePlan, List<String> referrers) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(sTradeId.replaceAll("-", ""));
        byte[] planId = purchasePlan == null ? null : Numeric.hexStringToByteArray(purchasePlan.getString(PurchasedPlan.ID).replaceAll("-", ""));

        Pricing pricing = new Pricing(offer.getObject(works.heymate.model.Offer.PRICING).asJSON());
        Currency currency = Currency.forName(pricing.getCurrency());

        String purchasePlanType = purchasePlan == null ? PurchasePlanTypes.SINGLE : purchasePlan.getString(PurchasedPlan.PLAN_TYPE);

        long rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricing.getPrice();
                planId = new byte[16];
                break;
            case PurchasePlanTypes.BUNDLE:
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = 0;
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided.");
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue(rate * 100);

        BigInteger initialDeposit;

        List<String> userAddresses = Arrays.asList(
                offer.getString(works.heymate.model.Offer.WALLET_ADDRESS),
                mContractKit.getAddress(),
                getCurrencyAddress(currency));

        initialDeposit = new BigInteger(String.valueOf(offer.getLong(works.heymate.model.Offer.PAYMENT_TERMS + "." + works.heymate.model.Offer.PaymentTerms.DEPOSIT)));
        List<BigInteger> config = getConfig(offer.getObject(works.heymate.model.Offer.PAYMENT_TERMS), pricing);

        adjustGasPayment(currency);

        if (rate > 0) {
            transfer(rate * 100, currency);
        }

        try {
            TransactionReceipt receipt = mContract.createOffer(
                    tradeId,
                    planId,
                    amount,
                    BigInteger.ONE, // fee
                    BigInteger.valueOf(offer.getLong(works.heymate.model.Offer.EXPIRATION)),
                    BigInteger.valueOf(timeSlot.getLong(TimeSlot.FROM_TIME)),
                    initialDeposit,
                    userAddresses,
                    config,
                    referrers,
                    new ArrayList<>(0),
                    Numeric.hexStringToByteArray(pricing.getString(Pricing.SIGNATURE))
            ).send();

            String transactionHash = receipt.getTransactionHash();

            System.out.println(transactionHash); // TODO To be returned for the new back-end.
        } catch (Exception e) {
            if (e instanceof TransactionException) {
                throw new CeloException(null, e);
            }
            else {
                throw new CeloException(CeloError.NETWORK_ERROR, e);
            }
        }
    }

    private void adjustGasPayment(Currency currency) throws CeloException {
        if (currency == Currency.USD) {
            mContractKit.setFeeCurrency(CeloContract.StableToken);
        }
        else if (currency == Currency.EUR) {
            mContractKit.setFeeCurrency(CeloContract.StableTokenEUR);
        }
        else {
            throw new CeloException(CeloError.NETWORK_ERROR, new Exception("Unknown currency: " + currency)); // TODO Unrelated error
        }
    }

    private void transfer(long cents, Currency currency) throws CeloException {
        BigInteger amount = CurrencyUtil.centsToBlockChainValue(cents);

        StableTokenWrapper token;

        if (currency == Currency.USD) {
            token = mContractKit.contracts.getStableToken();
        }
        else if (currency == Currency.EUR) {
            token = mContractKit.contracts.getStableTokenEUR();
        }
        else {
            throw new CeloException(CeloError.NETWORK_ERROR, new Exception("Unknown currency: " + currency)); // TODO Unrelated error
        }

        try {
            // token.approve(mContract.getContractAddress(), amount).send();
            token.transfer(mContract.getContractAddress(), amount).send();
        } catch (Exception e) {
            if (CeloSDK.isErrorCausedByInsufficientFunds(e)) {
                throw new CeloException(CeloError.INSUFFICIENT_BALANCE, e);
            }

            throw new CeloException(CeloError.NETWORK_ERROR, e);
        }
    }

    public void startService(APIObject offer, APIObject purchasedPlan, APIObject reservation, String consumerAddress) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getString(Reservation.TRADE_ID).replaceAll("-", ""));

        Pricing pricing = new Pricing(offer.getObject(works.heymate.model.Offer.PRICING).asJSON());
        Currency currency = Currency.forName(pricing.getCurrency());

        String purchasePlanType = purchasedPlan == null ? PurchasePlanTypes.SINGLE : purchasedPlan.getString(PurchasedPlan.PLAN_TYPE);

        long rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricing.getPrice();
                break;
            case PurchasePlanTypes.BUNDLE:
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = 0;
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided."); // TODO not runtime exception! (has repetitions in this class)
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (rate) * 100);

        adjustGasPayment(currency);

        try {
            mContract.startService(tradeId, offer.getString(works.heymate.model.Offer.WALLET_ADDRESS), consumerAddress, amount, BigInteger.ONE).send();
        } catch (Exception e) {
            if (e instanceof TransactionException) {
                throw new CeloException(null, e);
            }
            else {
                throw new CeloException(CeloError.NETWORK_ERROR, e);
            }
        }
    }

    public void finishService(APIObject offer, APIObject purchasedPlan, APIObject reservation, String consumerAddress) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getString(Reservation.TRADE_ID).replaceAll("-", ""));

        Pricing pricing = new Pricing(offer.getObject(works.heymate.model.Offer.PRICING).asJSON());
        Currency currency = Currency.forName(pricing.getCurrency());

        String purchasePlanType = purchasedPlan == null ? PurchasePlanTypes.SINGLE : purchasedPlan.getString(PurchasedPlan.PLAN_TYPE);

        long rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricing.getPrice();
                break;
            case PurchasePlanTypes.BUNDLE:
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = 0;
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided."); // TODO not runtime exception! (has repetitions in this class)
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (rate) * 100);

        adjustGasPayment(currency);

        try {
            mContract.release(tradeId, offer.getString(works.heymate.model.Offer.WALLET_ADDRESS), consumerAddress, amount, BigInteger.ONE).send();
        } catch (Exception e) {
            if (e instanceof TransactionException) {
                throw new CeloException(null, e);
            }
            else {
                throw new CeloException(CeloError.NETWORK_ERROR, e);
            }
        }
    }

    public void cancelService(APIObject offer, APIObject purchasedPlan, APIObject reservation, String consumerAddress, boolean consumerCancelled) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getString(Reservation.TRADE_ID).replaceAll("-", ""));

        Pricing pricing = new Pricing(offer.getObject(works.heymate.model.Offer.PRICING).asJSON());
        Currency currency = Currency.forName(pricing.getCurrency());

        String purchasePlanType = purchasedPlan == null ? PurchasePlanTypes.SINGLE : purchasedPlan.getString(PurchasedPlan.PLAN_TYPE);

        long rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricing.getPrice();
                break;
            case PurchasePlanTypes.BUNDLE:
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = 0;
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided."); // TODO not runtime exception! (has repetitions in this class)
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (rate) * 100);

        adjustGasPayment(currency);

        try {
            if (consumerCancelled) {
                mContract.consumerCancel(tradeId, offer.getString(works.heymate.model.Offer.WALLET_ADDRESS), consumerAddress, amount, BigInteger.ONE).send();
            }
            else {
                mContract.serviceProviderCancel(tradeId, offer.getString(works.heymate.model.Offer.WALLET_ADDRESS), consumerAddress, amount, BigInteger.ONE).send();
            }
        } catch (Exception e) {
            if (e instanceof TransactionException) {
                throw new CeloException(null, e);
            }
            else {
                throw new CeloException(CeloError.NETWORK_ERROR, e);
            }
        }
    }

    private static List<BigInteger> getConfig(APIObject paymentTerms, Pricing pricing) throws JSONException {
        APIObject delay = paymentTerms.getObject(works.heymate.model.Offer.PaymentTerms.DELAY_IN_START);
        APIArray cancellation = paymentTerms.getArray(works.heymate.model.Offer.PaymentTerms.CANCELLATION);
        APIObject cancel1 = cancellation.getObject(0);
        APIObject cancel2 = cancellation.getObject(1);

        List<BigInteger> config = new ArrayList<>(8);

        BigInteger hours1 = BigInteger.valueOf(cancel1.getLong(works.heymate.model.Offer.PaymentTerms.Cancellation.RANGE)).multiply(SIXTY);
        BigInteger percent1 = BigInteger.valueOf(cancel1.getLong(works.heymate.model.Offer.PaymentTerms.Cancellation.PENALTY));
        BigInteger hours2 = BigInteger.valueOf(cancel2.getLong(works.heymate.model.Offer.PaymentTerms.Cancellation.RANGE)).multiply(SIXTY);
        BigInteger percent2 = BigInteger.valueOf(cancel2.getLong(works.heymate.model.Offer.PaymentTerms.Cancellation.PENALTY));
        BigInteger delayTime = BigInteger.valueOf(delay.getLong(works.heymate.model.Offer.PaymentTerms.DelayInStart.DURATION));
        BigInteger delayPercent = BigInteger.valueOf(delay.getLong(works.heymate.model.Offer.PaymentTerms.DelayInStart.PENALTY));

        if (hours1.compareTo(hours2) > 0) {
            config.add(hours1);
            config.add(percent1);
            config.add(hours2);
            config.add(percent2);
        }
        else {
            config.add(hours2);
            config.add(percent2);
            config.add(hours1);
            config.add(percent1);
        }

        config.add(delayTime);
        config.add(delayPercent);
        config.add(BigInteger.valueOf(4)); // Linear config
        config.add(BigInteger.valueOf(0)); // TODO promotion rate

        return config;
    }

    private static List<BigInteger> getBundleConfig(Pricing pricing, int promotionPercent) {
        BigInteger price = BigInteger.valueOf(pricing.getPrice() * (100 - pricing.getBundleDiscountPercent()))
                .multiply(CurrencyUtil.WEI).divide(CurrencyUtil.ONE_HUNDRED);

        return Arrays.asList(
                price,
                BigInteger.valueOf(0),
                BigInteger.valueOf(pricing.getBundleCount()),
                BigInteger.valueOf(4),
                BigInteger.ZERO //BigInteger.valueOf(promotionPercent)
        );
    }

    private static List<BigInteger> getSubscriptionConfig(Pricing pricing, int promotionPercent) {
        return Arrays.asList(
                BigInteger.ZERO,
                BigInteger.valueOf(pricing.getSubscriptionPeriod() == null ? 0 : (pricing.getSubscriptionPeriod().equals(PriceInputItem.SUBSCRIPTION_PERIODS[0]) ? 1 : 2)),
                BigInteger.valueOf(0),
                BigInteger.valueOf(4),
                BigInteger.ZERO //BigInteger.valueOf(promotionPercent)
        );
    }

    private String sign(Object... params) {
        Sign.SignatureData signatureData = Sign.signPrefixedMessage(getBytes(params), mContractKit.transactionManager.getCredentials().getEcKeyPair());

        byte[] signature = getBytes(signatureData.getV(), signatureData.getR(), signatureData.getS());

        return Numeric.toHexString(signature);
    }

    private static byte[] getBytes(Object... params) {
        byte[][] bytes = new byte[params.length][];
        int length = 0;

        for (int i = 0; i < params.length; i++) {
            Object obj = params[i];

            if (obj instanceof byte[]) {
                bytes[i] = (byte[]) obj;
            }
            else if (obj instanceof BigInteger) {
                bytes[i] = ((BigInteger) obj).toByteArray();
            }
            else if (obj instanceof String) {
                bytes[i] = ((String) obj).getBytes();
            }
            else if (obj.getClass().isArray()) {
                bytes[i] = getBytes((Object[]) obj);
            }
            else if (obj instanceof List) {
                bytes[i] = getBytes(((List) obj).toArray(new Object[0]));
            }
            else {
                throw new IllegalArgumentException("Unsupported type");
            }

            length += bytes[i].length;
        }

        byte[] combinedBytes = new byte[length];

        length = 0;

        for (byte[] bs : bytes) {
            System.arraycopy(bs, 0, combinedBytes, length, bs.length);
            length += bs.length;
        }

        return combinedBytes;
    }

    private String getCurrencyAddress(Currency currency) {
        if (Currency.USD.equals(currency)) {
            return mContractKit.contracts.getStableToken().getContractAddress();
        }

        if (Currency.EUR.equals(currency)) {
            return mContractKit.contracts.getStableTokenEUR().getContractAddress();
        }

        return null;
    }

}
