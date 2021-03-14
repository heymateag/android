package org.telegram.ui.Heymate;

import android.content.Context;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HtAmplify {

    private static HtAmplify instance;
    private static Context context;

    public static HtAmplify getInstance() {
        return instance;
    }

    public static void setContext(Context context) {
        HtAmplify.context = context;
        if(instance == null)
            instance = new HtAmplify();
    }

    private HtAmplify(){
        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(context);

            Log.i("HtAmplify", "Initialized Amplify.");
        } catch (AmplifyException error) {
            Log.e("HtAmplify", "Could not initialize Amplify.", error);
        }
    }

    public Offer createOffer(OfferDto dto){
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
                .availabilitySlot("{\"time\": \"01-01-2021\"}")
                .locationData(dto.getLocation())
                .isActive(true)
                .terms(dto.getTerms())
                .termsConfig(dto.getConfigText())
                .latitude("" + dto.getLatitude())
                .longitude("" + dto.getLongitude())
                .build();

        Amplify.API.mutate(ModelMutation.create(newOffer),
                response -> Log.i("HtAmplify", "Offer Created."),
                error -> Log.e("HtAmplify", "Create failed", error)
        );

        return newOffer;
    }

    public ArrayList<Offer> getOffers(int userId, int currentAccount){
        Log.i("HtAmplify", "Getting Offers");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<ArrayList<Offer>> future = pool.submit(new Callable<ArrayList<Offer>>() {
            @Override
            public ArrayList<Offer> call() throws Exception {
                ArrayList<Offer> offers = new ArrayList();
                Amplify.API.query(
                        ModelQuery.list(Offer.class, Offer.USER_ID.eq("" + userId)),
                        response -> {
                            for (Offer offer : response.getData()) {
                                offers.add(offer);
                            }
                            OfferController.getInstance().updateOffers(offers, currentAccount);
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
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
