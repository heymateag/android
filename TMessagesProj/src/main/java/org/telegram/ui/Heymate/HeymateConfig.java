package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

import java.util.HashMap;
import java.util.Map;

public class HeymateConfig {

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