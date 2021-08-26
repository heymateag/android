package works.heymate.celo;

import com.amplifyframework.datastore.generated.model.PurchasedPlan;
import com.amplifyframework.datastore.generated.model.Reservation;

import org.celo.contractkit.CeloContract;
import org.celo.contractkit.ContractKit;
import org.celo.contractkit.wrapper.StableTokenWrapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;

import works.heymate.core.Currency;
import works.heymate.core.offer.PricingInfo;
import org.web3j.crypto.Sign;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import works.heymate.celo.contract.Offer;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.offer.PurchasePlanTypes;

public class CeloOffer {

    private static final BigInteger SIXTY = BigInteger.valueOf(60L);

    private final ContractKit mContractKit;
    private final Offer mContract;

    public CeloOffer(String address, ContractKit contractKit) {
        mContractKit = contractKit;
        mContract = Offer.load(address, contractKit.web3j, contractKit.transactionManager, new DefaultGasProvider());
    }

    public String createOfferSignature(PricingInfo pricingInfo, JSONObject termsConfig) throws Exception {
        byte[] serviceProviderAddress = Numeric.hexStringToByteArray(mContractKit.getAddress());

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (pricingInfo.price * 100));

        BigInteger initialDeposit = new BigInteger(termsConfig.getString(OfferUtils.INITIAL_DEPOSIT));
        List<BigInteger> config = getConfig(termsConfig, pricingInfo);

