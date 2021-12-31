package works.heymate.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.HeymateConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import works.heymate.core.Utils;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.offer.PricingInfo;
import works.heymate.util.SimpleNetworkCall;

class HeymateAPIImpl implements IHeymateAPI {

    private static final String UPDATE_PUSH_TOKEN_URL = HeymateConfig.API_BASE_URL + "/users/putPushToken";
    private static final String UPDATE_USER_INFO_URL = HeymateConfig.API_BASE_URL + "/users/updateUserInfo";
    private static final String CREATE_OFFER_URL = HeymateConfig.API_BASE_URL + "/offer";
    private static final String GET_OFFER_URL = HeymateConfig.API_BASE_URL + "/offer/";
    private static final String GET_MY_OFFERS_URL = HeymateConfig.API_BASE_URL + "/offer/me";
    private static final String GET_OFFER_SCHEDULE_URL = HeymateConfig.API_BASE_URL + "/time-table/{offerId}/schedule";
    private static final String GET_TIMESLOT_URL = HeymateConfig.API_BASE_URL + "/time-table/";
    private static final String UPDATE_TIMESLOT_URL = HeymateConfig.API_BASE_URL + "/time-table/";
    private static final String CREATE_PURCHASED_PLAN_URL = HeymateConfig.API_BASE_URL + "/purchase-plan";
    private static final String GET_PURCHASED_PLAN_URL = HeymateConfig.API_BASE_URL + "/purchase-plan/";
    private static final String CREATE_RESERVATION_URL = HeymateConfig.API_BASE_URL + "/reservation";
    private static final String GET_MY_ORDERS_URL = HeymateConfig.API_BASE_URL + "/reservation/myOrders";
    private static final String GET_RESERVATION_URL = HeymateConfig.API_BASE_URL + "/reservation/";
    private static final String UPDATE_RESERVATION_URL = HeymateConfig.API_BASE_URL + "/reservation/";

    @Override
    public void updatePushToken(String deviceId, String pushId, APICallback callback) {
        authorizedCall(result -> {
            if (callback != null) {
                callback.onAPIResult(new APIResult(result.responseCode == 201, result.exception));
            }
        }, callback , "POST", UPDATE_PUSH_TOKEN_URL, "deviceId", deviceId, "pushId", pushId);
    }

    @Override
    public void updateUserInfo(String fullName, String username, String avatarHash, String telegramId, APICallback callback) {
        authorizedCall(result -> {
            if (callback != null) {
                callback.onAPIResult(new APIResult(result.responseCode >= 200 && result.responseCode < 300, result.exception));
            }
        }, callback, "POST", UPDATE_USER_INFO_URL, "fullName", fullName, "userName", username, "avatarHash", avatarHash, "telegramId", telegramId);
    }

