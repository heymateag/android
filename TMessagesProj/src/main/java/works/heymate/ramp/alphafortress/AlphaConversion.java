package works.heymate.ramp.alphafortress;

import org.json.JSONException;

import works.heymate.core.APICallback;
import works.heymate.core.Currency;
import works.heymate.core.Utils;
import works.heymate.util.SimpleNetworkCall;

public class AlphaConversion {

    private static final String CONVERSION_URL = AlphaFortressness.BASE_URL + "conversions?source={{src}}&destination={{dst}}";

    static void getConversionRate(Currency fromCurrency, Currency toCurrency, APICallback<Float> callback) {
        String src;
        String dst;

        if (Currency.USD.equals(fromCurrency)) {
            src = "cUSD";
        }
        else if (Currency.EUR.equals(fromCurrency)) {
            src = "cEUR";
        }
        else if (Currency.REAL.equals(fromCurrency)) { // TODO really?
            src = "cREAL";
        }
        else {
            Utils.postOnUIThread(() -> callback.onAPICallResult(false, -1F, null));
            return;
        }

        if (Currency.USD.equals(toCurrency)) {
            dst = "USD";
        }
        else if (Currency.EUR.equals(toCurrency)) {
            dst = "EUR";
        }
        else if (Currency.REAL.equals(toCurrency)) { // TODO really?
            dst = "REAL";
        }
        else if (Currency.INR.equals(toCurrency)) {
            dst = "INR";
        }
        else {
            Utils.postOnUIThread(() -> callback.onAPICallResult(false, -1F, null));
            return;
        }

        String url = CONVERSION_URL.replace("{{src}}", src).replace("{{dst}}", dst);

        SimpleNetworkCall.callAsync(result -> {
            if (result.arrayResponse != null) {
                if (result.arrayResponse.length() == 0) {
                    callback.onAPICallResult(true, -1F, null);
                }
                else {
                    try {
                        float rate = (float) result.arrayResponse.getJSONObject(0).getDouble("rate");

                        callback.onAPICallResult(true, rate, null);
                    } catch (JSONException e) {
                        callback.onAPICallResult(false, -1F, e);
                    }
                }
            }
            else {
                callback.onAPICallResult(false, -1F, result.exception);
            }
        }, url, null);
    }

}
