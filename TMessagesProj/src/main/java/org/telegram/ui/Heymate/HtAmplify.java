package org.telegram.ui.Heymate;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.mobileconnectors.lambdainvoker.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.ApiException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.predicate.QueryPredicate;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.PurchasedPlan;
import com.amplifyframework.datastore.generated.model.Referral;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.amplifyframework.datastore.generated.model.Shop;
import com.amplifyframework.datastore.generated.model.TimeSlot;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.iid.FirebaseInstanceId;

import org.telegram.messenger.AndroidUtilities;

import works.heymate.core.HeymateEvents;
import works.heymate.core.Utils;

import org.telegram.messenger.UserConfig;

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

public class HtAmplify {
    
    private static final String TAG = "HtAmplify";

    private static HtAmplify instance;

    public interface OfferCallback<T> {

        void onOfferQueryResult(boolean success, T data, ApiException exception);

    }

    public interface APICallback<T> {

        void onCallResult(boolean success, T result, ApiException exception);

    }

    public interface ShopCallback {

        void onShopQueryResult(boolean success, Shop shop, ApiException exception);

    }

    public interface ShopsCallback {

        void onShopsQueryResult(boolean success, ArrayList<Shop> shop, ApiException exception);

    }

    public static class GetJWTRequest {

        long iat;
        long exp;
        long tokenExp;

        public GetJWTRequest() {

        }

        public GetJWTRequest(long startTime) {
            iat = startTime;
            exp = 48L * 60L * 60L;
            tokenExp = exp;
        }

        public long getIat() {
            return iat;
        }

        public void setIat(long iat) {
            this.iat = iat;
        }

        public long getExp() {
            return exp;
        }

        public void setExp(long exp) {
            this.exp = exp;
        }

        public long getTokenExp() {
            return tokenExp;
        }

        public void setTokenExp(long tokenExp) {
            this.tokenExp = tokenExp;
        }

    }

    public static class GetJWTResponse {

        String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

    }

    public interface LambdaFunctions {

        @LambdaFunction(functionName = "getZoomJWT-staging")
        GetJWTResponse getZoomJWT(GetJWTRequest request);

    }

    public static HtAmplify getInstance(Context context) {
        if (instance == null)
            instance = new HtAmplify(context.getApplicationContext());
        return instance;
    }

    private Context context;

    public AmazonS3Client amazonS3Client;
    private LambdaFunctions mFunctions;

    public Context getContext(){
        return context;
    }

