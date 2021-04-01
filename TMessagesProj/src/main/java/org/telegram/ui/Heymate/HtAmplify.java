package org.telegram.ui.Heymate;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.ApiException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.aws.ApiAuthProviders;
import com.amplifyframework.api.aws.sigv4.ApiKeyAuthProvider;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;

import org.telegram.messenger.UserConfig;
import org.telegram.ui.Heymate.AmplifyModels.Offer;
import org.telegram.ui.Heymate.AmplifyModels.TimeSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HtAmplify {

    private static HtAmplify instance;

    private static String API_KEY = "da2-gmv546zpg5edxi6eej66aq263u";

    public interface OfferCallback {

        void onOfferQueryResult(boolean success, Offer offer, ApiException exception);

    }

    public static HtAmplify getInstance(Context context) {
        if (instance == null)
            instance = new HtAmplify(context.getApplicationContext());
        return instance;
    }

    private HtAmplify(Context context) {
        try {
            Amplify.addPlugin(new AWSApiPlugin());
            ApiAuthProviders authProviders = ApiAuthProviders.builder()
                    .apiKeyAuthProvider(new ApiKeyAuthProvider() {
                        @Override
                        public String getAPIKey() {
                            return API_KEY;
                        }
                    })
                    .build();
            Amplify.addPlugin(new AWSApiPlugin(authProviders));
            Amplify.configure(context);

            Log.i("HtAmplify", "Initialized Amplify.");
        } catch (AmplifyException error) {
            Log.e("HtAmplify", "Could not initialize Amplify.", error);
        }
    }

    public Offer createOffer(OfferDto dto, String address, String signature) {
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
                .isActive(true)
                .terms(dto.getTerms())
                .termsConfig(dto.getConfigText())
                .latitude("" + dto.getLatitude())
                .longitude("" + dto.getLongitude())
                .serviceProviderAddress("Address goes here")
                .serviceProviderSignature("Signature goes here")
                .serviceProviderAddress(address)
                .serviceProviderSignature(signature)
                .build();

        Amplify.API.mutate(ModelMutation.create(newOffer),
                response -> {
                    HtSQLite.getInstance().addOffer(newOffer);
                    Log.i("HtAmplify", "Offer Created.");
                },
                error -> Log.e("HtAmplify", "Create failed", error)
        );

        ArrayList<Long> times = dto.getDateSlots();
        for (int i = 0; i < times.size(); i += 2) {

            TimeSlot timeSlot = TimeSlot.builder()
                    .startTime(times.get(i))
                    .endTime(times.get(i + 1))
                    .offerId(newOffer.getId())
                    .status(HtTimeSlotStatus.AVAILABLE.ordinal())
                    .clientUserId("0")
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

    public void bookTimeSlot(String timeSlotId, String clientUserId) {
        Log.i("HtAmplify", "Booking a time slot.");

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

        updateTimeSlot(timeSlotId, HtTimeSlotStatus.ENDED);
    }

    public void cancelTimeSlot(String timeSlotId) {
        Log.i("HtAmplify", "Cancelling a time slot.");

        updateTimeSlot(timeSlotId, HtTimeSlotStatus.CANCELLED);
    }

    private void updateTimeSlot(String timeSlotId, HtTimeSlotStatus status) {
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

    public ArrayList<TimeSlot> getAvailableTimeSlots(String offerId) {
        Log.i("HtAmplify", "Getting time slots");

        Log.i("HtAmplify", "Getting Offers");

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
                        },
                        error -> Log.e("HtAmplify", "Query failure", error)
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

    public void getOffer(String offerId, OfferCallback callback) {
        Amplify.API.query(
                ModelQuery.get(Offer.class, offerId),
                response -> {
                    if (response.hasData()) {
                        callback.onOfferQueryResult(true, response.getData(), null);
                    }
                    else {
                        callback.onOfferQueryResult(false, null, null);
                    }
                },
                error -> callback.onOfferQueryResult(false, null, error)
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
}
