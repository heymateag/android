package works.heymate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class APIObject {

    private JSONObject mJSON;

    public APIObject(JSONObject json) {
        mJSON = json;
    }

    public APIObject() {
        mJSON = new JSONObject();
    }

    public void set(String key, Object value) {
        if (key.contains(".")) {
            Object finalValue = value;

            resolve(true, key, null, (object, resolvedKey) -> {
                try {
                    object.mJSON.put(resolvedKey, finalValue);
                } catch (JSONException e) { }
                return null;
            });
            return;
        }

        if (value instanceof APIObject) {
            value = ((APIObject) value).mJSON;
        }

        try {
            mJSON.put(key, value);
        } catch (JSONException e) { }
    }

    public String getString(String key) {
        return resolve(key, null, (object, resolvedKey) -> {
            try {
                return object.mJSON.isNull(resolvedKey) ? null : object.mJSON.getString(resolvedKey);
            } catch (JSONException e) {
                return null;
            }
        });
    }

    public long getLong(String key) {
        return resolve(key, 0L, (object, resolvedKey) -> {
            try {
                return object.mJSON.getLong(resolvedKey);
            } catch (JSONException e) {
                return 0L;
            }
        });
    }

    public int getInt(String key) {
        return Long.valueOf(getLong(key)).intValue();
    }

    public double getDouble(String key) {
        return resolve(key, 0D, (object, resolvedKey) -> {
            try {
                return object.mJSON.getDouble(resolvedKey);
            } catch (JSONException e) {
                return 0D;
            }
        });
    }

    public float getFloat(String key) {
        return Double.valueOf(getDouble(key)).floatValue();
    }

    public boolean getBoolean(String key) {
        return resolve(key, false, (object, resolvedKey) -> {
            try {
                return object.mJSON.getBoolean(resolvedKey);
            } catch (JSONException e) {
                return false;
            }
        });
    }

    public APIObject getObject(String key) {
        return resolve(key, null, (object, resolvedKey) -> {
            try {
                return new APIObject(object.mJSON.getJSONObject(resolvedKey));
            } catch (JSONException e) {
                return null;
            }
        });
    }

    public APIArray getArray(String key) {
        return resolve(key, null, (object, resolvedKey) -> {
            try {
                return new APIArray(object.mJSON.getJSONArray(resolvedKey));
            } catch (JSONException e) {
                return null;
            }
        });
    }

    private<T> T resolve(String key, T defaultValue, TargetCallback<T> callback) {
        return resolve(false, key, defaultValue, callback);
    }

    private<T> T resolve(boolean forcing, String key, T defaultValue, TargetCallback<T> callback) {
        String[] keys = key.split("\\.");

        APIObject object = this;

        for (int i = 0; i < keys.length - 1; i++) {
            APIObject childObject = object.getObject(keys[i]);

            if (childObject == null) {
                if (forcing) {
                    childObject = new APIObject();
                    object.set(keys[i], childObject);
                }
                else {
                    return defaultValue;
                }
            }

            object = childObject;
        }

        return callback.onTargetFound(object, keys[keys.length - 1]);
    }

    public JSONObject asJSON() {
        return mJSON;
    }

    private interface TargetCallback<T> {

        T onTargetFound(APIObject object, String resolvedKey);

    }

}
