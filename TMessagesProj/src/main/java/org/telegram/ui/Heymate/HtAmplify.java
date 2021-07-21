package org.telegram.ui.Heymate;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.ApiException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.OperationType;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.predicate.QueryField;
import com.amplifyframework.core.model.query.predicate.QueryPredicate;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.PurchasedPlan;
import com.amplifyframework.datastore.generated.model.Referral;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.amplifyframework.datastore.generated.model.Shop;
import com.amplifyframework.datastore.generated.model.TimeSlot;
import com.amplifyframework.util.Wrap;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;

import works.heymate.beta.BuildConfig;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Utils;
import works.heymate.core.offer.PurchasePlanTypes;

import org.telegram.messenger.UserConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static HtAmplify getInstance(Context context) {
        if (instance == null)
            instance = new HtAmplify(context.getApplicationContext());
        return instance;
    }

    private Context context;

    public AmazonS3Client amazonS3Client;

    public Context getContext(){
        return context;
    }

    private HtAmplify(Context context) {
        this.context = context;

        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSDataStorePlugin());
            Amplify.configure(context);

            AWSCredentials credentials = new BasicAWSCredentials(
                    "AKIATNEPMKIM2UIPWSPC",
                    "y2qEASauUedSjUyLrbDZZ6qTZ4uzIG02y/z/Boco"
            );

            amazonS3Client = new AmazonS3Client(credentials);

            StaticCredentialsProvider credentialsProvider = new StaticCredentialsProvider(credentials);

            amazonS3Client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
            amazonS3Client.setEndpoint("https://s3-eu-central-1.amazonaws.com/");

            Log.i(TAG, "Initialized Amplify.");
        } catch (AmplifyException error) {
            Log.e(TAG, "Could not initialize Amplify.", error);
        }
    }

    public enum ShopType {
        Shop,
        MarketPlace
    }

    public void createOffer(Offer.BuildStep offerBuilder, List<Long> times, APICallback<Offer> callback) {
        Amplify.API.mutate(ModelMutation.create(offerBuilder.build()),
                response -> {
                    Offer offer = response.getData();

                    if (offer == null) {
                        Utils.runOnUIThread(() -> callback.onCallResult(false, null, null));
                        return;
                    }

                    String fcmToken = TG2HM.getFCMToken();

                    String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

                    int maximumReservations = offer.getMaximumReservations();

                    // TODO Request in a for?
                    for (int i = 0; i < times.size(); i += 2) {
                        TimeSlot timeSlot = TimeSlot.builder()
                                .startTime((int) (times.get(i) / 1000))
                                .endTime((int) (times.get(i + 1) / 1000))
                                .offerId(offer.getId())
                                .userId(userId)
                                .userFcmToken(fcmToken)
                                .maximumReservations(maximumReservations)
                                .completedReservations(0)
                                .remainingReservations(maximumReservations == 0 ? Integer.MAX_VALUE : maximumReservations)
                                .meetingType(offer.getMeetingType())
                                .build();

                        Amplify.API.mutate(ModelMutation.create(timeSlot),
                                response2 -> {
                                    Log.i(TAG, "Time Slot created");
                                },
                                error -> Log.e(TAG, "Time Slot creation failed", error)
                        );
                    }

                    Utils.runOnUIThread(() -> callback.onCallResult(true, offer, null));
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

            if (callback != null) {
                Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
            }
        });
    }

    public Reservation createReservation(TimeSlot timeSlot, PurchasedPlan purchasedPlan, Referral referral) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);
        String fcmToken = TG2HM.getFCMToken();

        return Reservation.builder()
                .consumerId(userId)
                .consumerFcmToken(fcmToken)
                .offerId(timeSlot.getOfferId())
                .purchasedPlanId(purchasedPlan == null ? null : purchasedPlan.getId())
                .purchasedPlanType(purchasedPlan == null ? PurchasePlanTypes.SINGLE : purchasedPlan.getPlanType())
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
                        .remainingReservations(timeSlot.getMaximumReservations() == 0 ? Integer.MAX_VALUE : timeSlot.getRemainingReservations() - 1)
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

        Amplify.API.query(
                ModelQuery.list(
                        PurchasedPlan.class,
                        PurchasedPlan.CONSUMER_ID.eq(userId)
                                .and(PurchasedPlan.PLAN_TYPE.ne(PurchasePlanTypes.SINGLE))
                ),
                result -> {
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

    public void getMyPendingOnlineReservations(APICallback<List<Reservation>> callback) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

        getReservations(Reservation.CONSUMER_ID.eq(userId).and(Reservation.MEETING_TYPE.eq(MeetingType.ONLINE_MEETING).and(Reservation.STATUS.ne(HtTimeSlotStatus.FINISHED.name()).and(Reservation.STATUS.ne(HtTimeSlotStatus.CANCELLED_BY_CONSUMER.name()).and(Reservation.STATUS.ne(HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER.name()))))), callback);
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
        updateReservation(reservation, status, (APICallback<Reservation>) null);
    }

    public void updateReservation(Reservation reservation, HtTimeSlotStatus status, APICallback<Reservation> callback) {
        Reservation mutatedReservation = reservation.copyOfBuilder()
                .status(status.name())
                .build();

        updateReservation(mutatedReservation, callback);
    }

    public void updateReservation(Reservation reservation, HtTimeSlotStatus status, String meetingId) {
        Reservation mutatedReservation = reservation.copyOfBuilder()
                .status(status.name())
                .meetingId(meetingId)
                .build();

        updateReservation(mutatedReservation, (APICallback<Reservation>) null);
    }

    private void updateReservation(Reservation reservation, APICallback<Reservation> callback) {
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
                                    .remainingReservations(result1.getMaximumReservations() == 0 ? Integer.MAX_VALUE : result1.getRemainingReservations() + 1)
                                    .build();

                            Amplify.API.mutate(ModelMutation.update(mutatedTimeSlot), result2 -> {
                                Log.i(TAG, "Time slot updated successfully.");

                                resumeReservationUpdate(reservation, callback);
                            }, error -> {
                                Log.e(TAG, "Failed to update time slot.", error);

                                resumeReservationUpdate(reservation, callback);
                            });
                        }
                        else {
                            resumeReservationUpdate(reservation, callback);
                        }
                    });
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
                                        result2 -> {
                                            Log.i(TAG, "Purchased plan updated.");

                                            if (callback != null) {
                                                Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                                            }
                                        },
                                        error -> {
                                            Log.e(TAG, "Failed to update purchased plan.", error);

                                            if (callback != null) {
                                                Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                                            }
                                        }
                                );
                            }
                            else {
                                Log.e(TAG, "Purchased plan with id " + reservation.getPurchasedPlanId() + " not found!");

                                if (callback != null) {
                                    Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                                }
                            }
                        },
                        error -> {
                            Log.e(TAG, "Failed to get purchased plan", error);

                            if (callback != null) {
                                Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                            }
                        });
                }
                else if (callback != null) {
                    Utils.runOnUIThread(() -> callback.onCallResult(true, result.getData(), null));
                }
            }
            else {
                Log.i(TAG, "Reservation not found to be updated.");

                if (callback != null) {
                    Utils.runOnUIThread(() -> callback.onCallResult(false, null, null));
                }
            }
        }, error -> {
            Log.e(TAG, "Failed to update reservation", error);

            if (callback != null) {
                Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
            }
        });
    }

    private void resumeReservationUpdate(Reservation reservation, APICallback<Reservation> callback) {
        if (reservation.getPurchasedPlanId() != null) {
            Amplify.API.query(ModelQuery.get(PurchasedPlan.class, reservation.getPurchasedPlanId()), result1 -> {
                    if (result1.hasData()) {
                        PurchasedPlan purchasedPlan = result1.getData();
                        PurchasedPlan modifiedPlan = purchasedPlan.copyOfBuilder()
                                .pendingReservationsCount(purchasedPlan.getPendingReservationsCount() - 1)
                                .build();

                        Amplify.API.mutate(ModelMutation.update(modifiedPlan),
                                result2 -> {
                                    Log.i(TAG, "Purchased plan updated.");

                                    if (callback != null) {
                                        Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                                    }
                                },
                                error -> {
                                    if (callback != null) {
                                        Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                                    }

                                    Log.e(TAG, "Failed to update purchased plan.", error);
                                }
                        );
                    }
                    else {
                        Log.e(TAG, "Purchased plan with id " + reservation.getPurchasedPlanId() + " not found!");

                        if (callback != null) {
                            Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                        }
                    }
            },
            error -> {
                Log.e(TAG, "Failed to get purchased plan", error);

                if (callback != null) {
                    Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
                }
            });
        }
        else if (callback != null) {
            Utils.runOnUIThread(() -> callback.onCallResult(true, reservation, null));
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

    public void getMyOffers(APICallback<List<Offer>> callback) {
        String userId = String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);

        Amplify.API.query(ModelQuery.list(Offer.class, Offer.USER_ID.eq(userId)), result -> {
            List<Offer> offers = new ArrayList<>();

            if (result.hasData()) {
                for (Offer offer: result.getData()) {
                    offers.add(offer);
                }
            }

            Utils.runOnUIThread(() -> callback.onCallResult(true, offers, null));
        }, error -> {
            Log.e(TAG, "Failed to get my offers", error);

            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    public void getOffers(Collection<String> ids, APICallback<List<Offer>> callback) {
        Amplify.API.query(ModelQuery.list(Offer.class, buildOneOfPredicate(ids, Offer.ID)), result -> {
            List<Offer> offers = new ArrayList<>();

            for (Offer offer: result.getData()) {
                offers.add(offer);
            }

            Utils.runOnUIThread(() -> callback.onCallResult(true, offers, null));
        }, error -> {
            Log.e(TAG, "Failed to get offers.", error);
            Utils.runOnUIThread(() -> callback.onCallResult(false, null, error));
        });
    }

    private QueryPredicate buildOneOfPredicate(Collection<String> ids, QueryField field) {
        QueryPredicate predicate = null;

        for (String id: ids) {
            if (predicate == null) {
                predicate = field.eq(id);
            }
            else {
                predicate = predicate.or(field.eq(id));
            }
        }

        return predicate;
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

    public void getZoomToken(String userName, String sessionName, long startTimeInSeconds, APICallback<String> callback) {
        Map<String, String> variableTypes = new HashMap<>();
        variableTypes.put("exp", "Float");
        variableTypes.put("iat", "Float");
        variableTypes.put("session_name", "String");
        variableTypes.put("user_identity", "String");
        variableTypes.put("version", "Int");

        List<String> inputKeys = Arrays.asList("exp", "iat", "session_name", "user_identity", "version");
        Collections.sort(inputKeys);

        List<String> inputTypes = new ArrayList<>();
        List<String> inputParameters = new ArrayList<>();
        for (String key : inputKeys) {
            inputTypes.add("$" + key + ": " + variableTypes.get(key));
            inputParameters.add(key + ": $" + key);
        }

        String inputTypeString = Wrap.inParentheses(TextUtils.join(", ", inputTypes));
        String inputParameterString = Wrap.inParentheses(TextUtils.join(", ", inputParameters));

        String operationString =
                "getZoomJWTQuery" +
                        inputParameterString;

        String document = OperationType.QUERY.getName() + " " +
                "getZoomJWTQuery" + inputTypeString +
                Wrap.inPrettyBraces(operationString, "", "  ") + "\n";

        int version = BuildConfig.VERSION_CODE;
        long iat = startTimeInSeconds;
        long exp = startTimeInSeconds + 48L * 60L * 60L;

        Map<String, Object> variables = new HashMap<>();
        variables.put("exp", exp);
        variables.put("iat", iat);
        variables.put("session_name", sessionName);
        variables.put("user_identity", userName);
        variables.put("version", version);

        GraphQLRequest<String> request = new SimpleGraphQLRequest<>(document, variables, String.class, new GsonVariablesSerializer());

        Amplify.API.query(request, result -> {
            try {
                JSONObject response = new JSONObject(result.getData());

                String token = response.getString("getZoomJWTQuery");

                Utils.runOnUIThread(() -> callback.onCallResult(true, token, null));
            } catch (JSONException e) {
                Utils.runOnUIThread(() -> callback.onCallResult(false, null, new ApiException(e.getMessage(), e, "")));
            }
        }, error -> Utils.runOnUIThread(() -> callback.onCallResult(false, null, error)));
    }

    public void uploadFile(String id, File file) throws AmazonClientException {
        amazonS3Client.putObject("offerdocuments", id, file);
    }

    public S3Object downloadFile(String id) throws AmazonClientException {
        return amazonS3Client.getObject("offerdocuments", id);
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
