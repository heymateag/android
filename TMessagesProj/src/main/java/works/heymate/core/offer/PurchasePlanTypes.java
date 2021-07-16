package works.heymate.core.offer;

import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.PurchasedPlan;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;

public class PurchasePlanTypes {

    public static final String SINGLE = "single";
    public static final String BUNDLE = "bundle";
    public static final String SUBSCRIPTION = "subscription";

    public static int getPurchasedPlanPrice(Offer offer, String purchasedPlanType) {
        try {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));
            return pricingInfo.getPurchasePlanInfo(purchasedPlanType).price;
        } catch (JSONException e) { }

        throw new IllegalArgumentException("Bad pricing info or unknown purchase plan type.");
    }

    public static int getPurchasedPlanTimeSlotPrice(Offer offer, String purchasedPlanType) {
        try {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));

            switch (purchasedPlanType) {
                case SINGLE:
                    return pricingInfo.price;
                case BUNDLE:
                case SUBSCRIPTION:
                    return 0;
            }
        } catch (JSONException e) { }

        throw new IllegalArgumentException("Bad pricing info or unknown purchase plan type.");
    }

}
