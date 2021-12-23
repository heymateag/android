package works.heymate.ramp.alphafortress;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.HeymateConfig;

import works.heymate.core.APICallback;
import works.heymate.core.Utils;
import works.heymate.util.SimpleNetworkCall;

class AlphaToken {

    private static final String TOKEN_URL = AlphaFortressness.BASE_URL + "auth/local";
    private static final String AUTH_IDENTIFIER = "yapukico@altmails.com";
    private static final String AUTH_PASSWORD = "thisisnotarealpassword";

    private static final String TOKEN = "alphafortress_token";
    private static final String TOKEN_EXPIRY = "alphafortress_token_expiry";

    private static AlphaToken mInstance = null;

    static AlphaToken get() {
        if (mInstance == null) {
            mInstance = new AlphaToken();
        }

        return mInstance;
    }

    private AlphaToken() {
    }

    void getToken(APICallback<String> callback) {
        if (System.currentTimeMillis() < getTokenExpiry()) {
            Utils.postOnUIThread(() -> callback.onAPICallResult(true, HeymateConfig.getGeneral().get(TOKEN), null));
            return;
        }

        JSONObject body = new JSONObject();

        try {
            body.put("identifier", AUTH_IDENTIFIER);
            body.put("password", AUTH_PASSWORD);
        } catch (JSONException e) { }

        SimpleNetworkCall.callAsync(result -> {
            if (result.response != null) {
                try {
                    String jwt = result.response.getString("jwt");

                    HeymateConfig.getGeneral().set(TOKEN, jwt);
                    HeymateConfig.getGeneral().set(TOKEN_EXPIRY, String.valueOf(System.currentTimeMillis() + 20L * 60L * 60L * 1000L));

                    callback.onAPICallResult(true, jwt, null);
                } catch (JSONException e) {
                    callback.onAPICallResult(false, null, e);
                }
            }
            else {
                callback.onAPICallResult(false, null, result.exception);
            }
        }, TOKEN_URL, body);
    }

    private long getTokenExpiry() {
        String sExpiry = HeymateConfig.getGeneral().get(TOKEN_EXPIRY);

        return sExpiry == null ? 0 : Long.parseLong(sExpiry);
    }

}
