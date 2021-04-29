package org.telegram.ui.Heymate;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.ApiException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.google.firebase.iid.FirebaseInstanceId;

import org.telegram.messenger.AndroidUtilities;

import works.heymate.core.Utils;

import org.telegram.messenger.UserConfig;
import org.telegram.ui.Heymate.AmplifyModels.Offer;
import org.telegram.ui.Heymate.AmplifyModels.Shop;
import org.telegram.ui.Heymate.AmplifyModels.TimeSlot;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class HtAmplify {

    private static HtAmplify instance;
    private Context context;

    public AmazonS3Client amazonS3Client;

    public interface OfferCallback<T> {

        void onOfferQueryResult(boolean success, T data, ApiException exception);

    }

    public interface APICallback<T> {

        void onCallResult(boolean success, T result, ApiException exception);

    }

    public interface TimeSlotsCallback {

        void onTimeSlotsQueryResult(boolean success, List<TimeSlot> timeSlots, ApiException exception);

    }

    public interface OffersCallback {

        void onOffersQueryResult(boolean success, List<Offer> offers, ApiException exception);

    }

    public interface ShopCallback {

        void onShopQueryResult(boolean success, Shop shop, ApiException exception);

    }

    public interface ShopsCallback {

        void onShopsQueryResult(boolean success, ArrayList<Shop> shop, ApiException exception);

    }



    public static HtAmplify getInstance(Context context) {
        if (instance == null)
            instance = new HtAmplify(context.getApplicationContext());
        return instance;
    }

    public Context getContext(){
        return context;
    }

    private HtAmplify(Context context) {
        this.context = context;

        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(context);
            // da2-l73xgtiwbbdkno7gerln5ahpm4
            amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(
                    "AKIATNEPMKIM2UIPWSPC",
                    "y2qEASauUedSjUyLrbDZZ6qTZ4uzIG02y/z/Boco"
            ));

            amazonS3Client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
            amazonS3Client.setEndpoint("https://s3-eu-central-1.amazonaws.com/");

            Log.i("HtAmplify", "Initialized Amplify.");
        } catch (AmplifyException error) {
            Log.e("HtAmplify", "Could not initialize Amplify.", error);
        }
    }

    public enum ShopType {
        Shop,
        MarketPlace
    }

    public void createOffer(OfferDto dto, Uri pickedImage, String address, String signature, APICallback<Offer> callback) {
        Offer newOffer = Offer.builder()
                .userId("" + dto.getUserId())
                .title(dto.getTitle())
                .category(dto.getCategory())
                .subCategory(dto.getSubCategory())
                .rate("" + dto.getRate())
                .rateType(dto.getRateType())
                .currency(dto.getCurrency())
                .description(dto.getDescription())
                .expiry(new Temporal.Date(dto.getExpire()))
                .id(UUID.randomUUID().toString())
                .availabilitySlot(dto.getTimeSlotsAsJson())
                .locationData(dto.getLocation())
                .terms(dto.getTerms())
                .termsConfig(dto.getConfigText())
                .latitude("" + dto.getLatitude())
                .longitude("" + dto.getLongitude())
                .serviceProviderAddress(address)
                .serviceProviderSignature(signature)
                .status(dto.getStatus().ordinal())
                .createdAt(dto.getCreatedAt())
                .editedAt(dto.getEditedAt())
                .build();

        Amplify.API.mutate(ModelMutation.create(newOffer),
                response -> {
                    HtSQLite.getInstance().addOffer(newOffer);
                    if (pickedImage != null) {
                        HtStorage.getInstance().setOfferImage(context, response.getData().getId(), pickedImage);
                    }
                    Log.i("HtAmplify", "Offer Created.");

                    String fcmToken = FirebaseInstanceId.getInstance().getToken();

                    ArrayList<Long> times = dto.getDateSlots();

                    // TODO Request in a for?
                    for (int i = 0; i < times.size(); i += 2) {
                        TimeSlot timeSlot = TimeSlot.builder()
                                .startTime((int) (times.get(i) / 1000))
                                .endTime((int) (times.get(i + 1) / 1000))
                                .offerId(newOffer.getId())
                                .status(HtTimeSlotStatus.AVAILABLE.ordinal())
                                .clientUserId("0")
                                .user1Id(fcmToken)
                                .build();

                        Amplify.API.mutate(ModelMutation.create(timeSlot),
                                response2 -> {
                                    Log.i("HtAmplify", "Time Slot created");
                                },
                                error -> Log.e("HtAmplify", "Time Slot creation failed", error)
                        );
                    }

                    Utils.runOnUIThread(() -> callback.onCallResult(true, newOffer, null));
                },
                error -> {
                    Log.e("HtAmplify", "Create failed", error);
                    Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
                }
        );
    }

    public Shop createShop(int tgId, String title, ShopType shopType) {
        Shop newShop = Shop.builder()
                .tgId(tgId)
                .title(title)
                .type(shopType.ordinal())
                .build();

        Amplify.API.mutate(ModelMutation.create(newShop),
                response -> {
                    Log.i("HtAmplify", "Shop Created.");
                },
                error -> Log.e("HtAmplify", "Create failed", error)
        );

        return newShop;
    }

    public void isShop(int tgId, ShopCallback callback){
        Amplify.API.query(
                ModelQuery.list(Shop.class, Shop.TG_ID.eq(tgId)),
                response -> {
                    if (response.getData() != null) {
                        for (Shop shop : response.getData()) {
                            callback.onShopQueryResult(true, shop, null);
                        }
                        if(!response.getData().hasNextResult()){
                            callback.onShopQueryResult(false, null, null);
                        }
                    }
                },
                error -> {}
        );
    }

    public void getShops(ShopsCallback callback){
        Amplify.API.query(
                ModelQuery.list(Shop.class),
                response -> {
                    ArrayList<Shop> shops = new ArrayList<>();
                    if (response.getData() != null) {
                        for (Shop shop : response.getData()) {
                            shops.add(shop);
                        }
                    }
                    callback.onShopsQueryResult(true, shops, null);
                },
                error -> {
                    callback.onShopsQueryResult(false, null, error);
                }
        );
    }


    public Offer updateOffer(OfferDto dto) {
        Offer newOffer = Offer.builder()
                .userId("" + dto.getUserId())
                .title(dto.getTitle())
                .category(dto.getCategory())
                .subCategory(dto.getSubCategory())
                .rate("" + dto.getRate())
                .rateType(dto.getRateType())
                .currency(dto.getCurrency())
                .description(dto.getDescription())
                .expiry(new Temporal.Date(dto.getExpire()))
                .id(dto.getServerUUID())
                .availabilitySlot(dto.getTimeSlotsAsJson())
                .locationData(dto.getLocation())
                .terms(dto.getTerms())
                .termsConfig(dto.getConfigText())
                .latitude("" + dto.getLatitude())
                .longitude("" + dto.getLongitude())
                .status(dto.getStatus().ordinal())
                .createdAt(dto.getCreatedAt())
                .editedAt((int) ((new Date()).toInstant().getEpochSecond() / 1000))
                .build();

        Amplify.API.query(
                ModelQuery.list(TimeSlot.class, TimeSlot.OFFER_ID.eq(dto.getServerUUID())),
                response -> {
                    if (response.getData() != null) {
                        for (TimeSlot timeSlot : response.getData()) {
                            Amplify.API.mutate(
                                    ModelMutation.delete(timeSlot),
                                    response2 -> {
                                        Log.i("HtAmplify", "TimeSlot updated.");
                                    },
                                    error2 -> Log.e("HtAmplify", "Failed to update", error2)
                            );
                        }
                    }
                },
                error -> Log.e("HtAmplify", "Query failure", error)
        );

        String fcmToken = FirebaseInstanceId.getInstance().getToken();


        ArrayList<Long> times = dto.getDateSlots();
        for (int i = 0; i < times.size(); i += 2) {

            TimeSlot timeSlot = TimeSlot.builder()
                    .startTime((int) (times.get(i) / 1000))
                    .endTime((int) (times.get(i + 1) / 1000))
                    .offerId(newOffer.getId())
                    .status(HtTimeSlotStatus.AVAILABLE.ordinal())
                    .clientUserId("0")
                    .user1Id(fcmToken)
                    .build();

            Amplify.API.mutate(ModelMutation.create(timeSlot),
                    response -> {
                        Log.i("HtAmplify", "Time Slot created");
                    },
                    error -> Log.e("HtAmplify", "Time Slot creation failed", error)
            );
        }

        return newOffer;
    }

    public void getTimeSlot(String timeSlotId, APICallback<TimeSlot> callback) {
        Amplify.API.query(ModelQuery.get(TimeSlot.class, timeSlotId),
                response -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        callback.onCallResult(true, response.getData(), null);
                    });
                },
                error -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        callback.onCallResult(false, null, error);
                    });
                });
    }

    public void bookTimeSlot(String timeSlotId, String clientUserId) {
        Log.i("HtAmplify", "Booking a time slot.");
        String fcmToken = FirebaseInstanceId.getInstance().getToken();

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future future = pool.submit(new Callable() {
            @Override
            public Boolean call() throws Exception {
                Amplify.API.query(
                        ModelQuery.list(TimeSlot.class, TimeSlot.ID.eq(timeSlotId)),
                        response -> {
                            if (response.getData() != null) {
                                for (TimeSlot timeSlot : response.getData()) {
                                    TimeSlot toUpdate = timeSlot.copyOfBuilder()
                                            .clientUserId(clientUserId)
                                            .status(HtTimeSlotStatus.BOOKED.ordinal())
                                            .user2Id(fcmToken)
                                            .build();

                                    Amplify.API.mutate(
                                            ModelMutation.update(toUpdate),
                                            response2 -> {
                                                Log.i("HtAmplify", "TimeSlot updated.");
                                            },
                                            error2 -> Log.e("HtAmplify", "Failed to update", error2)
                                    );
                                }
                            }
                        },
                        error -> Log.e("HtAmplify", "Query failure", error)
                );

                return true;
            }
        });
    }

    public void startTimeSlot(String timeSlotId) {
        Log.i("HtAmplify", "Starting a time slot.");

        updateTimeSlot(timeSlotId, HtTimeSlotStatus.STARTED);
    }

    public void endTimeSlot(String timeSlotId) {
        Log.i("HtAmplify", "Ending a time slot.");

        updateTimeSlot(timeSlotId, HtTimeSlotStatus.FINISHED);
    }

    public void cancelTimeSlot(String timeSlotId) {
        Log.i("HtAmplify", "Cancelling a time slot.");
        updateTimeSlot(timeSlotId, HtTimeSlotStatus.CANCELLED);
    }

    public void getMyOrders(String userId, TimeSlotsCallback callback) {
        Amplify.API.query(ModelQuery.list(TimeSlot.class, TimeSlot.CLIENT_USER_ID.eq(userId)),
                response -> {
                    ArrayList<TimeSlot> timeSlots = new ArrayList<>(10);

                    if (response.hasData()) {
                        for (TimeSlot timeSlot: response.getData()) {
                            timeSlots.add(timeSlot);
                        }
                    }

                    AndroidUtilities.runOnUIThread(() -> {
                        callback.onTimeSlotsQueryResult(true, timeSlots, null);
                    });
                },
                error -> AndroidUtilities.runOnUIThread(() -> {
                    callback.onTimeSlotsQueryResult(false, null, error);
                }));
    }

    public void updateTimeSlot(String timeSlotId, HtTimeSlotStatus status) {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future future = pool.submit(new Callable() {
            @Override
            public Boolean call() throws Exception {
                Amplify.API.query(
                        ModelQuery.list(TimeSlot.class, TimeSlot.ID.eq(timeSlotId)),
                        response -> {
                            if (response.getData() != null) {
                                for (TimeSlot timeSlot : response.getData()) {
                                    TimeSlot toUpdate = timeSlot.copyOfBuilder()
                                            .status(status.ordinal())
                                            .build();

                                    Amplify.API.mutate(
                                            ModelMutation.update(toUpdate),
                                            response2 -> {
                                                Log.i("HtAmplify", "TimeSlot updated.");
                                            },
                                            error2 -> Log.e("HtAmplify", "Failed to update", error2)
                                    );
                                }
                            }
                        },
                        error -> Log.e("HtAmplify", "Query failure", error)
                );

                return true;
            }
        });
    }

    public void getNonAvailableTimeSlots(String offerId, TimeSlotsCallback callback) {
        Amplify.API.query(
                ModelQuery.list(TimeSlot.class, TimeSlot.OFFER_ID.eq(offerId).and(TimeSlot.STATUS.ne(HtTimeSlotStatus.AVAILABLE.ordinal()))),
                response -> {
                    List<TimeSlot> timeSlots = new ArrayList<>();

                    if (response.hasData() && response.getData() != null) {
                        for (TimeSlot timeSlot: response.getData()) {
                            timeSlots.add(timeSlot);
                        }
                    }

                    AndroidUtilities.runOnUIThread(() -> {
                        callback.onTimeSlotsQueryResult(true, timeSlots, null);
                    });
                },
                error -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        callback.onTimeSlotsQueryResult(false, null, error);
                    });
                });
    }

    public ArrayList<TimeSlot> getAvailableTimeSlots(String offerId, OfferCallback callback) {
        Log.i("HtAmplify", "Getting time slots");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<ArrayList<TimeSlot>> future = pool.submit(new Callable<ArrayList<TimeSlot>>() {
            @Override
            public ArrayList<TimeSlot> call() throws Exception {
                ArrayList<TimeSlot> timeSlots = new ArrayList();

                Amplify.API.query(
                        ModelQuery.list(
                                TimeSlot.class, TimeSlot.OFFER_ID.eq("" + offerId)
                                        .and(TimeSlot.STATUS.eq(HtTimeSlotStatus.AVAILABLE.ordinal()))),
                        response -> {
                            if (response.hasData()) {
                                callback.onOfferQueryResult(true, response.getData(), null);
                            } else {
                                callback.onOfferQueryResult(false, null, null);
                            }
                        },
                        error -> callback.onOfferQueryResult(false, null, error)
                );
                return timeSlots;
            }
        });

        try {
            return future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<TimeSlot> getAvailableTimeSlots(String offerId) {
        Log.i("HtAmplify", "Getting time slots");

        AtomicBoolean completed = new AtomicBoolean(false);

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<ArrayList<TimeSlot>> future = pool.submit(new Callable<ArrayList<TimeSlot>>() {
            @Override
            public ArrayList<TimeSlot> call() throws Exception {
                ArrayList<TimeSlot> timeSlots = new ArrayList();

                Amplify.API.query(
                        ModelQuery.list(
                                TimeSlot.class, TimeSlot.OFFER_ID.eq("" + offerId)
                                        .and(TimeSlot.STATUS.eq(HtTimeSlotStatus.AVAILABLE.ordinal()))),
                        response -> {
                            if (response.getData() != null) {
                                for (TimeSlot timeSlot : response.getData()) {
                                    timeSlots.add(timeSlot);
                                }
                            }
                            completed.set(true);
                        },
                        error -> {
                            Log.e("HtAmplify", "Query failure", error);
                            completed.set(true);
                        }
                );
                return timeSlots;
            }
        });

        try {
            while (!completed.get()) ; // The worst approach ever, Will fix ASAP
            return future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void getOffer(String offerId, OfferCallback<Offer> callback) {
        Amplify.API.query(
                ModelQuery.get(Offer.class, offerId),
                response -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (response.hasData()) {
                            callback.onOfferQueryResult(true, response.getData(), null);
                        } else {
                            callback.onOfferQueryResult(false, null, null);
                        }
                    });
                },
                error -> AndroidUtilities.runOnUIThread(() -> callback.onOfferQueryResult(false, null, error))
        );
    }

    public void getOffersWithoutImages(OffersCallback callback) {
        int currentAccount = UserConfig.selectedAccount;
        int userId = UserConfig.getInstance(currentAccount).clientUserId;

        Amplify.API.query(
                ModelQuery.list(Offer.class, Offer.USER_ID.eq("" + userId)),
                response -> {
                    ArrayList<Offer> offers = new ArrayList<>();

                    if (response.getData() != null) {

                        for (Offer offer : response.getData()) {
                            offers.add(offer);
                        }

                        HtSQLite.getInstance().updateOffers(offers, UserConfig.getInstance(currentAccount).clientUserId);
                    }

                    callback.onOffersQueryResult(true, offers, null);
                },
                error -> callback.onOffersQueryResult(false, null, error)
        );
    }

    public ArrayList<Offer> getOffers(int userId, int currentAccount) {
        Log.i("HtAmplify", "Getting Offers");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<ArrayList<Offer>> future = pool.submit(new Callable<ArrayList<Offer>>() {
            @Override
            public ArrayList<Offer> call() throws Exception {
                ArrayList<Offer> offers = new ArrayList();
                Amplify.API.query(
                        ModelQuery.list(Offer.class, Offer.USER_ID.eq("" + userId)),
                        response -> {
                            if (response.getData() != null) {
                                for (Offer offer : response.getData()) {
                                    offers.add(offer);
                                }
                                HtSQLite.getInstance().updateOffers(offers, UserConfig.getInstance(currentAccount).clientUserId);

                                new Thread(){
                                    @Override
                                    public void run() {
                                        HashMap<String, S3Object> images = new HashMap<>();
                                        for(Offer offer : offers){
                                            if(HtStorage.getInstance().imageExists(context, offer.getId()))
                                                continue;
                                            S3Object image = getOfferImage(offer.getId());
                                            if(image != null){
                                                images.put(offer.getId(), image);
                                            }
                                        }
                                        HtStorage.getInstance().updateOfferImages(context, images);
                                    }
                                }.start();
                            }
                        },
                        error -> Log.e("HtAmplify", "Query failure", error)
                );
                return offers;
            }
        });

        try {
            return future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Offer getOffer(String offerUUID) {
        Log.i("HtAmplify", "Getting Offer");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<Offer> future = pool.submit(new Callable<Offer>() {
            @Override
            public Offer call() throws Exception {
                final Offer[] fetchedOffer = new Offer[1];
                Amplify.API.query(
                        ModelQuery.list(Offer.class, Offer.ID.eq(offerUUID)),
                        response -> {
                            if (response.getData() != null) {
                                for (Offer offer : response.getData()) {
                                    fetchedOffer[0] = offer;
                                    HtSQLite.getInstance().addOffer(offer);
                                }
                            }
                        },
                        error -> Log.e("HtAmplify", "Query failure", error)
                );
                return fetchedOffer[0];
            }
        });

        try {
            return future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveOfferImage(String offerUUID, File file) {
        amazonS3Client.putObject("offerdocuments", offerUUID, file);
    }

    public S3Object getOfferImage(String offerUUID) {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<S3Object> future = pool.submit(new Callable<S3Object>() {
            @Override
            public S3Object call() throws Exception {
                if(amazonS3Client.doesObjectExist("offerdocuments", offerUUID)) {
                    return amazonS3Client.getObject("offerdocuments", offerUUID);
                } else {
                    return null;
                }
            }
        });

        try {
            return future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
