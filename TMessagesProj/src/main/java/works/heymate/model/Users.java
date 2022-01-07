package works.heymate.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.OnlineReservation;
import org.telegram.ui.Heymate.TG2HM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import works.heymate.api.APICallback;
import works.heymate.api.APIObject;
import works.heymate.api.APIResult;
import works.heymate.api.APIs;
import works.heymate.api.Token;
import works.heymate.core.Utils;

public class Users {

    private static final String KEY_USER = "user";

    public static APIObject currentUser = null;
    private static String processingCurrentUser = null;

    private static final Map<String, APIObject> sUsers = new HashMap<>();

    private static final Map<String, List<APICallback>> sPendingUserRequests = new HashMap<>();

    /**
     *
     * @param callback
     * @return true if a new request to back-end has been called.
     */
    public static boolean onCurrentUserChanged(APICallback callback) {
        String phoneNumber = TG2HM.getCurrentPhoneNumber();

        if (phoneNumber == null) {
            currentUser = null;

            if (callback != null) {
                Utils.postOnUIThread(() -> callback.onAPIResult(new APIResult(new Exception("User not logged in"))));
            }

            return false;
        }

        HeymateConfig config = HeymateConfig.getForAccount(phoneNumber);

        boolean currentUserChanged = currentUser == null || !TextUtils.equals(currentUser.getString(User.ID), config.get(HeymateConfig.KEY_USER_ID));

        String sUser = config.get(KEY_USER);

        if (sUser != null) {
            try {
                currentUser = new APIObject(new JSONObject(sUser));

                sUsers.put(currentUser.getString(currentUser.getString(User.ID)), currentUser);

                if (callback != null) {
                    Utils.postOnUIThread(() -> callback.onAPIResult(new APIResult(currentUser)));
                }
            } catch (JSONException e) { }
        }

        if (phoneNumber.equals(processingCurrentUser)) {
            return false;
        }

        processingCurrentUser = phoneNumber;

        Token.get((token, exception) -> {
            if (token != null) {
                String userId = config.get(HeymateConfig.KEY_USER_ID);

                if (userId == null) {
                    processingCurrentUser = null;
                    return;
                }

                APIs.get().getUserInfo(userId, result -> {
                    processingCurrentUser = null;

                    if (result.success) {
                        currentUser = result.response;

                        sUsers.put(currentUser.getString(currentUser.getString(User.ID)), currentUser);

                        config.set(KEY_USER, currentUser.asJSON().toString());

                        if (currentUserChanged) {
                            OnlineReservation.stabilizeOnlineMeetingStatuses(ApplicationLoader.applicationContext);
                        }
                    }

                    if (sUser == null && callback != null) {
                        callback.onAPIResult(result);
                    }
                });
            }
            else {
                processingCurrentUser = null;
            }
        });

        return true;
    }

    public static void getUser(String userId, APICallback callback) {
        if (sUsers.containsKey(userId)) {
            Utils.postOnUIThread(() -> callback.onAPIResult(new APIResult(sUsers.get(userId))));
            return;
        }

        if (sPendingUserRequests.containsKey(userId)) {
            sPendingUserRequests.get(userId).add(callback);
            return;
        }

        List<APICallback> callbacks = new LinkedList<>();
        callbacks.add(callback);
        sPendingUserRequests.put(userId, callbacks);

        APIs.get().getUserInfo(userId, result -> {
            if (result.response != null) {
                sUsers.put(userId, result.response);
            }

            List<APICallback> pendingCallbacks = new ArrayList<>(sPendingUserRequests.remove(userId));

            for (APICallback pendingCallback: pendingCallbacks) {
                pendingCallback.onAPIResult(result);
            }
        });
    }

}
