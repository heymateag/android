package works.heymate.api;

import org.json.JSONObject;

import java.util.List;

import works.heymate.core.offer.PricingInfo;

public class HeymateAPIImpl implements IHeymateAPI {

    public void login(String phoneNumber, String password, APICallback callback) {

    }

    @Override
    public void updatePushToken(String deviceId, String pushId, APICallback callback) {

    }

    @Override
    public void updateUserInfo(String fullName, String username, String avatarHash, String telegramId, APICallback callback) {

    }

    @Override
    public void createOffer(String title, String description, String category, String subcategory, long expiration, String address, String latitude, String longitude, String meetingType, int participants, String terms, PricingInfo pricingInfo, JSONObject termsConfig, String walletAddress, String singleSignature, String bundleSignature, String subscriptionSignature, List<Long> timeSlots, APICallback callback) {

    }

    @Override
    public void getOffer(String id, APICallback callback) {

    }

    @Override
    public void deleteOffer(String id, APICallback callback) {

    }

    @Override
    public void getMyOffers(APICallback callback) {

    }

    @Override
    public void getOfferSchedule(String offerId, APICallback callback) {

    }

    @Override
    public void getTimeSlot(String id, APICallback callback) {

    }

    @Override
    public void updateTimeSlot(String timeSlotId, String status, String meetingId, String sessionPassword, APICallback callback) {

    }

    @Override
    public void createPurchasedPlan(String offerId, String planType, APICallback callback) {

    }

    @Override
    public void getPurchasedPlan(String planId, APICallback callback) {

    }

    @Override
    public void createReservation(String offerId, String serviceProviderId, String timeSlotId, APICallback callback) {

    }

    @Override
    public void getMyOrders(APICallback callback) {

    }

    @Override
    public void getReservation(String reservationId, APICallback callback) {

    }

    @Override
    public void updateReservation(String reservationId, String status, APICallback callback) {

    }
}
