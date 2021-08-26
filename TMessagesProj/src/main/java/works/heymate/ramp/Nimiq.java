package works.heymate.ramp;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.HeymateConfig;
import org.web3j.crypto.Hash;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import works.heymate.core.APICallback;
import works.heymate.core.Utils;

public class Nimiq {

    public static final String ASSET_EUR = "EUR";

    public static final String TOKEN_CEUR = "cEUR";
    public static final String TOKEN_CUSD = "cUSD";


    static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final int HASH_BYTES = 32;

    private static final String BASE_URL_DEVELOPMENT = "https://api-sandbox.nimiqoasis.com/v1";
    private static final String BASE_URL_PRODUCTION = "https://oasis.ten31.com/v1";

    private static final String HM_BASE_URL_ALFAJORES = "https://xty3xf0nra.execute-api.us-east-1.amazonaws.com/dev";
    private static final String HM_BASE_URL_MAIN = "https://cuth5smag8.execute-api.us-east-1.amazonaws.com/prod";

    private static final String BASE_URL = HeymateConfig.MAIN_NET ? BASE_URL_PRODUCTION : BASE_URL_DEVELOPMENT;
    private static final String HM_BASE_URL = HeymateConfig.MAIN_NET ? HM_BASE_URL_MAIN : HM_BASE_URL_ALFAJORES;

    private static final String PREPARE_URL = BASE_URL + "/htlc";
    private static final String REVEAL_URL = HM_BASE_URL + "/htlc/custodian";
    private static final String CHECK_URL = BASE_URL + "/htlc/";
    private static final String CLEAR_URL = BASE_URL_DEVELOPMENT + "/mock/clear/";

    public static void checkStatus(String id, APICallback<HTLC> callback) {
        new Thread() {

            @Override
            public void run() {
                try {
                    HTLC htlc = getHTLC(id);

                    Utils.runOnUIThread(() -> callback.onAPICallResult(true, htlc, null));
                } catch (Exception exception) {
                    Utils.runOnUIThread(() -> callback.onAPICallResult(false, null, exception));
                }
            }

        }.start();
    }

    public static void createRequest(double amount, String asset, String currency, String address, APICallback<HTLC> callback) {
        new Thread() {

            @Override
            public void run() {
                byte[] bPreImage = new byte[HASH_BYTES];
                new SecureRandom().nextBytes(bPreImage);

                String preImage = Base64.encodeToString(bPreImage, Base64.NO_WRAP | Base64.URL_SAFE);

                long expire = System.currentTimeMillis() + 60L * 60L * 1000L;

                try {
                    HTLC htlc = prepare(amount, asset, bPreImage, expire);
                    reveal(htlc.id, preImage, currency, address);

                    Utils.runOnUIThread(() -> callback.onAPICallResult(true, htlc, null));
                } catch (Exception exception) {
                    Utils.runOnUIThread(() -> callback.onAPICallResult(false, null, exception));
                }
            }

        }.start();
    }

    public static void testClear(String contractId, APICallback<Integer> callback) {
        new Thread() {

            @Override
            public void run() {
                try {
                    int responseCode = testClear(contractId);

                    if (callback != null) {
                        Utils.runOnUIThread(() -> callback.onAPICallResult(true, responseCode, null));
                    }
                } catch (Exception exception) {
                    if (callback != null) {
                        Utils.runOnUIThread(() -> callback.onAPICallResult(false, null, exception));
                    }
                }
            }

        }.start();
    }

    private static HTLC prepare(double amount, String asset, byte[] preImage, long expire) throws Exception {
        byte[] bHash = Hash.sha256(preImage);
        String hash = Base64.encodeToString(bHash, Base64.NO_WRAP | Base64.URL_SAFE);

        SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US);
        String sExpire = dateFormat.format(new Date(expire));

        JSONObject jBody = new JSONObject();

        try {
            jBody.put("asset", asset);
            jBody.put("amount", amount);

            JSONObject jBeneficiary = new JSONObject();
            jBeneficiary.put("kty", "EC");
            jBeneficiary.put("crv", "P-256");
            jBeneficiary.put("x", "OM2BiC4_OrrKZaPZu4RZN1qdZg3WIiC-bYQgpCyRNRM");
            jBeneficiary.put("y", "MgeeLyb-PtTMMB-lSWiO2ke2WQZBGI4rZKB_GVYQMxU");
            jBody.put("beneficiary", jBeneficiary);

            JSONObject jHash = new JSONObject();
            jHash.put("algorithm", "sha256");
            jHash.put("value", hash);
            jBody.put("hash", jHash);

            JSONObject jPreImage = new JSONObject();
            jPreImage.put("size", HASH_BYTES);
            jBody.put("preimage", jPreImage);

            jBody.put("expires", sExpire);
            jBody.put("includeFee", false);
        } catch (JSONException e) { }

        HttpURLConnection connection = null;

        HTLC result = null;
        Exception exception = null;

        try {
            connection = (HttpURLConnection) new URL(PREPARE_URL).openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(jBody.toString().getBytes());

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                String sHTLC = readStream(connection.getInputStream());

                try {
                    JSONObject jHTLC = new JSONObject(sHTLC);

                    result = new HTLC(jHTLC);
                } catch (JSONException e) {
                    exception = e;
                }
            }
            else {
                exception = new Exception("Received response code: " + responseCode);
            }
        } catch (IOException e) {
            exception = e;
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Throwable t) { }
        }

        if (exception != null) {
            throw exception;
        }

        return result;
    }

    private static void reveal(String contractId, String preimage, String currency, String address) throws Exception {
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("contractId", contractId);
            jBody.put("preimage", preimage);
            jBody.put("currency", currency);
            jBody.put("userAddress", address);
        } catch (JSONException e) { }

        Exception exception = null;

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(REVEAL_URL).openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(jBody.toString().getBytes());

            InputStream response = connection.getInputStream();

            if (response == null) {
                response = connection.getErrorStream();
            }

            String sResponse = readStream(response);

            JSONObject jResponse = new JSONObject(sResponse);

            boolean success = jResponse.getBoolean("success");
            String message = jResponse.getString("message");

            if (!success) {
                exception = new Exception(message);
            }
        } catch (IOException e) {
            exception = e;
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Throwable t) { }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private static HTLC getHTLC(String id) throws Exception {
        HTLC htlc = null;
        Exception exception = null;

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(CHECK_URL + id).openConnection();

            InputStream response = connection.getInputStream();

            if (response == null) {
                response = connection.getErrorStream();
            }

            String sHTLC = readStream(response);

            JSONObject jHTLC = new JSONObject(sHTLC);

            try {
                htlc = new HTLC(jHTLC);
            } catch (JSONException e) {
                exception = e;
            }
        } catch (IOException e) {
            exception = e;
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Throwable t) { }
        }

        if (exception != null) {
            throw exception;
        }

        return htlc;
    }

    private static int testClear(String id) throws Exception {
        int responseCode = 0;
        Exception exception = null;

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(CLEAR_URL + id).openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            exception = e;
        }

        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Throwable t) { }
        }

        if (exception != null) {
            throw exception;
        }

        return responseCode;
    }

    private static String readStream(InputStream stream) throws IOException {
        Scanner scanner = new Scanner(stream);

        StringBuilder sb = new StringBuilder();

        while (scanner.hasNext()) sb.append(scanner.next());

        return sb.toString();
    }

}
