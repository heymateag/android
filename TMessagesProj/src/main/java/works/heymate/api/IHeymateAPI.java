package works.heymate.api;

import org.json.JSONObject;

import java.util.List;

import works.heymate.core.offer.PricingInfo;

public interface IHeymateAPI {

    // TODO getServiceProviderSchedule

    void updatePushToken(String deviceId, String pushId, APICallback callback);

    void updateUserInfo(String fullName, String username, String avatarHash, String telegramId, APICallback callback);

    void createOffer(String title, String description, String category, String subcategory,
                     long expiration, String address, String latitude, String longitude,
                     String meetingType, int participants, String terms,
                     PricingInfo pricingInfo, JSONObject termsConfig,
                     String walletAddress, String singleSignature,
                     String bundleSignature, String subscriptionSignature,
                     List<Long> timeSlots, APICallback callback);

    void getOffer(String id, APICallback callback);

    void getMyOffers(APICallback callback);

    void getOfferSchedule(String offerId, APICallback callback);

    void getTimeSlot(String id, APICallback callback);

    void updateTimeSlot(String timeSlotId, String status, String meetingId, String sessionPassword, APICallback callback);

    void createPurchasedPlan(String offerId, String planType, APICallback callback);

    void getPurchasedPlan(String planId, APICallback callback);

    void createReservation(String offerId, String serviceProviderId, String timeSlotId, APICallback callback);

    void getMyOrders(APICallback callback);

    void getReservation(String reservationId, APICallback callback);

    void updateReservation(String reservationId, String status, APICallback callback);

}
