package works.heymate.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
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

    public static void callMapAsync(NetworkCallCallback callback, String method, String url, Map<String, Object> body, String... headers) {
        try {
            callAsync(callback, method, url, mapToObject(body), headers);
        } catch (JSONException e) {
            if (callback != null) {
                NetworkCallResult result = new NetworkCallResult();
                result.exception = e;

                Utils.postOnUIThread(() -> callback.onNetworkCallResult(result));
            }
        }
    }

    public static void callAsync(NetworkCallCallback callback, String url, JSONObject body, String... headers) {
        new Thread() {

            @Override
            public void run() {
                NetworkCallResult result = call(url, body, headers);

                if (callback != null) {
                    Utils.postOnUIThread(() -> callback.onNetworkCallResult(result));
                }
            }

        }.start();
    }

    public static void callAsync(NetworkCallCallback callback, String method, String url, JSONObject body, String... headers) {
        new Thread() {

            @Override
            public void run() {
                NetworkCallResult result = call(method, url, body, headers);

                if (callback != null) {
                    Utils.postOnUIThread(() -> callback.onNetworkCallResult(result));
                }
            }

        }.start();
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

    private static JSONObject mapToObject(Map<String, Object> map) throws JSONException {
        JSONObject json = new JSONObject();

        for (Map.Entry<String, Object> entry: map.entrySet()) {
            json.put(entry.getKey(), objectToJsonCompatible(entry.getValue()));
        }

        return json;
    }

    private static Object objectToJsonCompatible(Object object) throws JSONException {
        if (object instanceof JSONObject) {
            return object;
        }
        else if (object instanceof JSONArray) {
            return object;
        }
        else if (object instanceof Map) {
            return mapToObject((Map) object);
        }
        else if (object instanceof Collection) {
            JSONArray jArray = new JSONArray();

            for (Object obj: (Collection) object) {
                jArray.put(objectToJsonCompatible(obj));
            }

            return jArray;
        }
        else if (object == null) {
            return JSONObject.NULL;
        }
        else {
            return object;
        }
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
