package works.heymate.api;

import java.util.List;

class Token {

    private static final String KEY_TOKEN = "api_token";
    private static final String KEY_TOKEN_EXPIRY = "api_token_expiry";
    private static final String KEY_REFRESH_TOKEN = "api_refresh_token";

    interface TokenCallback {

        void onToken(String token, Exception exception);

    }

    static void get(TokenCallback callback) {

    }

}
