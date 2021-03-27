package works.heymate.core;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class Texts {

    public static final String SECURE = "secure";
    public static final String LATER = "later";
    public static final String NEXT = "next";
    public static final String CONFIRM = "confirm";
    public static final String DAY = "day";
    public static final String NETWORK_ERROR = "network_error";

    public static final String SUNDAY_SHORT = "sunday_short";
    public static final String MONDAY_SHORT = "monday_short";
    public static final String TUESDAY_SHORT = "tuesday_short";
    public static final String WEDNESDAY_SHORT = "wednesday_short";
    public static final String THURSDAY_SHORT = "thursday_short";
    public static final String FRIDAY_SHORT = "friday_short";
    public static final String SATURDAY_SHORT = "saturday_short";

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

    private static final String STRING_RESOURCE_PREFIX = "hm_";

    private static Context mContext;

    private static Map<String, CharSequence> mTexts = new HashMap<>();

    public static void initialize(Context context) {
        mContext = context.getApplicationContext();
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
