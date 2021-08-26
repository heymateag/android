package works.heymate.core.offer;

import org.json.JSONException;
import org.json.JSONObject;

import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Utils;

public class PricingInfo {

    private static final String PRICE = "price";
    private static final String CURRENCY = "currency";
    private static final String RATE_TYPE = "rate_type";
    private static final String BUNDLE_COUNT = "bundle_count";
    private static final String BUNDLE_DISCOUNT_PERCENT = "bundle_discount_percent";
    private static final String SUBSCRIPTION_PERIOD = "subscription_period";
    private static final String SUBSCRIPTION_PRICE = "subscription_price";

    public final int price;
    public final Currency currency;
    public final String rateType;
    public final int bundleCount;
    public final int bundleDiscountPercent;
    public final String subscriptionPeriod;
    public final int subscriptionPrice;

    public PricingInfo(int price, Currency currency, String rateType, int bundleCount, int bundleDiscountPercent, String subscriptionPeriod, int subscriptionPrice) {
        this.price = price;
        this.currency = currency;
        this.rateType = rateType;
        this.bundleCount = bundleCount;
        this.bundleDiscountPercent = bundleDiscountPercent;
        this.subscriptionPeriod = subscriptionPeriod;
        this.subscriptionPrice = subscriptionPrice;
    }

    public PricingInfo(JSONObject json) throws JSONException {
        price = json.getInt(PRICE);
        currency = Currency.forName(json.getString(CURRENCY));
        rateType = json.getString(RATE_TYPE);

        int bundleCountTemp = 0;
        int bundleDiscountPercentTemp = 0;
        try {
            bundleCountTemp = json.getInt(BUNDLE_COUNT);
            bundleDiscountPercentTemp = json.getInt(BUNDLE_DISCOUNT_PERCENT);
        } catch (JSONException e) {
        }
        bundleCount = bundleCountTemp;
        bundleDiscountPercent = bundleDiscountPercentTemp;

        String subscriptionPeriodTemp = null;
        int subscriptionPriceTemp = 0;
        try {
            subscriptionPeriodTemp = Utils.getOrNull(json, SUBSCRIPTION_PERIOD);
            subscriptionPriceTemp = json.getInt(SUBSCRIPTION_PRICE);
        } catch (JSONException e) {
        }
        subscriptionPeriod = subscriptionPeriodTemp;
        subscriptionPrice = subscriptionPriceTemp;
    }

    public int getBundleTotalPrice() {
        return price * bundleCount * (100 - bundleDiscountPercent) / 100;
    }

    public PurchasePlanInfo getPurchasePlanInfo(String purchasePlanType) {
        switch (purchasePlanType) {
            case PurchasePlanTypes.SINGLE:
                return new PurchasePlanInfo(PurchasePlanTypes.SINGLE, Money.create(price * 100, currency));
            case PurchasePlanTypes.BUNDLE:
                return bundleCount == 0 ? null : new PurchasePlanInfo(PurchasePlanTypes.BUNDLE, Money.create(getBundleTotalPrice() * 100, currency));
            case PurchasePlanTypes.SUBSCRIPTION:
                return subscriptionPeriod == null ? null : new PurchasePlanInfo(PurchasePlanTypes.SUBSCRIPTION, Money.create(subscriptionPrice * 100, currency));
        }

        throw new RuntimeException("Unknown purchase plan type.");
    }

    public JSONObject asJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put(PRICE, price);
            json.put(CURRENCY, currency);
            json.put(RATE_TYPE, rateType);
            json.put(BUNDLE_COUNT, bundleCount);
            json.put(BUNDLE_DISCOUNT_PERCENT, bundleDiscountPercent);
            json.put(SUBSCRIPTION_PERIOD, subscriptionPeriod);
            json.put(SUBSCRIPTION_PRICE, subscriptionPrice);
        } catch (JSONException e) {
        }

        return json;
    }

}