    @Override
    public void createOffer(String title, String description, String category, String subcategory, long expiration, String address, String latitude, String longitude, String meetingType, int participants, String terms, PricingInfo pricingInfo, JSONObject termsConfig, String walletAddress, String singleSignature, String bundleSignature, String subscriptionSignature, List<Long> timeSlots, APICallback callback) {
        Map<String, Object> body;

        try {
            body = quickMap(
                    "location", quickMap("lat", latitude, "long", longitude, "address", address),
                    "participants", participants,
                    "meeting_type", meetingType,
                    "payment_terms", quickMap(
                            "deposit", termsConfig.get(OfferUtils.INITIAL_DEPOSIT),
                            "delay_in_start", quickMap(
                                    "duration", termsConfig.get(OfferUtils.DELAY_TIME),
                                    "deposit", termsConfig.get(OfferUtils.DELAY_PERCENT)
                            ),
                            "cancellation", Arrays.asList(
                                    quickMap(
                                            "range", termsConfig.get(OfferUtils.CANCEL_HOURS1),
                                            "penalty", termsConfig.get(OfferUtils.CANCEL_PERCENT1)
                                    ),
                                    quickMap(
                                            "range", termsConfig.get(OfferUtils.CANCEL_HOURS2),
                                            "penalty", termsConfig.get(OfferUtils.CANCEL_PERCENT2)
                                    )
                            )
                    ),
                    "schedules", createSchedules(timeSlots),
                    "referral_plan", "REF",
                    "category", quickMap(
                            "main_cat", category,
                            "sub_cat", subcategory
                    ),
                    "simple_share", "simple or referral ? skip for now",
                    "term_condition", terms,
                    "description", description,
                    "pricing", quickMap(
                            "rate_type", pricingInfo.rateType,
                            "currency", pricingInfo.currency.name(),
                            "subscription", quickMap(
                                    "signature", subscriptionSignature,
                                    "period", pricingInfo.subscriptionPeriod,
                                    "subscription_price", pricingInfo.subscriptionPrice
                            ),
                            "signature", singleSignature,
                            "bundle", quickMap(
                                    "count", pricingInfo.bundleCount,
                                    "discount_percent", pricingInfo.bundleDiscountPercent,
                                    "signature", bundleSignature
                            ),
                            "price", pricingInfo.price
                    ),
                    "expiration", expiration / 1000,
                    "sp_wallet_address", walletAddress,
                    "title", title,
                    "media", Arrays.asList()
            );
        } catch (JSONException e) {
            Utils.postOnUIThread(() -> callback.onAPIResult(new APIResult(e)));
            return;
        }

        authorizedCallWithPreparedBody(result -> {
            if (result.responseCode == 201) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "POST", CREATE_OFFER_URL, body);
    }

    private List<Object> createSchedules(List<Long> timeSlots) {
        List<Object> schedules = new ArrayList<>(timeSlots.size() / 2);

        for (int i = 0; i < timeSlots.size(); i += 2) {
            schedules.add(
                    quickMap(
                            "form_time", timeSlots.get(i),
                            "to_time", timeSlots.get(i + 1)
                    )
            );
        }

        return schedules;
    }

    @Override
    public void getOffer(String id, APICallback callback) {
        String url = GET_OFFER_URL + id;

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", url);
    }

    @Override
    public void getMyOffers(APICallback callback) {
        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response)));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", GET_MY_OFFERS_URL);
    }

    @Override
    public void getOfferSchedule(String offerId, APICallback callback) {
        String url = GET_OFFER_SCHEDULE_URL.replace("{offerId}", offerId);

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response)));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", url);
    }

    @Override
    public void getTimeSlot(String id, APICallback callback) {
        String url = GET_TIMESLOT_URL + id;

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", url);
    }

    @Override
    public void updateTimeSlot(String timeSlotId, String status, String meetingId, String sessionPassword, APICallback callback) {
        String url = UPDATE_TIMESLOT_URL + timeSlotId;

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "PUT", url, quickMap("status", status, "meetingId", meetingId, "sessionPassword", sessionPassword));
    }

    @Override
    public void createPurchasedPlan(String offerId, String planType, APICallback callback) {
        authorizedCall(result -> {
            if (result.responseCode == 201) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "POST", CREATE_PURCHASED_PLAN_URL, "planType", planType, "offerId", offerId);
    }

    @Override
    public void getPurchasedPlan(String planId, APICallback callback) {
        String url = GET_PURCHASED_PLAN_URL + planId;

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", url);
    }

    @Override
    public void createReservation(String offerId, String serviceProviderId, String timeSlotId, APICallback callback) {
        authorizedCall(result -> {
            if (result.responseCode == 201) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "POST", CREATE_RESERVATION_URL, "offerId", offerId, "serviceProviderId", serviceProviderId, "timeSlotId", timeSlotId);
    }

    @Override
    public void getMyOrders(APICallback callback) {
        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response)));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", GET_MY_ORDERS_URL);
    }

    @Override
    public void getReservation(String reservationId, APICallback callback) {
        String url = GET_RESERVATION_URL + reservationId;

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", url);
    }

    @Override
    public void updateReservation(String reservationId, String status, APICallback callback) {
        String url = UPDATE_RESERVATION_URL + reservationId;

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "PUT", url, quickMap("status", status));
    }

    private void authorizedCall(SimpleNetworkCall.NetworkCallCallback networkCallback, APICallback callback, String method, String url, Object... body) {
        authorizedCallWithPreparedBody(networkCallback, callback, method, url, quickMap(body));
    }

    private void authorizedCallWithPreparedBody(SimpleNetworkCall.NetworkCallCallback networkCallback, APICallback callback, String method, String url, Map<String, Object> body) {
        Token.get((token, exception) -> {
            if (token != null) {
                SimpleNetworkCall.callMapAsync(networkCallback, method, url, body, "Authorization", "Bearer " + token);
            }
            else {
                callback.onAPIResult(new APIResult(exception));
            }
        });
    }

    private static Map<String, Object> quickMap(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i].toString(), keyValues[i + 1]);
        }

        return map;
    }

}
