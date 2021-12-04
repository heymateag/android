package works.heymate.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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

    public static void callAsync(NetworkCallCallback callback, String url, JSONObject body, String... headers) {
        new Thread() {

            @Override
            public void run() {
                NetworkCallResult result = call(url, body);

                Utils.postOnUIThread(() -> callback.onNetworkCallResult(result));
            }

        }.start();
    }

    public static NetworkCallResult call(String url, JSONObject body, String... headers) {
        NetworkCallResult result = new NetworkCallResult();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            for (int i = 0; i < headers.length; i += 2) {
                connection.setRequestProperty(headers[i], headers[i + 1]);
            }

            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
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

        while (scanner.hasNext()) sb.append(scanner.next());

        return sb.toString();
    }

}
