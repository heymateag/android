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

    public static int getPurchasedPlanTimeSlotPrice(Offer offer, PurchasedPlan purchasedPlan) {
        try {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));

            switch (purchasedPlan.getPlanType()) {
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
