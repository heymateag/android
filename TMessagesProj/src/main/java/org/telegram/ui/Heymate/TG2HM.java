package org.telegram.ui.Heymate;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.telegram.messenger.UserConfig;

public class TG2HM {

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

}
