package works.heymate.util;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultObjectBuilder implements Template.ObjectBuilder {

    private JSONObject mJSON = new JSONObject();

    @Override
    public Object ensure(Object from, String name) {
        JSONObject jFrom;

        if (from == null) {
            jFrom = mJSON;
        }
        else {
            jFrom = (JSONObject) from;
        }

        JSONObject child;

        if (jFrom.has(name)) {
            try {
                child = jFrom.getJSONObject(name);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            child = new JSONObject();

            try {
                jFrom.put(name, child);
            } catch (JSONException e) { }
        }

        return child;
    }

    @Override
    public void set(Object from, String name, String value) {
        JSONObject jFrom;

        if (from == null) {
            jFrom = mJSON;
        }
        else {
            jFrom = (JSONObject) from;
        }

        try {
            jFrom.put(name, value);
        } catch (JSONException e) { }
    }

    public JSONObject getJSON() {
        return mJSON;
    }

}
