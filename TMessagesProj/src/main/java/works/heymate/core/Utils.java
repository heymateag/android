package works.heymate.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import works.heymate.api.APIObject;

public class Utils {

    private static Handler mHandler = null;

    public static void putValues(JSONObject json, String... keyValues) {
        try {
            for (int i = 0; i < keyValues.length; i += 2) {
                json.put(keyValues[i], keyValues[i + 1]);
            }
        } catch (JSONException e) { }
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

    public static JSONObject quickJSON(Object... keyValues) {
        return mapToJSON(quickMap(keyValues));
    }

    public static Map<String, Object> quickMap(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i].toString(), keyValues[i + 1]);
        }

        return map;
    }

    public static JSONObject mapToJSON(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        JSONObject json = new JSONObject();

        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                json.put(entry.getKey(), objectToJsonCompatible(entry.getValue()));
            }
        } catch (JSONException e) { }

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
            return mapToJSON((Map) object);
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

    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
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
