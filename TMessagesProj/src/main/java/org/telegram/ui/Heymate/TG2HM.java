package org.telegram.ui.Heymate;

import org.telegram.messenger.UserConfig;

public class TG2HM {

    public static String getCurrentPhoneNumber() {
        String phoneNumber = getPhoneNumber(UserConfig.selectedAccount);
        return phoneNumber.startsWith("+") ? phoneNumber : ("+" + phoneNumber);
    }

    public static String getPhoneNumber(int num) {
        return UserConfig.getInstance(num).getCurrentUser().phone;
    }

}
