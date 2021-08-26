package works.heymate.core.offer;

import com.amplifyframework.datastore.generated.model.Offer;

import org.json.JSONException;
import org.json.JSONObject;

import works.heymate.core.Money;

public class PurchasePlanTypes {

    public static final String SINGLE = "single";
    public static final String BUNDLE = "bundle";
    public static final String SUBSCRIPTION = "subscription";

    public static Money getPurchasedPlanPrice(Offer offer, String purchasedPlanType) {
        try {
            PricingInfo pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));
            return pricingInfo.getPurchasePlanInfo(purchasedPlanType).price;
        } catch (JSONException e) { }

        throw new IllegalArgumentException("Bad pricing info or unknown purchase plan type.");
    }

    public static Money getPurchasedPlanTimeSlotPrice(Offer offer, String purchasedPlanType) {
        try {
            PricingInfo pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));

            switch (purchasedPlanType) {
                case SINGLE:
                    return Money.create(pricingInfo.price * 100, pricingInfo.currency);
                case BUNDLE:
                case SUBSCRIPTION:
                    return Money.create(0, pricingInfo.currency);
            }
        } catch (JSONException e) { }

        throw new IllegalArgumentException("Bad pricing info or unknown purchase plan type.");
    }

}
