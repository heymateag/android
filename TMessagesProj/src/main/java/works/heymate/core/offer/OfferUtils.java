package works.heymate.core.offer;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

public class OfferUtils {

    private static final String TITLE = "t";
    private static final String PAYMENT_TYPE = "p_t";
    private static final String PRICE = "p";
    private static final String PRICE_CURRENCY = "p_c";
    private static final String ADDRESS = "a";
    private static final String LATITUDE = "la";
    private static final String LONGITUDE = "lo";

    /*
    Heymate Offer

    {title}
    {description}

    {name/@username} will provide {sub category} for {price} in {currency} {payment_type}
     */
    public static String serializeBeautiful(Offer offer) {
        return null;
    }

    public static String serializeOfferEssentials(Offer offer) {
        JSONObject json = new JSONObject();

        try {
            json.put(TITLE, offer.getTitle());
            json.put(PAYMENT_TYPE, offer.getRateType());
            json.put(PRICE, offer.getRate());
            json.put(PRICE_CURRENCY, offer.getCurrency());
            json.put(ADDRESS, offer.getLocationData());
            json.put(LATITUDE, offer.getLatitude());
            json.put(LONGITUDE, offer.getLongitude());
        } catch (JSONException e) { }

        return json.toString();
    }

}
