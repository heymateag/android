package works.heymate.model;

import static works.heymate.core.Utils.quickMap;
import static works.heymate.core.Utils.quickJSON;

import org.json.JSONObject;

import works.heymate.api.APIObject;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.offer.PurchasePlanInfo;
import works.heymate.core.offer.PurchasePlanTypes;

public class Pricing extends APIObject {

    public static final String RATE_TYPE = "rate_type";
    public static final String CURRENCY = "currency";
    public static final String SUBSCRIPTION = "subscription";
    public static final String BUNDLE = "bundle";
    public static final String PRICE = "price";
    public static final String SIGNATURE = "signature";

    public interface Subscription {

        String SIGNATURE = "signature";
        String PERIOD = "period";
        String PRICE = "subscription_price";

    }

    public interface Bundle {

        String COUNT = "count";
        String DISCOUNT_PERCENT = "discount_percent";
        String SIGNATURE = "signature";

    }

    public Pricing(int price, Currency currency, String rateType, int bundleCount, int bundleDiscountPercent, String subscriptionPeriod, int subscriptionPrice) {
        super(quickJSON(
                RATE_TYPE, rateType,
                CURRENCY, currency.name(),
                SUBSCRIPTION, quickMap(
                        Subscription.SIGNATURE, null,
                        Subscription.PERIOD, subscriptionPeriod,
                        Subscription.PRICE, subscriptionPrice
                ),
                SIGNATURE, null,
                BUNDLE, quickMap(
                        Bundle.COUNT, bundleCount,
                        Bundle.DISCOUNT_PERCENT, bundleDiscountPercent,
                        Bundle.SIGNATURE, null
                ),
                PRICE, price
        ));
    }

    public Pricing(JSONObject json) {
        super(json);
    }

    public long getPrice() {
        return getLong(PRICE);
    }

    public String getCurrency() {
        return getString(CURRENCY);
    }

    public String getRateType() {
        return getString(RATE_TYPE);
    }

    public int getBundleCount() {
        APIObject bundle = getObject(BUNDLE);

        return bundle == null ? 0 : bundle.getInt(Bundle.COUNT);
    }

    public int getBundleDiscountPercent() {
        APIObject bundle = getObject(BUNDLE);

        return bundle == null ? 0 : bundle.getInt(Bundle.DISCOUNT_PERCENT);
    }

    public long getBundleTotalPrice() {
        return getPrice() * getBundleCount() * (100 - getBundleDiscountPercent()) / 100;
    }

    public String getSubscriptionPeriod() {
        APIObject subscription = getObject(SUBSCRIPTION);

        return subscription == null ? null : subscription.getString(Subscription.PERIOD);
    }

    public long getSubscriptionPrice() {
        APIObject subscription = getObject(SUBSCRIPTION);

        return subscription == null ? 0 : subscription.getLong(Subscription.PRICE);
    }

    public PurchasePlanInfo getPurchasePlanInfo(String purchasePlanType) {
        Currency currency = Currency.forName(getCurrency());

        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                return new PurchasePlanInfo(PurchasePlanTypes.SINGLE, Money.create(getLong(PRICE) * 100, currency));
            case PurchasePlanTypes.BUNDLE:
                return getBundleCount() == 0 ? null : new PurchasePlanInfo(PurchasePlanTypes.BUNDLE, Money.create(getBundleTotalPrice() * 100, currency));
            case PurchasePlanTypes.SUBSCRIPTION:
                return getSubscriptionPeriod() == null ? null : new PurchasePlanInfo(PurchasePlanTypes.SUBSCRIPTION, Money.create(getSubscriptionPrice() * 100, currency));
        }

        throw new RuntimeException("Unknown purchase plan type.");
    }

    public void setSignature(String signature) {
        set(SIGNATURE, signature);
    }

    public void setBundleSignature(String signature) {
        APIObject bundle = getObject(BUNDLE);

        if (bundle == null) {
            bundle = new APIObject();
            set(BUNDLE, bundle);
        }

        bundle.set(Bundle.SIGNATURE, signature);
    }

    public void setSubscriptionSignature(String signature) {
        APIObject subscription = getObject(SUBSCRIPTION);

        if (subscription == null) {
            subscription = new APIObject();
            set(SUBSCRIPTION, subscription);
        }

        subscription.set(Subscription.SIGNATURE, signature);
    }

}
