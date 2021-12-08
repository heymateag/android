package works.heymate.ramp.alphafortress;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.HeymateConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import works.heymate.core.APICallback;
import works.heymate.core.Currency;
import works.heymate.core.Utils;
import works.heymate.util.SimpleNetworkCall;

public class BeneficiaryModel {

    private static final String MODEL_URL = AlphaFortressness.BASE_URL + "destination-currencies?name=";
    private static final String BENEFICIARY_URL = AlphaFortressness.BASE_URL + "benificiaries";

    private static final String KEY_MODEL_BASE = "alphafortress_beneficiarymodel_";
    private static final String KEY_BENEFICIARY_BASE = "alphafortress_beneficiary_";

    static void get(Currency currency, APICallback<BeneficiaryModel> callback) {
        String modelJSON = HeymateConfig.getGeneral().get(KEY_MODEL_BASE + currency.name());

        try {
            BeneficiaryModel model = new BeneficiaryModel(new JSONObject(modelJSON));

            Utils.postOnUIThread(() -> callback.onAPICallResult(true, model, null));
            return;
        } catch (JSONException | NullPointerException e) { }

        AlphaToken.get().getToken((success, token, exception) -> {
            if (success) {
                SimpleNetworkCall.callAsync(result -> {
                    if (result.arrayResponse != null) {
                        try {
                            JSONObject jBeneficiary = result.arrayResponse.getJSONObject(0);

                            BeneficiaryModel model = new BeneficiaryModel(jBeneficiary);

                            HeymateConfig.getGeneral().set(KEY_MODEL_BASE + currency.name(), jBeneficiary.toString());

                            callback.onAPICallResult(true, model, null);
                        } catch (JSONException e) {
                            callback.onAPICallResult(false, null, e);
                        }
                    }
                    else {
                        callback.onAPICallResult(false, null, result.exception);
                    }
                }, MODEL_URL + currency.name(), null, "Authorization", "Bearer " + token);
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

    static boolean hasBeneficiary(Currency currency) {
        return HeymateConfig.getGeneral().get(KEY_BENEFICIARY_BASE + currency.name()) != null;
    }

    static long getBeneficiaryId(Currency currency) {
        return Long.parseLong(HeymateConfig.getGeneral().get(KEY_BENEFICIARY_BASE + currency.name()));
    }

    static void createBeneficiary(Currency currency, BeneficiaryModel model, APICallback<Long> callback) {
        AlphaToken.get().getToken((success, token, exception) -> {
            if (token != null) {
                JSONObject body = new JSONObject();

                try {
                    body.put("destination_currency", model.id);

                    for (Field field: model.fields) {
                        body.put(field.key, field.getValue());
                    }
                } catch (JSONException e) { }

                SimpleNetworkCall.callAsync(result -> {
                    if (result.response != null) {
                        try {
                            long id = result.response.getLong("id");

                            HeymateConfig.getGeneral().set(KEY_MODEL_BASE + currency.name(), model.asJSON().toString());
                            HeymateConfig.getGeneral().set(KEY_BENEFICIARY_BASE + currency.name(), String.valueOf(id));

                            callback.onAPICallResult(true, id, null);
                        } catch (JSONException e) {
                            callback.onAPICallResult(false, null, e);
                        }
                    }
                    else {
                        callback.onAPICallResult(false, null, result.exception);
                    }
                }, BENEFICIARY_URL, body, "Authorization", "Bearer " + token);
            }
            else {
                callback.onAPICallResult(false, null, exception);
            }
        });
    }

    public enum FieldType { TEXT, NUMBER, EMAIL }

    public static class Field {

        private final JSONObject model;

        public final String key;
        public final FieldType type;
        public final String title;
        public final String placeholder;
        public final boolean required;

        public String value = null;

        private Field(String key, JSONObject jModel) throws JSONException {
            this.key = key;

            model = jModel;

            switch (jModel.getString("type")) {
                case "number":
                    type = FieldType.NUMBER;
                    break;
                case "email":
                    type = FieldType.EMAIL;
                    break;
                case "text":
                default:
                    type = FieldType.TEXT;
                    break;
            }

            title = jModel.getString("Title");

            if (jModel.has("Placeholder")) {
                placeholder = jModel.getString("Placeholder");
            }
            else if (jModel.has("Plaseholder")) {
                placeholder = jModel.getString("Plaseholder");
            }
            else {
                placeholder = null;
            }

            JSONObject jValidations = jModel.getJSONObject("validations");
            required = jValidations.getBoolean("required");

            if (jModel.has("value") && !jModel.isNull("value")) {
                value = jModel.getString("value");
            }
        }

        private Object getValue() {
            if (value == null) {
                return JSONObject.NULL;
            }

            if (type == FieldType.NUMBER) {
                return Long.parseLong(value);
            }

            return value;
        }

        JSONObject asJSON() {
            try {
                model.put("value", value == null ? JSONObject.NULL : value);
            } catch (JSONException e) { }

            return model;
        }

    }

    public final long id;
    public final List<Field> fields;

    private BeneficiaryModel(JSONObject jBeneficiary) throws JSONException {
        id = jBeneficiary.getLong("id");

        JSONObject jMetadata = jBeneficiary.getJSONObject("metadata").getJSONObject("bank");

        fields = new ArrayList<>(jMetadata.length());

        Iterator<String> keys = jMetadata.keys();

        while (keys.hasNext()) {
            String key = keys.next();

            fields.add(new Field(key, jMetadata.getJSONObject(key)));
        }
    }

    public boolean validate() {
        for (Field field: fields) {
            if (field.required && TextUtils.isEmpty(field.value)) {
                return false;
            }
        }

        return true;
    }

    boolean hasChanges(BeneficiaryModel that) {
        if (this.id != that.id) {
            return true;
        }

        List<Field> thatFields = new ArrayList<>(that.fields);

        for (Field field: fields) {
            for (Field thatField: thatFields) {
                if (field.key.equals(thatField.key)) {
                    if (!TextUtils.equals(field.value, thatField.value)) {
                        return true;
                    }

                    thatFields.remove(thatField);
                    break;
                }
            }

            return true;
        }

        return !thatFields.isEmpty();
    }

    JSONObject asJSON() {
        JSONObject jBeneficiary = new JSONObject();

        try {
            jBeneficiary.put("id", id);

            JSONObject jBank = new JSONObject();

            for (Field field: fields) {
                jBank.put(field.key, field.asJSON());
            }

            JSONObject jMetadata = new JSONObject();
            jMetadata.put("bank", jBank);

            jBeneficiary.put("metadata", jMetadata);
        } catch (JSONException e) { }

        return jBeneficiary;
    }

}
