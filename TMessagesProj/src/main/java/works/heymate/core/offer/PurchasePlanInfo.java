package works.heymate.core.offer;

import works.heymate.core.Money;

public class PurchasePlanInfo {

    public final String type;
    public final Money price;

    public PurchasePlanInfo(String type, Money price) {
        this.type = type;
        this.price = price;
    }

}
