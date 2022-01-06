package works.heymate.api;

import org.telegram.ui.Heymate.HeymateConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import works.heymate.model.Pricing;
import works.heymate.util.SimpleNetworkCall;

import static works.heymate.core.Utils.quickJSON;
import static works.heymate.core.Utils.quickMap;

class HeymateAPIImpl implements IHeymateAPI {

    private static final String GET_USER_INFO_URL = HeymateConfig.API_BASE_URL + "/users/getUserById";
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
    private static final String GET_TIMESLOT_RESERVATIONS_URL = HeymateConfig.API_BASE_URL + "/offer/{timeslotId}/offerParticipant";
    private static final String GET_RESERVATION_URL = HeymateConfig.API_BASE_URL + "/reservation/";
    private static final String UPDATE_RESERVATION_URL = HeymateConfig.API_BASE_URL + "/reservation/";

    @Override
    public void getUserInfo(String userId, APICallback callback) {
        String url = GET_USER_INFO_URL + (userId != null ? "/" + userId : "");

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
        }, callback, "PATCH", UPDATE_USER_INFO_URL, "fullName", fullName, "userName", username, "avatarHash", avatarHash, "telegramId", telegramId);
    }

    @Override
    public void createOffer(String title, String description, String category, String subcategory, long expiration, String address, String latitude, String longitude, String meetingType, int participants, String terms, Pricing pricing, APIObject paymentTerms, String walletAddress, List<Long> timeSlots, APICallback callback) {
        Map<String, Object> body;

        body = quickMap(
                "location", quickMap("lat", latitude, "long", longitude, "address", address),
                "participants", participants,
                "meeting_type", meetingType,
                "payment_terms", paymentTerms.asJSON(),
                "schedules", createSchedules(timeSlots),
                "referral_plan", "REF",
                "category", quickMap(
                        "main_cat", category,
                        "sub_cat", subcategory
                ),
                "simple_share", "simple or referral ? skip for now",
                "term_condition", terms,
                "description", description,
                "pricing", pricing.asJSON(),
                "expiration", expiration / 1000,
                "sp_wallet_address", walletAddress,
                "title", title,
                "media", Arrays.asList()
        );

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
                            "form_time", String.valueOf(timeSlots.get(i) / 1000),
                            "to_time", String.valueOf(timeSlots.get(i + 1) / 1000)
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
        }, callback, "PUT", url, "status", status, "meetingId", meetingId, "sessionPassword", sessionPassword);
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
    public void createReservation(String offerId, String serviceProviderId, String timeSlotId, String tradeId, APICallback callback) {
        authorizedCall(result -> {
            if (result.responseCode == 201) {
                callback.onAPIResult(new APIResult(new APIObject(result.response).getObject("data")));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "POST", CREATE_RESERVATION_URL, "offerId", offerId, "serviceProviderId", serviceProviderId, "timeSlotId", timeSlotId, "tradeId", tradeId);
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
    public void getTimeSlotReservations(String timeSlotId, APICallback callback) {
        String url = GET_TIMESLOT_RESERVATIONS_URL.replace("{timeslotId}", timeSlotId);

        authorizedCall(result -> {
            if (result.responseCode == 200) {
                callback.onAPIResult(new APIResult(new APIObject(result.response)));
            }
            else if (result.responseCode == 404) { // TODO This should actually return an error
                callback.onAPIResult(new APIResult(new APIObject(quickJSON("data", new LinkedList<>()))));
            }
            else {
                callback.onAPIResult(new APIResult(result.exception));
            }
        }, callback, "GET", url);
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
        }, callback, "PUT", url, "status", status);
    }

    private void authorizedCall(SimpleNetworkCall.NetworkCallCallback networkCallback, APICallback callback, String method, String url, Object... body) {
        authorizedCallWithPreparedBody(networkCallback, callback, method, url, "GET".equals(method) ? null : quickMap(body));
    }

    private void authorizedCallWithPreparedBody(SimpleNetworkCall.NetworkCallCallback networkCallback, APICallback callback, String method, String url, Map<String, Object> body) {
        Token.get((token, exception) -> {
            if (token != null) {
                SimpleNetworkCall.callMapAsync(networkCallback, method, url, body, "Authorization", "Bearer " + token);
            }
            else if (callback != null) {
                callback.onAPIResult(new APIResult(exception));
            }
        });
    }

}
