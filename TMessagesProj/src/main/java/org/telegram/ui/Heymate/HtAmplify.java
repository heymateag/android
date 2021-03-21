package org.telegram.ui.Heymate;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.BasicAPIKeyAuthProvider;
import com.amazonaws.mobileconnectors.appsync.sigv4.BasicCognitoUserPoolsAuthProvider;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.aws.ApiAuthProviders;
import com.amplifyframework.api.aws.sigv4.ApiKeyAuthProvider;
import com.amplifyframework.api.graphql.QueryType;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.apollographql.apollo.api.Query;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HtAmplify {

    private static HtAmplify instance;
    private AWSAppSyncClient awsClient;

    private static String API_KEY = "da2-gmv546zpg5edxi6eej66aq263u";

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

    public Offer createOffer(OfferDto dto) {
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
                .build();

        Amplify.API.mutate(ModelMutation.create(newOffer),
                response -> {
                    HtSQLite.getInstance().addOffer(newOffer);
                    Log.i("HtAmplify", "Offer Created.");
                },
                error -> Log.e("HtAmplify", "Create failed", error)
        );

        return newOffer;
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
