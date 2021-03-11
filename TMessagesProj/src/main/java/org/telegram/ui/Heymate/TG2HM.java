package org.telegram.ui.Heymate;

import org.telegram.messenger.UserConfig;

public class TG2HM {

    public static String getCurrentPhoneNumber() {
        return getPhoneNumber(UserConfig.selectedAccount);
    }

    public static String getPhoneNumber(int num) {
        return UserConfig.getInstance(num).getCurrentUser().phone;
    }

}
