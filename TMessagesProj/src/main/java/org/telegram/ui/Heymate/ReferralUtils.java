package org.telegram.ui.Heymate;

import android.net.Uri;

import com.amplifyframework.api.ApiException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

import works.heymate.core.URLs;
import works.heymate.core.Utils;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.wallet.Wallet;

public class ReferralUtils {

    public static void getReferralId(OfferUtils.PhraseInfo phraseInfo, HtAmplify.APICallback<String> callback) {
        if (phraseInfo == null) {
            Utils.postOnUIThread(() -> {
                callback.onCallResult(false, null, new ApiException("No phrase info", null, null));
            });
            return;
        }

        if (phraseInfo.urlType.equals(URLs.PATH_OFFER)) {
            getReferralLinkFromOfferUrl(phraseInfo, callback);
        }
        else if (phraseInfo.urlType.equals(URLs.PATH_REFERRAL)) {
            getReferralLinkFromReferralUrl(phraseInfo, callback);
        }
        else {
            Utils.postOnUIThread(() -> {
                callback.onCallResult(false, null, new ApiException("Invalid URL", null, null));
            });
        }
    }

    private static void getReferralLinkFromOfferUrl(OfferUtils.PhraseInfo phraseInfo, HtAmplify.APICallback<String> callback) {
        getReferralId(phraseInfo.offerId, null, new ArrayList<>(1), callback);
    }

    private static void getReferralLinkFromReferralUrl(OfferUtils.PhraseInfo phraseInfo, HtAmplify.APICallback<String> callback) {
        HtAmplify.getInstance(ApplicationLoader.applicationContext)
                .getReferralInfo(phraseInfo.referralId, (success, data, exception) -> {
                    if (success) {
                        getReferralId(data.getOfferId(), phraseInfo.referralId, decodeReferrers(data.getReferrers()), callback);
                    }
                    else {
                        callback.onCallResult(false, null, exception);
                    }
                });
    }

    private static void getReferralId(String offerId, String referralId, List<Referrer> referrers, HtAmplify.APICallback<String> callback) {
        if (!referrers.isEmpty()) {
            referrers.get(referrers.size() - 1).referralId = referralId;
        }

        TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();

        String userId = String.valueOf(user.id);
        String walletAddress = Wallet.get(ApplicationLoader.applicationContext, TG2HM.getCurrentPhoneNumber()).getAddress();
        String fcmToken = TG2HM.getFCMToken();

        Referrer me = new Referrer(userId, walletAddress, fcmToken);

        int myIndex = referrers.indexOf(me);

        if (myIndex > 0) {
            me = referrers.get(myIndex);

            String referralLink = Uri.withAppendedPath(Uri.parse(URLs.getBaseURL(URLs.PATH_REFERRAL)), me.referralId).toString();

            callback.onCallResult(true, referralLink, null);
            return;
        }

        referrers.add(me);

        HtAmplify.getInstance(ApplicationLoader.applicationContext)
                .createReferral(offerId, encodeReferrers(referrers), (success, data, exception) -> {
                    if (success) {
                        callback.onCallResult(true, data.getId(), null);
                    }
                    else {
                        callback.onCallResult(false, null, exception);
                    }
                });
    }

    private static List<Referrer> decodeReferrers(String referrersString) {
        try {
            JSONArray jReferrers = new JSONArray(referrersString);

            List<Referrer> referrers = new ArrayList<>(jReferrers.length());

            for (int i = 0; i < jReferrers.length(); i++) {
                referrers.add(new Referrer(jReferrers.getJSONObject(i)));
            }

            return referrers;
        } catch (JSONException e) {
            return null;
        }
    }

    private static String encodeReferrers(List<Referrer> referrers) {
        JSONArray jReferrers = new JSONArray();

        for (Referrer referrer: referrers) {
            jReferrers.put(referrer.asJSON());
        }

        return jReferrers.toString();
    }

    public static class Referrer {

        private static final String USER_ID = "userId";
        private static final String WALLET_ADDRESS = "walletAddress";
        private static final String FCM_TOKEN = "fcmToken";
        private static final String REFERRAL_ID = "referralId";

        public final String userId;
        public final String walletAddress;
        public final String fcmToken;
        public String referralId = null;

        public Referrer(String userId, String walletAddress, String fcmToken) {
            this.userId = userId;
            this.walletAddress = walletAddress;
            this.fcmToken = fcmToken;
        }

        public Referrer(JSONObject json) throws JSONException {
            userId = json.getString(USER_ID);
            walletAddress = json.getString(WALLET_ADDRESS);
            fcmToken = json.getString(FCM_TOKEN);

            if (json.has(REFERRAL_ID) && !json.isNull(REFERRAL_ID)) {
                referralId = json.getString(REFERRAL_ID);
            }
        }

        public JSONObject asJSON() {
            JSONObject json = new JSONObject();

            try {
                json.put(USER_ID, userId);
                json.put(WALLET_ADDRESS, walletAddress);
                json.put(FCM_TOKEN, fcmToken);
                json.put(REFERRAL_ID, referralId);
            } catch (JSONException e) { }

            return json;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Referrer referrer = (Referrer) o;

            return userId.equals(referrer.userId);
        }

        @Override
        public int hashCode() {
            return userId.hashCode();
        }

    }

}
