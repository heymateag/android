package works.heymate.api;

import org.json.JSONArray;
import org.json.JSONException;

public class APIArray {

    private JSONArray mJSON;

    public APIArray(JSONArray json) {
        mJSON = json;
    }

    public int size() {
        return mJSON.length();
    }

    public String getString(int i) {
        try {
            return mJSON.getString(i);
        } catch (JSONException e) {
            return null;
        }
    }

    public long getLong(int i) {
        try {
            return mJSON.getLong(i);
        } catch (JSONException e) {
            return 0;
        }
    }

    public int getInt(int i) {
        return Long.valueOf(getLong(i)).intValue();
    }

    public double getDouble(int i) {
        try {
            return mJSON.getDouble(i);
        } catch (JSONException e) {
            return 0;
        }
    }

    public float getFloat(int i) {
        return Double.valueOf(getDouble(i)).floatValue();
    }

    public boolean getBoolean(int i) {
        try {
            return mJSON.getBoolean(i);
        } catch (JSONException e) {
            return false;
        }
    }

    public APIObject getObject(int i) {
        try {
            return new APIObject(mJSON.getJSONObject(i));
        } catch (JSONException e) {
            return null;
        }
    }

    public APIArray getArray(int i) {
        try {
            return new APIArray(mJSON.getJSONArray(i));
        } catch (JSONException e) {
            return null;
        }
    }

}
