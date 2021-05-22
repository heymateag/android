package works.heymate.celo;

import com.amplifyframework.datastore.generated.model.PurchasedPlan;
import com.amplifyframework.datastore.generated.model.Reservation;

import org.celo.contractkit.ContractKit;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;
import org.web3j.crypto.Sign;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
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

    public String createOfferSignature(String address, String rate, JSONObject termsConfig) throws Exception {
        byte[] serviceProviderAddress = Numeric.hexStringToByteArray(address);

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (Double.parseDouble(rate) * 100));

        BigInteger initialDeposit = new BigInteger(termsConfig.getString(OfferUtils.INITIAL_DEPOSIT));
        BigInteger[] config = getConfig(termsConfig).toArray(new BigInteger[0]);

        Sign.SignatureData signatureData = Sign.signPrefixedMessage(getBytes(serviceProviderAddress, amount, initialDeposit, config), mContractKit.transactionManager.getCredentials().getEcKeyPair());

        byte[] signature = getBytes(signatureData.getV(), signatureData.getR(), signatureData.getS());

        return Numeric.toHexString(signature);
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
    public void create(com.amplifyframework.datastore.generated.model.Offer offer, String consumerAddress,
                       Reservation reservation, PurchasedPlan purchasePlan, List<String> referrers) throws CeloException, JSONException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));

        PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));

        String purchasePlanType = reservation.getPurchasedPlanType();

        String rate;

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                rate = String.valueOf(pricingInfo.price);
                break;
            case PurchasePlanTypes.BUNDLE:
                rate = String.valueOf(pricingInfo.price * pricingInfo.bundleCount * (100 - pricingInfo.bundleDiscountPercent) / 100);
                break;
            case PurchasePlanTypes.SUBSCRIPTION:
                rate = "0";
                break;
            default:
                throw new IllegalArgumentException("Purchase plan type not provided.");
        }

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (Double.parseDouble(rate) * 100));

        BigInteger initialDeposit;

        List<String> userAddresses = Arrays.asList(offer.getServiceProviderAddress(), consumerAddress);

        JSONObject configJSON = new JSONObject(offer.getTermsConfig());

        initialDeposit = new BigInteger(configJSON.getString(OfferUtils.INITIAL_DEPOSIT));
        List<BigInteger> config = getConfig(configJSON);

        try {
            mContractKit.contracts.getStableToken().approve(mContract.getContractAddress(), amount).send();
            mContractKit.contracts.getStableToken().transfer(mContract.getContractAddress(), amount).send();
        } catch (Exception e) {
            if (CeloSDK.isErrorCausedByInsufficientFunds(e)) {
                throw new CeloException(CeloError.INSUFFICIENT_BALANCE, e);
            }

            throw new CeloException(CeloError.NETWORK_ERROR, e);
        }

        try {
            mContract.createOffer(
                    tradeId,
                    amount,
                    BigInteger.ONE, // fee
                    BigInteger.valueOf(offer.getExpiry().toDate().getTime() / 1000),
                    BigInteger.valueOf(reservation.getStartTime()),
                    initialDeposit,
                    userAddresses,
                    config,
                    referrers,
                    new ArrayList<>(0),
                    Numeric.hexStringToByteArray(offer.getServiceProviderSignature())
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

    public void startService(com.amplifyframework.datastore.generated.model.Offer offer, Reservation reservation, String consumerAddress) throws CeloException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));
        // TODO
//        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (Double.parseDouble(offer.getRate()) * 100));
//
//        try {
//            mContract.startService(tradeId, offer.getServiceProviderAddress(), consumerAddress, amount, BigInteger.ONE).send();
//        } catch (Exception e) {
//            if (e instanceof TransactionException) {
//                throw new CeloException(null, e);
//            }
//            else {
//                throw new CeloException(CeloError.NETWORK_ERROR, e);
//            }
//        }
    }

    public void finishService(com.amplifyframework.datastore.generated.model.Offer offer, Reservation reservation, String consumerAddress) throws CeloException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));
        // TODO
//        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (Double.parseDouble(offer.getRate()) * 100));
//
//        try {
//            mContract.release(tradeId, offer.getServiceProviderAddress(), consumerAddress, amount, BigInteger.ONE).send();
//        } catch (Exception e) {
//            if (e instanceof TransactionException) {
//                throw new CeloException(null, e);
//            }
//            else {
//                throw new CeloException(CeloError.NETWORK_ERROR, e);
//            }
//        }
    }

    public void cancelService(com.amplifyframework.datastore.generated.model.Offer offer, Reservation reservation, String consumerAddress, boolean consumerCancelled) throws CeloException {
        byte[] tradeId = Numeric.hexStringToByteArray(reservation.getId().replaceAll("-", ""));
        // TODO
//        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (Double.parseDouble(offer.getRate()) * 100));
//
//        try {
//            if (consumerCancelled) {
//                mContract.consumerCancel(tradeId, offer.getServiceProviderAddress(), consumerAddress, amount, BigInteger.ONE).send();
//            }
//            else {
//                mContract.serviceProviderCancel(tradeId, offer.getServiceProviderAddress(), consumerAddress, amount, BigInteger.ONE).send();
//            }
//        } catch (Exception e) {
//            if (e instanceof TransactionException) {
//                throw new CeloException(null, e);
//            }
//            else {
//                throw new CeloException(CeloError.NETWORK_ERROR, e);
//            }
//        }
    }

    private static List<BigInteger> getConfig(JSONObject configJSON) throws JSONException {
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

}
