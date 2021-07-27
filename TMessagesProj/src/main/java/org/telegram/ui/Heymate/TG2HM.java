package org.telegram.ui.Heymate;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

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

}
