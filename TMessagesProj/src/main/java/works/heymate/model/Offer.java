package works.heymate.model;

public interface Offer {

    interface Location {

        String LATITUDE = "lat";
        String LONGITUDE = "long";
        String ADDRESS = "address";

    }

    interface PaymentTerms {

        interface DelayInStart {
            String DURATION = "duration";
            String PENALTY = "deposit";
        }

        interface Cancellation {
            String RANGE = "range";
            String PENALTY = "penalty";
        }

        String DEPOSIT = "deposit";
        String DELEY_IN_START = "delay_in_start";
        String CANCELLATION = "cancellation";

    }

    interface Category {

        String MAIN_CATEGORY = "main_cat";
        String SUB_CATEGORY = "sub_cat";

    }

    interface Pricing {

        interface Subscription {

            String SIGNATURE = "signature";
            String PERIOD = "period";
            String PRICE = "subscription_price";

        }

        interface Bundle {

            String COUNT = "count";
            String DISCOUNT_PERCENT = "discount_percent";
            String SIGNATURE = "signature";

        }

        String RATE_TYPE = "rate_type";
        String CURRENCY = "currency";
        String SUBSCRIPTION = "subscription";
        String SIGNATURE = "signature";
        String BUNDLE = "bundle";
        String PRICE = "price";

    }

    interface Media {

        String KEY = "key";
        String TYPE = "type";

    }

    String ID = "id";
    String LOCATION = "location";
    String PARTICIPANTS = "participants";
    String MEETING_TYPE = "meeting_type";
    String CREATED_AT = "createdAt";
    String PAYMENT_TERMS = "payment_terms";
    String TIMESLOTS = "schedules";
    String CATEGORY = "category";
    String TERMS_AND_CONDITIONS = "term_condition";
    String DESCRIPTION = "description";
    String PRICING = "pricing";
    String EXPIRATION = "expiration";
    String WALLET_ADDRESS = "sp_wallet_address";
    String TITLE = "title";
    String MEDIA = "media";
    String USER_ID = "userId";
    String UPDATED_AT = "updatedAt";

}
