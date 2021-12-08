package works.heymate.ramp.alphafortress;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.TG2HM;

import works.heymate.core.APICallback;
import works.heymate.core.Utils;
import works.heymate.util.SimpleNetworkCall;

public class AlphaTransaction {

    private static final String TRANSACTION_URL = AlphaFortressness.BASE_URL + "transactions";

    private static final String KEY_TRANSACTION_ID = "alphafortress_transaction_id";

    public static class Transaction {

        public final long id;
        public final String transactionHash;
        public final long sourceAmount;
        public final long destinationAmount;
        public final Boolean isPaid;

        private Transaction(JSONObject jTransaction) throws JSONException {
            id = jTransaction.getLong("id");
            transactionHash = jTransaction.isNull("txnHash") ? null : jTransaction.getString("txnHash");
            sourceAmount = jTransaction.getLong("sourceAmount");
            destinationAmount = jTransaction.getLong("destinationAmount");
            isPaid = jTransaction.isNull("isPaid") ? null : jTransaction.getBoolean("isPaid");
        }

    }

    static boolean hasPendingTransaction() {
        return HeymateConfig.getForAccount(TG2HM.getCurrentPhoneNumber()).get(KEY_TRANSACTION_ID) != null;
    }

    static void clearPendingTransaction() {
        HeymateConfig.getForAccount(TG2HM.getCurrentPhoneNumber()).set(KEY_TRANSACTION_ID, null);
    }

    static void getPendingTransaction(APICallback<Transaction> callback) {
        String transactionId = HeymateConfig.getForAccount(TG2HM.getCurrentPhoneNumber()).get(KEY_TRANSACTION_ID);

        if (transactionId == null) {
            Utils.postOnUIThread(() -> callback.onAPICallResult(true, null, null));
            return;
        }

        AlphaToken.get().getToken((success, token, exception) -> {
            if (token != null) {
                String url = TRANSACTION_URL + "?id=" + transactionId;

                SimpleNetworkCall.callAsync(result -> {
                    if (result.arrayResponse != null) {
                        try {
                            for (int i = 0; i < result.arrayResponse.length(); i++) {
                                JSONObject jTransaction = result.arrayResponse.getJSONObject(i);

                                if (Long.parseLong(transactionId) == jTransaction.getLong("id")) {
                                    callback.onAPICallResult(true, new Transaction(jTransaction), null);
                                    return;
                                }
                            }

                            callback.onAPICallResult(true, null, null);
                        } catch (JSONException e) {
                            callback.onAPICallResult(false, null, e);
                        }
                    }
                    else {
                        callback.onAPICallResult(false, null, result.exception);
                    }
                }, url, null, "Authorization", "Bearer " + token);
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

    static void newTransaction(String walletAddress, AlphaWallet.WalletInfo walletInfo,
                               long beneficiaryId, BeneficiaryModel beneficiary,
                               long sourceAmount, long destinationAmount,
                               APICallback<Transaction> callback) {
        AlphaToken.get().getToken((success, token, exception) -> {
            if (token != null) {
                JSONObject body = new JSONObject();

                try {
                    body.put("fromAddress", walletAddress);
                    body.put("sourceCurrency", walletInfo.currencyId);
                    body.put("destinationCurrency", beneficiary.id);
                    body.put("sourceAmount", sourceAmount);
                    body.put("destinationAmount", destinationAmount);
                    body.put("benificiary", beneficiaryId);
                    body.put("fees", 0);
                } catch (JSONException e) { }

                SimpleNetworkCall.callAsync(result -> {
                    if (result.response != null) {
                        try {
                            Transaction transaction = new Transaction(result.response);

                            HeymateConfig.getForAccount(TG2HM.getCurrentPhoneNumber()).set(KEY_TRANSACTION_ID, String.valueOf(transaction.id));

                            callback.onAPICallResult(true, transaction, null);
                        } catch (JSONException e) {
                            callback.onAPICallResult(false, null, e);
                        }
                    }
                    else {
                        callback.onAPICallResult(false, null, result.exception);
                    }
                }, TRANSACTION_URL, body, "Authorization", "Bearer " + token);
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

    static void completeTransaction(String walletAddress, String transactionHash, APICallback<Transaction> callback) {
        AlphaToken.get().getToken((success, token, exception) -> {
            if (token != null) {
                String url = TRANSACTION_URL + "/" + HeymateConfig.getForAccount(TG2HM.getCurrentPhoneNumber()).get(KEY_TRANSACTION_ID);

                JSONObject body = new JSONObject();

                try {
                    body.put("txnHash", transactionHash);
                    body.put("fromAddress", walletAddress);
                } catch (JSONException e) { }

                SimpleNetworkCall.callAsync(result -> {
                    if (result.response != null) {
                        try {
                            Transaction transaction = new Transaction(result.response);

                            callback.onAPICallResult(true, transaction, null);
                        } catch (JSONException e) {
                            callback.onAPICallResult(false, null, e);
                        }
                    }
                    else {
                        callback.onAPICallResult(false, null, result.exception);
                    }
                }, "PUT", url, body, "Authorization", "Bearer " + token);
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

}
