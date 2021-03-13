package works.heymate.core;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class Texts {

    public static final String WALLET = "Wallet";

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

        return mContext.getResources().getIdentifier(name, "string", null);
    }

}
