package works.heymate.ramp.alphafortress;

import org.json.JSONException;
import org.json.JSONObject;

import works.heymate.core.APICallback;
import works.heymate.core.Currency;
import works.heymate.core.Utils;
import works.heymate.util.SimpleNetworkCall;

public class AlphaWallet {

    private static final String WALLET_URL = AlphaFortressness.BASE_URL + "wallet-addresses?source_currency.name=";

    static class WalletInfo {

        public final long currencyId;
        public final String address;

        private WalletInfo(JSONObject jAddress) throws JSONException {
            currencyId = jAddress.getJSONObject("source_currency").getLong("id");
            address = jAddress.getString("address");
        }

    }

    static void getWalletAddress(Currency currency, APICallback<WalletInfo> callback) {
        String currencyName;

        if (Currency.USD.equals(currency)) {
            currencyName = "cUSD";
        }
        else if (Currency.EUR.equals(currency)) {
            currencyName = "cEUR";
        }
        else {
            Utils.postOnUIThread(() -> callback.onAPICallResult(false, null, null));
            return;
        }

        AlphaToken.get().getToken((success, token, exception) -> {
            if (success) {
                SimpleNetworkCall.callAsync(result -> {
                    if (result.arrayResponse != null) {
                        try {
                            callback.onAPICallResult(true, new WalletInfo(result.arrayResponse.getJSONObject(0)), null);
                        } catch (JSONException e) {
                            callback.onAPICallResult(false, null, e);
                        }
                    }
                    else {
                        callback.onAPICallResult(false, null, null);
                    }
                }, WALLET_URL + currencyName, null, "Authorization", "Bearer " + token);
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

}