    private HtAmplify(Context context) {
        this.context = context;

        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(context);

            AWSCredentials credentials = new BasicAWSCredentials(
                    "AKIATNEPMKIM2UIPWSPC",
                    "y2qEASauUedSjUyLrbDZZ6qTZ4uzIG02y/z/Boco"
            );

            amazonS3Client = new AmazonS3Client(credentials);

            StaticCredentialsProvider credentialsProvider = new StaticCredentialsProvider(credentials);

            amazonS3Client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
            amazonS3Client.setEndpoint("https://s3-eu-central-1.amazonaws.com/");

//            AWSLambda lambda = new AWSLambdaClient(credentialsProvider);
//            lambda.setRegion(Region.getRegion(Regions.US_EAST_1));
//            lambda.setEndpoint("getZoomJWT-staging");
//            lambda.invoke(new InvokeRequest());


            // Create an instance of CognitoCachingCredentialsProvider
//            CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
//                    context, "us-east-1:883d9973-a1d4-4bc0-aa34-55ad8a2cc6c3", Regions.US_EAST_1);

            // Create LambdaInvokerFactory, to be used to instantiate the Lambda proxy.
            LambdaInvokerFactory factory = new LambdaInvokerFactory(context, Regions.US_EAST_1, credentialsProvider);

            // Create the Lambda proxy object with a default Json data binder.
            // You can provide your own data binder by implementing
            // LambdaDataBinder.
            mFunctions = factory.build(LambdaFunctions.class);

            Log.i(TAG, "Initialized Amplify.");
        } catch (AmplifyException error) {
            Log.e(TAG, "Could not initialize Amplify.", error);
        }
    }

    public enum ShopType {
        Shop,
        MarketPlace
    }

    public void createOffer(OfferDto dto, Uri pickedImage, String address, String priceSignature, String bundleSignature, String subscriptionSignature, APICallback<Offer> callback) {
        int maximumReservations = dto.getMaximumReservations();

        Offer newOffer = Offer.builder()
                .userId("" + dto.getUserId())
                .title(dto.getTitle())
                .category(dto.getCategory())
                .subCategory(dto.getSubCategory())
                .pricingInfo(dto.getPricingInfo() == null ? null : dto.getPricingInfo().asJSON().toString())
                .description(dto.getDescription())
                .expiry(new Temporal.Date(dto.getExpire()))
                .id(UUID.randomUUID().toString())
                .availabilitySlot(dto.getTimeSlotsAsJson())
                .locationData(dto.getLocation())
                .meetingType(dto.getMeetingType())
                .maximumReservations(maximumReservations)
                .terms(dto.getTerms())
                .termsConfig(dto.getConfigText())
                .latitude("" + dto.getLatitude())
                .longitude("" + dto.getLongitude())
                .walletAddress(address)
                .priceSignature(priceSignature)
                .bundleSignature(bundleSignature)
                .subscriptionSignature(subscriptionSignature)
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
                    Log.i(TAG, "Offer Created.");

                    String fcmToken = FirebaseInstanceId.getInstance().getToken();

                    ArrayList<Long> times = dto.getDateSlots();

                    String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

                    // TODO Request in a for?
                    for (int i = 0; i < times.size(); i += 2) {
                        TimeSlot timeSlot = TimeSlot.builder()
                                .startTime((int) (times.get(i) / 1000))
                                .endTime((int) (times.get(i + 1) / 1000))
                                .offerId(newOffer.getId())
                                .userId(userId)
                                .userFcmToken(fcmToken)
                                .maximumReservations(maximumReservations)
                                .completedReservations(0)
                                .remainingReservations(maximumReservations)
                                .meetingType(newOffer.getMeetingType())
                                .build();

                        Amplify.API.mutate(ModelMutation.create(timeSlot),
                                response2 -> {
                                    Log.i(TAG, "Time Slot created");
                                },
                                error -> Log.e(TAG, "Time Slot creation failed", error)
                        );
                    }

                    Utils.runOnUIThread(() -> callback.onCallResult(true, newOffer, null));
                },
                error -> {
                    Log.e(TAG, "Create failed", error);
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
                    Log.i(TAG, "Shop Created.");
                },
                error -> Log.e(TAG, "Create failed", error)
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

    public void createPurchasedPlan(PurchasedPlan plan, APICallback<PurchasedPlan> callback) {
        Amplify.API.mutate(ModelMutation.create(plan), result -> {
                if (callback != null) {
                    Utils.runOnUIThread(() -> callback.onCallResult(true, result.getData(), null));
                }
            },
                error -> {
                    Log.e(TAG, "Failed to create purchased plan", error);

                    if (callback != null) {
                        callback.onCallResult(false, null, error);
                    }
                });
    }

    public void updatePurchasedPlan(PurchasedPlan plan, APICallback<PurchasedPlan> callback) {
        Amplify.API.mutate(ModelMutation.update(plan), result -> {
                if (callback != null) {
                    Utils.runOnUIThread(() -> callback.onCallResult(true, result.getData(), null));
                }
            },
            error -> {
                Log.e(TAG, "Failed to update purchased plan", error);

                if (callback != null) {
                    callback.onCallResult(false, null, error);
                }
            });
    }

    public void createOrUpdatePurchasedPlan(PurchasedPlan plan, APICallback<PurchasedPlan> callback) {
        Amplify.API.query(ModelQuery.get(PurchasedPlan.class, plan.getId()), result -> {
            if (result.hasData()) {
                updatePurchasedPlan(plan, callback);
            }
            else {
                createPurchasedPlan(plan, callback);
            }
        }, error -> {
            Log.e(TAG, "Failed to query for purchased plan");
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public Reservation createReservation(TimeSlot timeSlot, PurchasedPlan purchasedPlan, Referral referral) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);
        String fcmToken = FirebaseInstanceId.getInstance().getToken();

        return Reservation.builder()
                .consumerId(userId)
                .consumerFcmToken(fcmToken)
                .offerId(timeSlot.getOfferId())
                .purchasedPlanId(purchasedPlan.getId())
                .purchasedPlanType(purchasedPlan.getPlanType())
                .timeSlotId(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .referralId(referral == null ? null : referral.getId())
                .referrers(referral == null ? null : referral.getReferrers())
                .serviceProviderId(timeSlot.getUserId())
                .serviceProviderFcmToken(timeSlot.getUserFcmToken())
                .status(HtTimeSlotStatus.BOOKED.name())
                .meetingType(timeSlot.getMeetingType())
                .build();
    }

    public void bookTimeSlot(Reservation reservation, TimeSlot timeSlot, APICallback<Reservation> callback) {
        Log.i(TAG, "Booking a time slot.");

        Amplify.API.mutate(ModelMutation.create(reservation), result -> {
            if (result.hasData()) {
                HeymateEvents.notify(HeymateEvents.RESERVATION_STATUS_UPDATED, reservation.getId());

                Utils.runOnUIThread(() -> callback.onCallResult(true, result.getData(), null));

                TimeSlot newTimeSlot = timeSlot.copyOfBuilder()
                        .completedReservations(timeSlot.getCompletedReservations() + 1)
                        .remainingReservations(timeSlot.getRemainingReservations() - 1)
                        .build();

                Amplify.API.mutate(ModelMutation.update(newTimeSlot), result2 -> {
                    if (!result2.hasData()) {
                        Log.e(TAG, "Failed to update time slot. Has errors: " + result.hasErrors());
                    }
                }, error -> Log.e(TAG, "Failed to update time slot.", error));
            }
            else {
                Log.e(TAG, "Failed to create reserved time slot. Has errors: " + result.hasErrors());
                Utils.runOnUIThread(() -> callback.onCallResult(false, null, null));
            }
        }, error -> {
            Log.e(TAG, "Failed to create reserved time slot", error);
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public void getTimeSlots(String offerId, APICallback<ArrayList<TimeSlot>> callback) {
        Log.i(TAG, "Getting time slots");

        Amplify.API.query(
                ModelQuery.list(TimeSlot.class, TimeSlot.OFFER_ID.eq(offerId)), result -> {
                    if (result.hasData()) {
                        ArrayList<TimeSlot> timeSlots = new ArrayList<>();

                        for (TimeSlot timeSlot: result.getData().getItems()) {
                            timeSlots.add(timeSlot);
                        }

                        Utils.runOnUIThread(() -> callback.onCallResult(true, timeSlots, null));
                    }
                    else {
                        Log.e(TAG, "Failed to get time slots. Has errors: " + result.hasErrors());
                        Utils.runOnUIThread(() -> callback.onCallResult(false, null, null));
                    }
                }, error -> {
                    Log.e(TAG, "Failed to get time slots.", error);
                    Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
                });
    }

    public void getMyReservedTimeSlots(APICallback<List<TimeSlot>> callback) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

        Amplify.API.query(ModelQuery.list(TimeSlot.class, TimeSlot.USER_ID.eq(userId).and(TimeSlot.COMPLETED_RESERVATIONS.ge(1))), result -> {
            List<TimeSlot> timeSlots = new ArrayList<>();

            if (result.hasData()) {
                for (TimeSlot timeSlot: result.getData()) {
                    timeSlots.add(timeSlot);
                }
            }

            Utils.runOnUIThread(() -> callback.onCallResult(true, timeSlots, null));
        }, error -> {
            Log.e(TAG, "Failed to get reserved time slots", error);
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public void getPurchasedPlans(APICallback<List<PurchasedPlan>> callback) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

        Amplify.API.query(ModelQuery.list(PurchasedPlan.class, PurchasedPlan.CONSUMER_ID.eq(userId)), result -> {
            List<PurchasedPlan> purchasedPlans = new ArrayList<>();

            if (result.hasData()) {
                for (PurchasedPlan purchasedPlan: result.getData()) {
                    purchasedPlans.add(purchasedPlan);
                }
            }

            Utils.runOnUIThread(() -> callback.onCallResult(true, purchasedPlans, null));
        }, error -> {
            Log.e(TAG, "Failed to get purchased plans", error);
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public void getPurchasedPlan(String purchasedPlanId, APICallback<PurchasedPlan> callback) {
        Amplify.API.query(ModelQuery.get(PurchasedPlan.class, purchasedPlanId), result ->
            Utils.runOnUIThread(() -> callback.onCallResult(true, result.getData(), null))
        , error -> {
            Log.e(TAG, "Failed to get purchased plan", error);
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public void getMyOrders(APICallback<List<Reservation>> callback) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);
        getReservations(Reservation.CONSUMER_ID.eq(userId), callback);
    }

    public void getMyAcceptedOffers(APICallback<List<Reservation>> callback) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);
        getReservations(Reservation.SERVICE_PROVIDER_ID.eq(userId), callback);
    }

    private void getReservations(QueryPredicate predicate, APICallback<List<Reservation>> callback) {
        Amplify.API.query(ModelQuery.list(Reservation.class, predicate),
                response -> {
                    if (response.hasData()) {
                        ArrayList<Reservation> reservations = new ArrayList<>(10);

                        for (Reservation reservation : response.getData()) {
                            reservations.add(reservation);
                        }

                        AndroidUtilities.runOnUIThread(() -> callback.onCallResult(true, reservations, null));
                    }
                    else {
                        Log.e(TAG, "Failed to get reservations. Has errors: " + response.hasErrors());
                        AndroidUtilities.runOnUIThread(() -> callback.onCallResult(false, null, null));
                    }
                },
                error -> {
                    Log.e(TAG, "Failed to get reservations.", error);
                    AndroidUtilities.runOnUIThread(() -> callback.onCallResult(false, null, error));
                });
    }

    public void getReservation(String id, APICallback<Reservation> callback) {
        Amplify.API.query(ModelQuery.get(Reservation.class, id), result -> {
            Utils.runOnUIThread(() -> callback.onCallResult(true, result.getData(), null));
        }, error -> {
            Log.e(TAG, "Failed to query reservation.", error);
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public void getTimeSlotReservations(String id, APICallback<List<Reservation>> callback) {
        Amplify.API.query(ModelQuery.list(Reservation.class, Reservation.TIME_SLOT_ID.eq(id)), result -> {
            List<Reservation> reservations = new ArrayList<>();

            if (result.hasData()) {
                for (Reservation reservation: result.getData()) {
                    reservations.add(reservation);
                }
            }

            Utils.runOnUIThread(() -> callback.onCallResult(true, reservations, null));
        }, error -> {
            Log.e(TAG, "Failed to query reservations.", error);
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public void updateReservation(Reservation reservation, HtTimeSlotStatus status) {
        Reservation mutatedReservation = reservation.copyOfBuilder()
                .status(status.name())
                .build();

        updateReservation(mutatedReservation);
    }

    public void updateReservation(Reservation reservation, HtTimeSlotStatus status, String meetingId) {
        Reservation mutatedReservation = reservation.copyOfBuilder()
                .status(status.name())
                .meetingId(meetingId)
                .build();

        updateReservation(mutatedReservation);
    }

    private void updateReservation(Reservation reservation) {
        Amplify.API.mutate(ModelMutation.update(reservation), result -> {
            HeymateEvents.notify(HeymateEvents.RESERVATION_STATUS_UPDATED, reservation.getId());

            if (result.hasData()) {
                Log.i(TAG, "Reservation updated.");

                if (HtTimeSlotStatus.CANCELLED_BY_CONSUMER.name().equals(reservation.getStatus()) ||
                        HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER.name().equals(reservation.getStatus())) {
                    Log.i(TAG, "Updating time slot.");
                    getTimeSlot(reservation.getTimeSlotId(), (success, result1, exception) -> {
                        if (success && result1 != null) {
                            TimeSlot mutatedTimeSlot = result1.copyOfBuilder()
                                    .completedReservations(result1.getCompletedReservations() - 1)
                                    .remainingReservations(result1.getRemainingReservations() + 1)
                                    .build();

                            Amplify.API.mutate(ModelMutation.update(mutatedTimeSlot), result2 -> {
                                Log.i(TAG, "Time slot updated successfully.");
                            }, error -> {
                                Log.e(TAG, "Failed to update time slot.", error);
                            });
                        }
                    });

                    if (reservation.getPurchasedPlanId() != null) {
                        Amplify.API.query(ModelQuery.get(PurchasedPlan.class, reservation.getPurchasedPlanId()), result1 -> {
                            if (result1.hasData()) {
                                PurchasedPlan purchasedPlan = result1.getData();
                                PurchasedPlan modifiedPlan = purchasedPlan.copyOfBuilder()
                                        .pendingReservationsCount(purchasedPlan.getPendingReservationsCount() - 1)
                                        .build();

                                Amplify.API.mutate(ModelMutation.update(modifiedPlan),
                                        result2 -> Log.i(TAG, "Purchased plan updated."),
                                        error -> Log.e(TAG, "Failed to update purchased plan.", error)
                                        );
                            }
                            else {
                                Log.e(TAG, "Purchased plan with id " + reservation.getPurchasedPlanId() + " not found!");
                            }
                        },
                        error -> Log.e(TAG, "Failed to get purchased plan", error));
                    }
                }
                else if (HtTimeSlotStatus.FINISHED.name().equals(reservation.getStatus()) && reservation.getPurchasedPlanId() != null) {
                    Amplify.API.query(ModelQuery.get(PurchasedPlan.class, reservation.getPurchasedPlanId()), result1 -> {
                                if (result1.hasData()) {
                                    PurchasedPlan purchasedPlan = result1.getData();
                                    PurchasedPlan modifiedPlan = purchasedPlan.copyOfBuilder()
                                            .finishedReservationsCount(purchasedPlan.getFinishedReservationsCount() + 1)
                                            .pendingReservationsCount(purchasedPlan.getPendingReservationsCount() - 1)
                                            .build();

                                    Amplify.API.mutate(ModelMutation.update(modifiedPlan),
                                            result2 -> Log.i(TAG, "Purchased plan updated."),
                                            error -> Log.e(TAG, "Failed to update purchased plan.", error)
                                    );
                                }
                                else {
                                    Log.e(TAG, "Purchased plan with id " + reservation.getPurchasedPlanId() + " not found!");
                                }
                            },
                            error -> Log.e(TAG, "Failed to get purchased plan", error));
                }
            }
            else {
                Log.i(TAG, "Reservation not found to be updated.");
            }
        }, error -> {
            Log.e(TAG, "Failed to update reservation", error);
        });
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

    public ArrayList<Offer> getOffers(int userId, int currentAccount) {
        Log.i(TAG, "Getting Offers");

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
                        error -> Log.e(TAG, "Query failure", error)
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
        Log.i(TAG, "Getting Offer");

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
                        error -> Log.e(TAG, "Query failure", error)
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

    public void getReferralInfo(String referralId, APICallback<Referral> callback) {
        Amplify.API.query(ModelQuery.get(Referral.class, referralId), response -> {
            Utils.runOnUIThread(() -> {
                if (response.hasData()) {
                    callback.onCallResult(true, response.getData(), null);
                }
                else {
                    Log.e(TAG, "Referral not found.");
                    callback.onCallResult(false, null, null);
                }
            });
        }, error -> {
            Utils.runOnUIThread(() -> {
                Log.e(TAG, "Failed to get referral.", error);
                callback.onCallResult(false, null, error);
            });
        });
    }

    public void createReferral(String offerId, String referrers, APICallback<Referral> callback) {
        Referral referral = Referral.builder().offerId(offerId).referrers(referrers).build();

        Amplify.API.mutate(ModelMutation.create(referral), response -> {
            Utils.runOnUIThread(() -> {
                if (response.hasData()) {
                    callback.onCallResult(true, response.getData(), null);
                }
                else if (response.hasErrors()) {
                    callback.onCallResult(false, null, null);
                }
            });
        }, error -> {
            Utils.runOnUIThread(() -> {
                Log.e(TAG, "Failed to create referral.", error);
                callback.onCallResult(false, null, error);
            });
        });
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

    public void getZoomToken(long startTimeInSeconds, APICallback<String> callback) {
        new Thread() {

            @Override
            public void run() {
                try {
                    GetJWTRequest request = new GetJWTRequest(startTimeInSeconds);
                    GetJWTResponse response = mFunctions.getZoomJWT(request);

                    Utils.runOnUIThread(() -> callback.onCallResult(true, response.token, null));
                } catch (LambdaFunctionException e) {
                    Utils.runOnUIThread(() -> callback.onCallResult(false, null, new ApiException(String.valueOf(e.getMessage()), e, e.getDetails())));
                }
            }

        }.start();
    }

}
