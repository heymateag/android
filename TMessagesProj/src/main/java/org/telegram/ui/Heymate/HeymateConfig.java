package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

import java.util.HashMap;
import java.util.Map;

public class HeymateConfig {

    public static final boolean MAIN_NET = false;
    public static final boolean PRODUCTION = false;
    public static final boolean DEBUG = true;
    public static final boolean DEMO = false;

    public static final int INTERNAL_VERSION = 105;

    public static final String API_BASE_URL_PRODUCTION = "https://api.heymate.works";
    public static final String API_BASE_URL_STAGING = "https://lar9nm8ay9.execute-api.us-east-1.amazonaws.com/dev";
    public static final String API_BASE_URL = PRODUCTION ? API_BASE_URL_PRODUCTION : API_BASE_URL_STAGING;

    public static final String KEY_USER_ID = "user_id";

    private static final String PREFERENCES_PREFIX = "HeymateConfig_";

    private static final Map<String, HeymateConfig> sMap = new HashMap<>();

    public static HeymateConfig getForAccount(String phoneNumber) {
        HeymateConfig config = sMap.get(phoneNumber);

        if (config == null) {
            config = new HeymateConfig(PREFERENCES_PREFIX + (phoneNumber != null ? phoneNumber : ""));
            sMap.put(phoneNumber, config);
        }

        return config;
    }

    public static HeymateConfig getGeneral() {
        return getForAccount(null);
    }

    private SharedPreferences mPreferences;

    private HeymateConfig(String name) {
        mPreferences = ApplicationLoader.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void set(String key, String value) {
        mPreferences.edit().putString(key, value).apply();
    }

    public String get(String key) {
        return mPreferences.getString(key, null);
    }

}
