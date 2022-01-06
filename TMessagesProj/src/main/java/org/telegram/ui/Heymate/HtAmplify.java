package org.telegram.ui.Heymate;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.ApiException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.OperationType;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.AmplifyConfiguration;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.util.Wrap;

import org.json.JSONException;
import org.json.JSONObject;

import works.heymate.beta.BuildConfig;
import works.heymate.beta.R;
import works.heymate.core.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtAmplify {
    
    private static final String TAG = "HtAmplify";

    private static HtAmplify instance;

    public interface APICallback<T> {

        void onCallResult(boolean success, T result, ApiException exception);

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
            //Amplify.addPlugin(new AWSDataStorePlugin());
            Amplify.configure(AmplifyConfiguration.fromConfigFile(context, HeymateConfig.PRODUCTION ? R.raw.amplifyconfiguration_production : R.raw.amplifyconfiguration_staging), context);

            AWSCredentials credentials = new BasicAWSCredentials(
                    "AKIATNEPMKIM4PV225S6",
                    "xVYv+bzX/EAO16yRwT5Qs+Cr4JLNdcv9cmw9zBbp"
            );

            amazonS3Client = new AmazonS3Client(credentials, Region.getRegion(Regions.EU_CENTRAL_1));
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

}
