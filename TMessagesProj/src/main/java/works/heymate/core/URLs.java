package works.heymate.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class URLs {

    public static final String HOST = "heymate.works";

    public static final String PATH_OFFER = "offer";
    public static final String PATH_REFERRAL = "referral";

    public static String getBaseURL(String path) {
        return "https://" + HOST + "/" + path;
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

}
