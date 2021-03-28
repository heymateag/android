package works.heymate.core;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {

    private static Handler mHandler = null;

    public static void putValues(JSONObject json, String... keyValues) {
        try {
            for (int i = 0; i < keyValues.length; i += 2) {
                json.put(keyValues[i], keyValues[i + 1]);
            }
        } catch (JSONException e) { }
    }

    public static JSONObject getParameters(String rawURL) {
        try {
            URL url = new URL(rawURL);
            String query = url.getQuery();

            JSONObject parameters = new JSONObject();

            if (query == null) {
                return parameters;
            }

            String[] pairs = query.split("&");

            for (String pair: pairs) {
                String[] keyValue = pair.split("=");

                if (keyValue.length == 2) {
                    try {
                        parameters.put(keyValue[0], keyValue[1]);
                    } catch (JSONException e) { }
                }
            }

            return parameters;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String getOrNull(JSONObject json, String name) {
        if (json.has(name) && !json.isNull(name)) {
            try {
                return json.getString(name);
            } catch (JSONException e) {
                return null;
            }
        }

        return null;
    }

    public static void runOnUIThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }

        ensureHandler();

        mHandler.post(runnable);
    }

    public static void postOnUIThread(Runnable runnable) {
        ensureHandler();

        mHandler.post(runnable);
    }

    synchronized private static void ensureHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
    }

}
