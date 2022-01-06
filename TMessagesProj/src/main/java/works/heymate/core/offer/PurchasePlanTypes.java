package works.heymate.core.offer;

import works.heymate.api.APIObject;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.model.Offer;
import works.heymate.model.Pricing;

public class PurchasePlanTypes {

    public static final String SINGLE = "single";
    public static final String BUNDLE = "bundle";
    public static final String SUBSCRIPTION = "subscription";

    public static Money getPurchasedPlanPrice(APIObject offer, String purchasedPlanType) {
        Pricing pricing = new Pricing(offer.getObject(Offer.PRICING).asJSON());
        return pricing.getPurchasePlanInfo(purchasedPlanType).price;
    }

    public static Money getPurchasedPlanTimeSlotPrice(APIObject offer, String purchasedPlanType) {
        Pricing pricing = new Pricing(offer.getObject(Offer.PRICING).asJSON());

        Currency currency = Currency.forName(pricing.getCurrency());

        switch (purchasedPlanType) {
            case SINGLE:
                return Money.create(pricing.getPrice() * 100, currency);
            case BUNDLE:
            case SUBSCRIPTION:
                return Money.create(0, currency);
        }

        throw new IllegalArgumentException("Bad pricing info or unknown purchase plan type.");
    }

}
