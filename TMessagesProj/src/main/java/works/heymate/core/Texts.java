package works.heymate.core;

import android.content.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Texts {

    public static final String HEYMATE = "heymate";
    public static final String LOGO_URL = "logo_url";
    public static final String OFFER_PHRASE_NAME = "offer_phrase_name";

    public static final String ADD = "add";
    public static final String SECURE = "secure";
    public static final String LATER = "later";
    public static final String NEXT = "next";
    public static final String CONFIRM = "confirm";
    public static final String CANCEL = "cancel";
    public static final String DAY = "day";
    public static final String SEARCH_ADDRESS = "search_address";
    public static final String CREATE_WALLET = "create_wallet";
    public static final String TODAY = "today";
    public static final String YESTERDAY = "yesterday";
    public static final String DETAILS = "details";
    public static final String START = "start";
    public static final String FINISH = "finish";
    public static final String ONLINE_MEETING = "online_meeting";
    public static final String START_SESSION = "start_session";
    public static final String JOIN_SESSION = "join_session";

    public static final String NETWORK_BLOCKCHAIN_ERROR = "network_blockchain_error";
    public static final String NETWORK_ERROR = "network_error";
    public static final String UNKNOWN_ERROR = "unknown_error";
    public static final String INSUFFICIENT_BALANCE = "insufficient_balance";

    public static final String SUNDAY_SHORT = "sunday_short";
    public static final String MONDAY_SHORT = "monday_short";
    public static final String TUESDAY_SHORT = "tuesday_short";
    public static final String WEDNESDAY_SHORT = "wednesday_short";
    public static final String THURSDAY_SHORT = "thursday_short";
    public static final String FRIDAY_SHORT = "friday_short";
    public static final String SATURDAY_SHORT = "saturday_short";

    public static final String TIME_DIFF_DAY = "time_diff_day";
    public static final String TIME_DIFF_HOUR = "time_diff_hour";
    public static final String TIME_DIFF_MINUTE = "time_diff_minute";

    public static final String OFFERS = "offers";
    public static final String OFFERS_ACTIVE_OFFERS = "offers_active_offers";

    public static final String CREATE_OFFER_HEYMATE_TERMS = "create_offer_heymate_terms";
    public static final String CREATE_OFFER_SERVICE_PROVIDER_TERMS = "create_offer_service_provider_terms";

    public static final String PARTICIPANTS_INPUT_TITLE = "participants_input_title";
    public static final String PARTICIPANTS_INPUT_DESCRIPTION = "participants_input_descrpition";
    public static final String PARTICIPANTS_INPUT_USERS = "participants_input_users";
    public static final String PARTICIPANTS_INPUT_UNLIMITED = "participants_input_unlimited";
    public static final String PARTICIPANTS_INPUT_EMPTY = "participants_input_empty";

    public static final String MY_SCHEDULE = "my_schedule";
    public static final String MY_SCHEDULE_OFFERS = "my_schedule_offers";
    public static final String MY_SCHEDULE_ORDERS = "my_schedule_orders";
    public static final String MY_SCHEDULE_ACCEPTED = "my_schedule_accepted";
    public static final String MY_SCHEDULE_CANCELLED = "my_schedule_cancelled";
    public static final String MY_SCHEDULE_MARKED_STARTED = "my_schedule_marked_started";
    public static final String MY_SCHEDULE_STARTED = "my_schedule_started";
    public static final String MY_SCHEDULE_MARKED_FINISHED = "my_schedule_marked_finished";
    public static final String MY_SCHEDULE_FINISHED = "my_schedule_finished";

    public static final String AUTHENTICATION = "authentication";
    public static final String AUTHENTICATION_DESCRIPTION = "authentication_description";

    public static final String CREATE_OFFER_NO_WALLET = "create_offer_no_wallet";
    public static final String CREATE_OFFER_NO_WALLET_DESCRIPTION = "create_offer_no_wallet_description";
    public static final String CREATE_OFFER_SAVED = "create_offer_saved";

    public static final String YOUR_WALLET = "your_wallet";
    public static final String NO_WALLET_DETECTED = "no_wallet_detected";
    public static final String NO_WALLET_DETECTED_MESSAGE = "no_wallet_detected_message";
    public static final String CREATE_NEW_WALLET = "create_new_wallet";
    public static final String IMPORT_EXISTING_WALLET = "import_existing_wallet";
    public static final String WALLET_DETECTED = "wallet_detected";
    public static final String WALLET_DETECTED_MESSAGE = "wallet_detected_message";

    public static final String SECURE_BIOMETRIC = "secure_biometric";
    public static final String SECURE_BIOMETRIC_DESCRIPTION = "secure_biometric_description";
    public static final String SECURE_PIN = "secure_pin";
    public static final String SECURE_PIN_DESCRIPTION = "secure_pin_description";

    public static final String ATTESTATION_CHECK_MESSAGES = "attestation_check_messages";
    public static final String ATTESTATION_CHECK_MESSAGES_DESCRIPTION = "attestation_check_message_description";
    public static final String ATTESTATION_REQUESTING = "attestation_requesting";
    public static final String ATTESTATION_REQUESTING_MESSAGE = "attestation_requesting_message";
    public static final String ATTESTATION_BAD_CODE = "attestation_bad_code";
    public static final String ATTESTATION_INVALID_CODE = "attestation_invalid_code";
    public static final String ATTESTATION_USED_CODE = "attestation_used_code";

    public static final String TIMESLOTSSELECTED_TITLE = "timeslotselection_title";

    public static final String CREATE_SHOP_TITLE = "create_shop_title";
    public static final String CREATE_SHOP_SHOP_TYPE = "create_shop_shop_type";
    public static final String CREATE_SHOP_MARKETPLACE = "create_shop_marketplace";
    public static final String CREATE_SHOP_MARKETPLACE_DESCRIPTION = "create_shop_marketplace_description";
    public static final String CREATE_SHOP_SHOP = "create_shop_shop";
    public static final String CREATE_SHOP_SHOP_DESCRIPTION = "create_shop_shop_description";

    public static final String NEW_MARKETPLACE_TITLE = "new_marketplace_title";
    public static final String NEW_MARKETPLACE_NAME_HINT = "new_marketplace_name_hint";

    public static final String NEW_SHOP_NO_LINK = "new_shop_no_link";
    public static final String NEW_SHOP_TITLE = "new_shop_title";
    public static final String NEW_SHOP_NAME_HINT = "new_shop_name_hint";
    public static final String NEW_SHOP_DESCRIPTION_GUIDE = "new_shop_description_guide";
    public static final String NEW_SHOP_SETTINGS = "new_shop_settings";
    public static final String NEW_SHOP_TYPE = "new_shop_type";
    public static final String NEW_SHOP_PUBLIC_SHOP = "new_shop_public_shop";
    public static final String NEW_SHOP_PUBLIC_SHOP_DESCRIPTION = "new_shop_public_shop_description";
    public static final String NEW_SHOP_PRIVATE_SHOP = "new_shop_private_shop";
    public static final String NEW_SHOP_PRIVATE_SHOP_DESCRIPTION = "new_shop_private_shop_description";
    public static final String NEW_SHOP_PRIVATE_LINK_GUIDE = "new_shop_private_link_guide";
    public static final String NEW_SHOP_PUBLIC_LINK_GUIDE = "new_shop_public_link_guide";

    private static final String STRING_RESOURCE_PREFIX = "hm_";

    private static Context mContext;

    private static Map<String, CharSequence> mTexts = new HashMap<>();

    public static void initialize(Context context) {
        mContext = context.getApplicationContext();
    }

    public static String getLanguageCode() {
        return Locale.getDefault().getLanguage(); // TODO Revisit this.
    }

    public static CharSequence get(String key, String languageCode) { // TODO Attempt to get it considering the language code.
        return get(key);
    }

    public static CharSequence get(String key) {
        CharSequence text = mTexts.get(key);

        if (text == null) {
            int resourceId = getResourceId(key);

            if (resourceId > 0) {
                text = mContext.getText(resourceId);

                mTexts.put(key, text);
            }
            else {
                text = '{' + key + '}';
            }
        }

        return text;
    }

    private static int getResourceId(String key) {
        String name = STRING_RESOURCE_PREFIX + key;

        return mContext.getResources().getIdentifier(name, "string", mContext.getPackageName());
    }

}
