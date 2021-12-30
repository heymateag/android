package works.heymate.api;

import org.json.JSONException;
import org.json.JSONObject;

public class APIObject {

    private JSONObject mJSON;

    public APIObject(JSONObject json) {
        mJSON = json;
    }

    public String getString(String key) {
        try {
            return mJSON.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public long getLong(String key) {
        try {
            return mJSON.getLong(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    public int getInt(String key) {
        try {
            return mJSON.getInt(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    public double getDouble(String key) {
        try {
            return mJSON.getDouble(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    public float getFloat(String key) {
        return Double.valueOf(getDouble(key)).floatValue();
    }

    public boolean getBoolean(String key) {
        try {
            return mJSON.getBoolean(key);
        } catch (JSONException e) {
            return false;
        }
    }

    public APIObject getObject(String key) {
        try {
            return new APIObject(mJSON.getJSONObject(key));
        } catch (JSONException e) {
            return null;
        }
    }

    public APIArray getArray(String key) {
        try {
            return new APIArray(mJSON.getJSONArray(key));
        } catch (JSONException e) {
            return null;
        }
    }

}
