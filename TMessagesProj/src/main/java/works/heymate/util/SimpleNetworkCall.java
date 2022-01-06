package works.heymate.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import works.heymate.core.Utils;

public class SimpleNetworkCall {

    public interface NetworkCallCallback {

        void onNetworkCallResult(NetworkCallResult result);

    }

    public static class NetworkCallResult {

        public int responseCode;
        public JSONObject response;
        public JSONArray arrayResponse;
        public Exception exception;

    }

    private static final Executor sExecutor = new ThreadPoolExecutor(2, 5, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    public static void callMapAsync(NetworkCallCallback callback, String method, String url, Map<String, Object> body, String... headers) {
        callAsync(callback, method, url, Utils.mapToJSON(body), headers);
    }

    public static void callAsync(NetworkCallCallback callback, String url, JSONObject body, String... headers) {
        sExecutor.execute(() -> {
            NetworkCallResult result = call(url, body, headers);

            if (callback != null) {
                Utils.postOnUIThread(() -> callback.onNetworkCallResult(result));
            }
        });
    }

    public static void callAsync(NetworkCallCallback callback, String method, String url, JSONObject body, String... headers) {
        sExecutor.execute(() -> {
            NetworkCallResult result = call(method, url, body, headers);

            if (callback != null) {
                Utils.postOnUIThread(() -> callback.onNetworkCallResult(result));
            }
        });
    }

    public static NetworkCallResult call(String url, JSONObject body, String... headers) {
        return call(body == null ? "GET" : "POST", url, body, headers);
    }

    public static NetworkCallResult call(String method, String url, JSONObject body, String... headers) {
        NetworkCallResult result = new NetworkCallResult();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod(method);

            for (int i = 0; i < headers.length; i += 2) {
                connection.setRequestProperty(headers[i], headers[i + 1]);
            }

            if (body != null) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.getOutputStream().write(body.toString().getBytes());
            }

            result.responseCode = connection.getResponseCode();

            if (result.responseCode >= 200 && result.responseCode < 300) {
                String response = readStream(connection.getInputStream());

                try {
                    result.response = new JSONObject(response);
                } catch (JSONException ignore) {
                    result.arrayResponse = new JSONArray(response);
                }
            }

            connection.disconnect();
        } catch (IOException | JSONException e) {
            result.exception = e;
        }

        return result;
    }

    private static String readStream(InputStream stream) throws IOException {
        Scanner scanner = new Scanner(stream);

        StringBuilder sb = new StringBuilder();

        boolean first = true;

        while (scanner.hasNext()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(" ");
            }

            sb.append(scanner.next());
        }

        return sb.toString();
    }

}
