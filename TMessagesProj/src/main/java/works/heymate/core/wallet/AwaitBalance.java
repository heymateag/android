package works.heymate.core.wallet;

import android.os.Handler;
import android.os.Looper;

import org.celo.contractkit.wrapper.StableTokenWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.HeymateConfig;

import java.math.BigInteger;

import works.heymate.celo.CeloUtils;
import works.heymate.core.Currency;
import works.heymate.core.Utils;
import works.heymate.util.SimpleNetworkCall;

public class AwaitBalance {

    private static final long TOTAL_DURATION = 120_000L;
    private static final long CHECK_INTERVAL = 5_000L;

    private static final String BASE_URL = HeymateConfig.MAIN_NET ? "https://explorer.celo.org/" : "https://alfajores-blockscout.celo-testnet.org/";

    public interface Callback {

        void onResult(boolean success);

    }

    public interface TransactionsCallback {

        void onTransactions(JSONArray jTransactions);

    }

    public static void on(Wallet wallet, Currency currency, Callback callback) {
        final long requestTime = System.currentTimeMillis();

        Utils.runOnUIThread(() -> wallet.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                StableTokenWrapper token = CeloUtils.getToken(contractKit, currency);

                final String tokenAddress = token.getContractAddress();

                checkReceipt(requestTime, wallet.getAddress(), tokenAddress, callback);
            }
            else {
                Utils.postOnUIThread(() -> callback.onResult(false));
            }
        }));
    }

    private static void checkReceipt(long requestTime, String walletAddress, String tokenAddress, Callback callback) {
        String url = BASE_URL + "api?module=account&action=tokentx&page=0&offset=1&address=" + walletAddress;

        SimpleNetworkCall.callAsync(result -> {
            if (result.response != null) {
                try {
                    JSONArray jTransactions = result.response.getJSONArray("result");

                    if (jTransactions.length() > 0) {
                        JSONObject jTransaction = jTransactions.getJSONObject(0);

                        String from = jTransaction.getString("from");
                        String to = jTransaction.getString("to");
                        long timestamp = Long.parseLong(jTransaction.getString("timeStamp")) * 1000L;
                        BigInteger value = new BigInteger(jTransaction.getString("value"));
                        String contract = jTransaction.getString("contractAddress");

                        if (timestamp > requestTime && tokenAddress.equals(contract)) {
                            callback.onResult(true);
                            return;
                        }
                    }

                    long now = System.currentTimeMillis();

                    if (now > requestTime + TOTAL_DURATION) {
                        callback.onResult(false);
                        return;
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> checkReceipt(requestTime, walletAddress, tokenAddress, callback), CHECK_INTERVAL);
                } catch (JSONException e) {
                    callback.onResult(false);
                }
            }
            else {
                callback.onResult(false);
            }
        }, url, null);
    }

    public static void getLatestTransactions(String walletAddress, TransactionsCallback callback) {
        String url = BASE_URL + "api?module=account&action=tokentx&page=0&offset=20&address=" + walletAddress;

        SimpleNetworkCall.callAsync(result -> {
            if (result.response != null) {
                try {
                    JSONArray jTransactions = result.response.getJSONArray("result");

                    callback.onTransactions(jTransactions);
                } catch (JSONException e) {
                    callback.onTransactions(null);
                }
            }
            else {
                callback.onTransactions(null);
            }
        }, url, null);
    }

}
