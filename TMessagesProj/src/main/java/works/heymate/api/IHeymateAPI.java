package works.heymate.api;

import java.io.File;
import java.util.List;

import works.heymate.model.Pricing;

public interface IHeymateAPI {

    // TODO getServiceProviderSchedule

    void getUserInfo(String userId, APICallback callback);

    void getUserByTelegramId(String telegramId, APICallback callback);

    void updateUserInfo(String fullName, String username, String avatarHash, String telegramId, APICallback callback);

    void updateUserDevices(String walletAddress, String currency, String deviceName, String deviceId, String pushToken, APICallback callback);

    void uploadFile(File file, APICallback callback);

    void downloadFile(String fileName, File destination, APICallback callback);

    void createOffer(String title, String description, String category, String subcategory,
                     long expiration, String address, String latitude, String longitude,
                     String offerType, String meetingType, String meetingLink,
                     int participants, String terms,
                     Pricing pricing, APIObject paymentTerms,
                     String walletAddress, List<Long> timeSlots, List<String> images,
                     APICallback callback);

    void getOffer(String id, APICallback callback);

    void getMyOffers(APICallback callback);

    void getOfferSchedule(String offerId, APICallback callback);

    void getTimeSlot(String id, APICallback callback);

    void updateTimeSlot(String timeSlotId, String status, String meetingId, String sessionPassword, APICallback callback);

    void createPurchasedPlan(String offerId, String planType, APICallback callback);

    void getPurchasedPlan(String planId, APICallback callback);

    void createReservation(String offerId, String serviceProviderId, String timeSlotId, String tradeId, APICallback callback);

    void getMyOrders(APICallback callback);

    void getTimeSlotReservations(String timeSlotId, APICallback callback);

    void getReservation(String reservationId, APICallback callback);

    void updateReservation(String reservationId, String status, APICallback callback);

}
