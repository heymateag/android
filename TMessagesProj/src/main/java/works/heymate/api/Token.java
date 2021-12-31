package works.heymate.api;

import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.TG2HM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import works.heymate.core.Utils;
import works.heymate.util.SimpleNetworkCall;

class Token {

    private static final String REFRESH_TOKEN_URL = HeymateConfig.API_BASE_URL + "/";// TODO
    private static final String REGISTER_URL = HeymateConfig.API_BASE_URL + "/auth/register";
    private static final String LOGIN_URL = HeymateConfig.API_BASE_URL + "/auth/login";

    private static final String FAKE_PASSWORD = "123456";

    private static final String KEY_TOKEN = "api_token";
    private static final String KEY_TOKEN_EXPIRY = "api_token_expiry";
    private static final String KEY_REFRESH_TOKEN = "api_refresh_token";

    interface TokenCallback {

        void onToken(String token, Exception exception);

    }

    private static final List<TokenCallback> sCallbacks = new LinkedList<>();
    private static boolean sGettingToken = false;

    synchronized static void get(TokenCallback callback) {
        if (sGettingToken) {
            sCallbacks.add(callback);
            return;
        }

        String phoneNumber = TG2HM.getCurrentPhoneNumber();

        if (phoneNumber == null) {
            Utils.runOnUIThread(() -> callback.onToken(null, new Exception("No accounts yet")));
            return;
        }

        HeymateConfig config = HeymateConfig.getForAccount(phoneNumber);

        String token = config.get(KEY_TOKEN);

        if (token != null) {
            long tokenExpiry = Long.parseLong(config.get(KEY_TOKEN_EXPIRY));

            if (tokenExpiry < System.currentTimeMillis() / 1000 - 5 * 60) {
                Utils.runOnUIThread(() -> callback.onToken(token, null));
                return;
            }
        }

        sGettingToken = true;

        sCallbacks.add(callback);

        String refreshToken = config.get(KEY_REFRESH_TOKEN);

        if (refreshToken != null) {
            // TODO

            // If fails
            config.set(KEY_TOKEN, null);
            config.set(KEY_TOKEN_EXPIRY, null);
            config.set(KEY_REFRESH_TOKEN, null);
            sCallbacks.remove(callback);
            sGettingToken = false;
            get(callback);
            return;
        }

        register(phoneNumber, (registerResult, exception) -> {
            if (registerResult != null) {
                login(phoneNumber, (newToken, exception1) -> {
                    if (newToken != null) {
                        notifyNewToken(newToken);
                    }
                    else {
                        notifyTokenFailed(exception1);
                    }
                });
            }
            else {
                notifyTokenFailed(exception);
            }
        });
    }

    synchronized static private void notifyNewToken(String newToken) {
        sGettingToken = false;

        List<TokenCallback> callbacks = new ArrayList<>(sCallbacks);
        sCallbacks.clear();

        for (TokenCallback callback: callbacks) {
            callback.onToken(newToken, null);
        }
    }

    synchronized static private void notifyTokenFailed(Exception exception) {
        sGettingToken = false;

        List<TokenCallback> callbacks = new ArrayList<>(sCallbacks);
        sCallbacks.clear();

        for (TokenCallback callback: callbacks) {
            callback.onToken(null, exception);
        }
    }

    private static void login(String phoneNumber, TokenCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("phone_number", phoneNumber);
        body.put("password", FAKE_PASSWORD);

        SimpleNetworkCall.callMapAsync(result -> {
            if (result.responseCode == 201 && result.response != null) {
                APIObject response = new APIObject(result.response);

                String token = response.getObject("idToken").getString("jwtToken");
                long tokenExpiry = response.getObject("idToken").getObject("payload").getLong("exp");
                String refreshToken = response.getObject("refreshToken").getString("token");

                HeymateConfig config = HeymateConfig.getForAccount(phoneNumber);
                config.set(KEY_TOKEN, token);
                config.set(KEY_TOKEN_EXPIRY, String.valueOf(tokenExpiry));
                config.set(KEY_REFRESH_TOKEN, refreshToken);

                callback.onToken(token, null);
            }
            else {
                callback.onToken(null, result.exception);
            }
        }, "POST", LOGIN_URL, body);
    }

    private static void register(String phoneNumber, TokenCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("phone_number", phoneNumber);
        body.put("password", FAKE_PASSWORD);

        SimpleNetworkCall.callMapAsync(result -> {
            if (result.responseCode == 201 || result.responseCode == 400) {
                callback.onToken("ok", null);
            }
            else {
                callback.onToken(null, result.exception);
            }
        }, "POST", REGISTER_URL, body);
    }

}
