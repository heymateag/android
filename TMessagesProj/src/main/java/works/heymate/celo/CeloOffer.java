package works.heymate.celo;

import org.celo.contractkit.ContractKit;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
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

public class CeloOffer {

    private static final BigInteger SIXTY = BigInteger.valueOf(60L);

    private final ContractKit mContractKit;
    private final Offer mContract;

    public CeloOffer(String address, ContractKit contractKit) {
        mContractKit = contractKit;
        mContract = Offer.load(address, contractKit.web3j, contractKit.transactionManager, new DefaultGasProvider());
    }

    public String createOfferSignature(String rate, String termsConfig) throws Exception {
        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (Double.parseDouble(rate) * 100));

        JSONObject configJSON = new JSONObject(termsConfig);

        BigInteger initialDeposit = new BigInteger(configJSON.getString(OfferUtils.INITIAL_DEPOSIT));
        BigInteger[] config = getConfig(configJSON).toArray(new BigInteger[0]);

        Sign.SignatureData signatureData = Sign.signPrefixedMessage(getBytes(amount, initialDeposit, config), mContractKit.transactionManager.getCredentials().getEcKeyPair());

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
    public void create(org.telegram.ui.Heymate.AmplifyModels.Offer offer, String consumerAddress, long startTime) throws CeloException, JSONException {
        byte[] tradeId = new byte[16];
        new SecureRandom().nextBytes(tradeId);

        BigInteger amount = CurrencyUtil.centsToBlockChainValue((long) (Double.parseDouble(offer.getRate()) * 100));

        BigInteger initialDeposit;

        List<String> userAddresses = Arrays.asList(offer.getServiceProviderAddress(), consumerAddress);

        JSONObject configJSON = new JSONObject(offer.getTermsConfig());

        initialDeposit = new BigInteger(configJSON.getString(OfferUtils.INITIAL_DEPOSIT));
        List<BigInteger> config = getConfig(configJSON);

        try {
            mContractKit.contracts.getStableToken().approve(mContract.getContractAddress(), amount).send();
        } catch (Exception e) {
            if (e instanceof TransactionException) {
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
                    BigInteger.valueOf(startTime),
                    initialDeposit,
                    userAddresses,
                    config,
                    Numeric.hexStringToByteArray(offer.getServiceProviderSignature()),
                    amount
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
        config.add(BigInteger.ZERO);
        config.add(BigInteger.ZERO);

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