        return sign(serviceProviderAddress, amount, initialDeposit, config);
    }

    public String createBundleSignature(PricingInfo pricingInfo, int promotionPercent) {
        byte[] address = Numeric.hexStringToByteArray(mContractKit.getAddress());
        List<BigInteger> config = getBundleConfig(pricingInfo, promotionPercent);
        return sign(address, config);
    }

    public String createSubscriptionSignature(PricingInfo pricingInfo, int promotionPercent) {
        byte[] address = Numeric.hexStringToByteArray(mContractKit.getAddress());
        List<BigInteger> config = getSubscriptionConfig(pricingInfo, promotionPercent);
        return sign(address, config);
    }

    public void createPaymentPlan(com.amplifyframework.datastore.generated.model.Offer offer,
                                  PurchasedPlan purchasePlan, List<String> referrers) throws JSONException, CeloException {
        PricingInfo pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));

        JSONObject configJSON = new JSONObject(offer.getTermsConfig());
        int promotionPercent = configJSON.getInt(OfferUtils.PROMOTION_RATE);

        boolean isBundle = PurchasePlanTypes.BUNDLE.equals(purchasePlan.getPlanType());

        byte[] planId = Numeric.hexStringToByteArray(purchasePlan.getId().replaceAll("-", ""));
        BigInteger planType = isBundle ? BigInteger.ONE : BigInteger.valueOf(2L);
        List<BigInteger> config = isBundle ? getBundleConfig(pricingInfo, promotionPercent) : getSubscriptionConfig(pricingInfo, promotionPercent);
        List<String> userAddresses = Arrays.asList(
                offer.getWalletAddress(),
                mContractKit.getAddress(),
                getCurrencyAddress(pricingInfo.currency)
                );

        long cents = pricingInfo.getBundleTotalPrice() * 100L;
        Currency currency = pricingInfo.currency;

        adjustGasPayment(currency);
        transfer(cents, currency);

        try {
            mContract.createPlan(
                    planId,
                    planType,
                    config,
                    userAddresses,
                    Numeric.hexStringToByteArray(isBundle ? offer.getBundleSignature() : offer.getSubscriptionSignature())).send();
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
    public void create(com.amplifyframework.datastore.generated.model.Offer offer, Reservation reservation,
                       PurchasedPlan purchasePlan, List<String> referrers) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));
        byte[] planId = purchasePlan == null ? null : Numeric.hexStringToByteArray(purchasePlan.getId().replaceAll("-", ""));

        PricingInfo pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));

        String purchasePlanType = reservation.getPurchasedPlanType();

        int rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricingInfo.price;
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
                offer.getWalletAddress(),
                mContractKit.getAddress(),
                getCurrencyAddress(pricingInfo.currency));

        JSONObject configJSON = new JSONObject(offer.getTermsConfig());

        initialDeposit = new BigInteger(configJSON.getString(OfferUtils.INITIAL_DEPOSIT));
        List<BigInteger> config = getConfig(configJSON, pricingInfo);

        adjustGasPayment(pricingInfo.currency);

        if (rate > 0) {
            transfer(rate * 100, pricingInfo.currency);
        }

        try {
            mContract.createOffer(
                    tradeId,
                    planId,
                    amount,
                    BigInteger.ONE, // fee
                    BigInteger.valueOf(offer.getExpiry().toDate().getTime() / 1000),
                    BigInteger.valueOf(reservation.getStartTime()),
                    initialDeposit,
                    userAddresses,
                    config,
                    referrers,
                    new ArrayList<>(0),
                    Numeric.hexStringToByteArray(offer.getPriceSignature())
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
            token.approve(mContract.getContractAddress(), amount).send();
            token.transfer(mContract.getContractAddress(), amount).send();
        } catch (Exception e) {
            if (CeloSDK.isErrorCausedByInsufficientFunds(e)) {
                throw new CeloException(CeloError.INSUFFICIENT_BALANCE, e);
            }

            throw new CeloException(CeloError.NETWORK_ERROR, e);
        }
    }

    public void startService(com.amplifyframework.datastore.generated.model.Offer offer, Reservation reservation, String consumerAddress) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));

        PricingInfo pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));

        String purchasePlanType = reservation.getPurchasedPlanType();

        int rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricingInfo.price;
                break;
            case PurchasePlanTypes.BUNDLE:
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = 0;
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided."); // TODO not runtime exception! (has repetitions in this class)
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (rate) * 100);

        adjustGasPayment(pricingInfo.currency);

        try {
            mContract.startService(tradeId, offer.getWalletAddress(), consumerAddress, amount, BigInteger.ONE).send();
        } catch (Exception e) {
            if (e instanceof TransactionException) {
                throw new CeloException(null, e);
            }
            else {
                throw new CeloException(CeloError.NETWORK_ERROR, e);
            }
        }
    }

    public void finishService(com.amplifyframework.datastore.generated.model.Offer offer, Reservation reservation, String consumerAddress) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));

        PricingInfo pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));

        String purchasePlanType = reservation.getPurchasedPlanType();

        int rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricingInfo.price;
                break;
            case PurchasePlanTypes.BUNDLE:
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = 0;
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided."); // TODO not runtime exception! (has repetitions in this class)
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) rate * 100);

        adjustGasPayment(pricingInfo.currency);

        try {
            mContract.release(tradeId, offer.getWalletAddress(), consumerAddress, amount, BigInteger.ONE).send();
        } catch (Exception e) {
            if (e instanceof TransactionException) {
                throw new CeloException(null, e);
            }
            else {
                throw new CeloException(CeloError.NETWORK_ERROR, e);
            }
        }
    }

    public void cancelService(com.amplifyframework.datastore.generated.model.Offer offer, Reservation reservation, String consumerAddress, boolean consumerCancelled) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));

        PricingInfo pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));

        String purchasePlanType = reservation.getPurchasedPlanType();

        int rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = pricingInfo.price;
                break;
            case PurchasePlanTypes.BUNDLE:
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = 0;
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided."); // TODO not runtime exception! (has repetitions in this class)
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) rate * 100);

        adjustGasPayment(pricingInfo.currency);

        try {
            if (consumerCancelled) {
                mContract.consumerCancel(tradeId, offer.getWalletAddress(), consumerAddress, amount, BigInteger.ONE).send();
            }
            else {
                mContract.serviceProviderCancel(tradeId, offer.getWalletAddress(), consumerAddress, amount, BigInteger.ONE).send();
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

    private static List<BigInteger> getConfig(JSONObject configJSON, PricingInfo pricingInfo) throws JSONException {
        List<BigInteger> config = new ArrayList<>(8);

        BigInteger hours1 = new BigInteger(configJSON.getString(OfferUtils.CANCEL_HOURS1)).multiply(SIXTY);
        BigInteger percent1 = new BigInteger(configJSON.getString(OfferUtils.CANCEL_PERCENT1));
        BigInteger hours2 = new BigInteger(configJSON.getString(OfferUtils.CANCEL_HOURS2)).multiply(SIXTY);
        BigInteger percent2 = new BigInteger(configJSON.getString(OfferUtils.CANCEL_PERCENT2));
        BigInteger delayTime = new BigInteger(configJSON.getString(OfferUtils.DELAY_TIME));
        BigInteger delayPercent = new BigInteger(configJSON.getString(OfferUtils.DELAY_PERCENT));

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
        config.add(new BigInteger(configJSON.getString(OfferUtils.PROMOTION_RATE)));

        return config;
    }

    private static List<BigInteger> getBundleConfig(PricingInfo pricingInfo, int promotionPercent) {
        BigInteger price = BigInteger.valueOf(pricingInfo.price * (100 - pricingInfo.bundleDiscountPercent))
                .multiply(CurrencyUtil.WEI).divide(CurrencyUtil.ONE_HUNDRED);

        return Arrays.asList(
                price,
                BigInteger.valueOf(0),
                BigInteger.valueOf(pricingInfo.bundleCount),
                BigInteger.valueOf(4),
                BigInteger.ZERO //BigInteger.valueOf(promotionPercent)
        );
    }

    private static List<BigInteger> getSubscriptionConfig(PricingInfo pricingInfo, int promotionPercent) {
        return Arrays.asList(
                BigInteger.ZERO,
                BigInteger.valueOf(pricingInfo.subscriptionPeriod == null ? 0 : (pricingInfo.subscriptionPeriod.equals(PriceInputItem.SUBSCRIPTION_PERIODS[0]) ? 1 : 2)),
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
