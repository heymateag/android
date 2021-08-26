package org.telegram.ui.Heymate;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import works.heymate.core.Currency;
import works.heymate.core.Money;

public class TG2HM {

    private static final List<String> EUROPEAN_UNION_COUNTRIES = Arrays.asList(
            "AT", // Austria
            "BE", // Belgium
            "BG", // Bulgaria
            "HR", // Croatia
            "CY", // Republic of Cyprus
            "CZ", // Czech Republic
            "DK", // Denmark
            "EE", // Estonia
            "FI", // Finland
            "FR", // France
            "DE", // Germany
            "GR", // Greece
            "HU", // Hungary
            "IE", // Ireland
            "IT", // Italy
            "LV", // Latvia
            "LT", // Lithuania
            "LU", // Luxembourg
            "MT", // Malta
            "NL", // Netherlands
            "PL", // Poland
            "PT", // Portugal
            "RO", // Romania
            "SK", // Slovakia
            "SI", // Slovenia
            "ES", // Spain
            "SE" // Sweden
    );

    static Currency defaultCurrency = null;

    public static Currency getDefaultCurrency() {
        if (defaultCurrency == null) {
            defaultCurrency = getCurrencyForPhoneNumber(getCurrentPhoneNumber());
        }

        return defaultCurrency;
    }

    public static Money pickTheRightMoney(Money usd, Money eur) {
        if (getDefaultCurrency().equals(Currency.USD)) {
            return usd;
        }
        else if (getDefaultCurrency().equals(Currency.EUR)) {
            return eur;
        }

        return usd;
    }

    public static String getCurrentPhoneNumber() {
        String phoneNumber = getPhoneNumber(UserConfig.selectedAccount);
        return phoneNumber.startsWith("+") ? phoneNumber : ("+" + phoneNumber);
    }

    public static String getPhoneNumber(int num) {
        return UserConfig.getInstance(num).getCurrentUser().phone;
    }

    public static String getFCMToken() {
        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();

        if (tokenTask.isComplete() && tokenTask.isSuccessful()) {
            return tokenTask.getResult();
        }

        return null;
    }

    public static String getSelfName() {
        return getUserName(String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId));
    }

    public static String getUserName(String sUserId) {
        String name = "Service provider"; // TODO Fix it!

        try {
            int userId = Integer.parseInt(sUserId);

            TLRPC.User user = MessagesStorage.getInstance(UserConfig.selectedAccount).getUser(userId);;

            if (user != null) {
                if (user.username != null) {
                    name = "@" + user.username;
                } else {
                    name = user.first_name;

                    if (!TextUtils.isEmpty(user.last_name)) {
                        name = name + " " + user.last_name;
                    }
                }
            }
        } catch (Throwable t) { }

        return name;
    }

    public static Drawable getThemedDrawable(int resId, int color) {
        Drawable drawable = AppCompatResources.getDrawable(ApplicationLoader.applicationContext, resId);

        if (drawable == null) {
            return null;
        }

        drawable = drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        return drawable;
    }

    private static Currency getCurrencyForPhoneNumber(String phoneNumber) {
        String country = getCountryForPhoneNumber(phoneNumber);

        return EUROPEAN_UNION_COUNTRIES.contains(country) ? Currency.EUR : Currency.USD;
    }

    private static String getCountryForPhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("+")) {
            phoneNumber = phoneNumber.substring(1);
        }

        Map<String, String> codesMap = getCountryCodesMap();

        for (int a = 4; a >= 1; a--) {
            String sub = phoneNumber.substring(0, a);
            String country = codesMap.get(sub);

            if (country != null) {
                return country;
            }
        }

        return null;
    }

    private static Map<String, String> getCountryCodesMap() {
        HashMap<String, String> languageMap = new HashMap<>(236);
        List<String> countriesArray = new ArrayList<>(236);
        Map<String, String> countriesMap = new HashMap<>(236);
        Map<String, String> codesMap = new HashMap<>(236);
        Map<String, String> phoneFormatMap = new HashMap<>(236);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ApplicationLoader.applicationContext.getResources().getAssets().open("countries.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                countriesArray.add(0, args[2]);
                countriesMap.put(args[2], args[0]);
                codesMap.put(args[0], args[1]);
                if (args.length > 3) {
                    phoneFormatMap.put(args[0], args[3]);
                }
                languageMap.put(args[1], args[2]);
            }
            reader.close();
        } catch (Exception e) {
            FileLog.e(e);
        }

        return codesMap;
    }

}
