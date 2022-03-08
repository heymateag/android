package works.heymate.ramp.alphafortress;

import org.celo.contractkit.CeloContract;
import org.celo.contractkit.wrapper.StableTokenWrapper;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.TG2HM;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

import works.heymate.celo.CurrencyUtil;
import works.heymate.core.APICallback;
import works.heymate.core.Currency;
import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;

/**
 * 1. Get JWT token
 * 2. Get destination currency requirements
 * 3. Show the UI to get user info
 *
 */
public class AlphaFortressness {

    private static final String MAIN_URL = "https://api.bitssa.com/";
    private static final String STAGING_URL = "https://api.staging.bitssa.com/";

    public static final String BASE_URL = HeymateConfig.PRODUCTION ? MAIN_URL : STAGING_URL;

    private static final String KEY_BENEFICIARY_ID = "alphafortress_beneficiary_id";

    public static void getConversionRate(Currency fromCurrency, Currency toCurrency, APICallback<Float> callback) {
        AlphaConversion.getConversionRate(fromCurrency, toCurrency, callback);
    }

    public static long applyRate(BigInteger weiValue, float rate) {
        int decimalFix = 1;

        float rawRate = rate;

        while (rawRate % 1 != 0) {
            rawRate *= 10;
            decimalFix *= 10;
        }

        return CurrencyUtil.blockChainValueToCents(weiValue.multiply(BigInteger.valueOf((long) rawRate)).divide(BigInteger.valueOf(decimalFix)));
    }

    public static BigInteger getConvertibleAmount(BigInteger weiValue, float rate) {
        int decimalFix = 1;

        float rawRate = rate;

        while (rawRate % 1 != 0) {
            rawRate *= 10;
            decimalFix *= 10;
        }

        BigInteger usableAmount = weiValue.subtract(CurrencyUtil.centsToBlockChainValue(10));

        if (usableAmount.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ZERO;
        }

        BigInteger bigRawRate = BigInteger.valueOf((long) rawRate);
        BigInteger bigDecimalFix = BigInteger.valueOf(decimalFix);

        BigInteger convertedValue = usableAmount.multiply(bigRawRate).divide(bigDecimalFix);

        BigInteger roundedValue = convertedValue.subtract(convertedValue.remainder(CurrencyUtil.centsToBlockChainValue(100)));

        return roundedValue.multiply(bigDecimalFix).divide(bigRawRate);
    }

    public static void getBeneficiaryModel(Currency currency, APICallback<BeneficiaryModel> callback) {
        BeneficiaryModel.get(currency, callback);
    }

    public static boolean hasPendingTransaction() {
        return AlphaTransaction.hasPendingTransaction();
    }

    public static void getPendingTransaction(APICallback<AlphaTransaction.Transaction> callback) {
        AlphaTransaction.getPendingTransaction(callback);
    }

    public static void clearPendingTransaction() {
        AlphaTransaction.clearPendingTransaction();
    }

    public static void sell(Currency sellCurrency, BigInteger amount, float rate, Currency beneficiaryCurrency, BeneficiaryModel model, APICallback<AlphaTransaction.Transaction> callback) {
        BeneficiaryModel.get(beneficiaryCurrency, (success, previousModel, exception) -> {
            if (previousModel != null) {
                if (previousModel.hasChanges(model)) {
                    BeneficiaryModel.createBeneficiary(beneficiaryCurrency, model, (success1, result, exception1) -> {
                        if (success1) {
                            sellWithBeneficiary(sellCurrency, amount, rate, beneficiaryCurrency, model, callback);
                        }
                        else {
                            callback.onAPICallResult(false, null, exception1);
                        }
                    });
                }
                else {
                    sellWithBeneficiary(sellCurrency, amount, rate, beneficiaryCurrency, model, callback);
                }
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

    private static void sellWithBeneficiary(Currency sellCurrency, BigInteger amount, float rate, Currency beneficiaryCurrency, BeneficiaryModel model, APICallback<AlphaTransaction.Transaction> callback) {
        long beneficiaryId = BeneficiaryModel.getBeneficiaryId(beneficiaryCurrency);

        AlphaWallet.getWalletAddress(sellCurrency, (success, walletInfo, exception) -> {
            if (walletInfo != null) {
                Wallet wallet = Wallet.get(ApplicationLoader.applicationContext, TG2HM.getCurrentPhoneNumber());

                double sourceAmount = CurrencyUtil.blockChainValueToCents(amount) / 100d;
                double destinationAmount = applyRate(amount, rate) / 100d;

                AlphaTransaction.newTransaction(wallet.getAddress(), walletInfo, beneficiaryId, model, sourceAmount, destinationAmount, (success1, result, exception1) -> {
                    if (result != null) {
                        wallet.getContractKit((success2, contractKit, errorCause) -> {
                            if (contractKit != null) {
                                StableTokenWrapper token;

                                if (Currency.USD.equals(sellCurrency)) {
                                    token = contractKit.contracts.getStableToken();
                                    contractKit.setFeeCurrency(CeloContract.StableToken);
                                }
                                else if (Currency.EUR.equals(sellCurrency)) {
                                    token = contractKit.contracts.getStableTokenEUR();
                                    contractKit.setFeeCurrency(CeloContract.StableTokenEUR);
                                }
                                else if (Currency.REAL.equals(sellCurrency)) {
                                    token = contractKit.contracts.getStableTokenBRL();
                                    contractKit.setFeeCurrency(CeloContract.StableTokenBRL);
                                }
                                else {
                                    AlphaTransaction.clearPendingTransaction();

                                    Utils.postOnUIThread(() -> callback.onAPICallResult(false, null, null));
                                    return;
                                }

                                try {
                                    TransactionReceipt receipt = token.transfer(walletInfo.address, amount).send();

                                    AlphaTransaction.completeTransaction(wallet.getAddress(), receipt.getTransactionHash(), (success3, transaction, exception2) -> {
                                        if (transaction != null) {
                                            callback.onAPICallResult(true, transaction, null);
                                        }
                                        else {
                                            // TODO ULTIMATE FAILURE IS HERE
                                            callback.onAPICallResult(false, null, null);
                                        }
                                    });
                                } catch (Exception e) {
                                    AlphaTransaction.clearPendingTransaction();

                                    Utils.postOnUIThread(() -> callback.onAPICallResult(false, null, e));
                                }
                            }
                            else {
                                AlphaTransaction.clearPendingTransaction();

                                Utils.postOnUIThread(() -> callback.onAPICallResult(false, null, errorCause));
                            }
                        });
                    }
                    else {
                        callback.onAPICallResult(false, null, exception1);
                    }
                });
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

}
