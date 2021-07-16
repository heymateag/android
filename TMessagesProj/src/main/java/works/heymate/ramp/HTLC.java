package works.heymate.ramp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class HTLC {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CLEARED = "cleared";
    public static final String STATUS_SETTLED = "settled";
    public static final String STATUS_EXPIRED = "expired";

    public static final String CLEARING_STATUS_WAITING = "waiting";
    public static final String CLEARING_STATUS_PARTIAL = "partial";
    public static final String CLEARING_STATUS_DENIED = "denied";

    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String ASSET = "asset";
    private static final String AMOUNT = "amount";
    private static final String FEE = "fee";
    private static final String EXPIRES = "expires";
    private static final String CLEARING = "clearing";

    private static final String CLEARING_STATUS = "status";
    private static final String CLEARING_OPTIONS = "options";

    private static final String OPTION_TYPE = "type";
    private static final String OPTION_AMOUNT = "amount";
    private static final String OPTION_RECIPIENT = "recipient";
    private static final String OPTION_PURPOSE = "purpose";

    private static final String RECIPIENT_NAME = "name";
    private static final String RECIPIENT_IBAN = "iban";
    private static final String RECIPIENT_BIC = "bic";

    public final String id;
    public final String status;
    public final String asset;
    public final float amount;
    public final float fee;
    public final long expires;
    public final Clearing clearing;

    HTLC(JSONObject jHTLC) throws JSONException {
        id = jHTLC.getString(ID);
        status = jHTLC.getString(STATUS);
        asset = jHTLC.getString(ASSET);
        amount = (float) jHTLC.getDouble(AMOUNT);
        fee = (float) jHTLC.getDouble(FEE);
        String sExpires = jHTLC.getString(EXPIRES);

        if (jHTLC.has(CLEARING)) {
            clearing = new Clearing(jHTLC.getJSONObject(CLEARING));
        }
        else {
            clearing = null;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Nimiq.TIMESTAMP_PATTERN, Locale.US);
            expires = dateFormat.parse(sExpires).getTime();
        } catch (ParseException | NullPointerException e) {
            throw new JSONException(e.getMessage());
        }
    }

    public static class Clearing {

        public final String status;
        public final Option[] options;

        private Clearing(JSONObject jClearing) throws JSONException {
            status = jClearing.getString(CLEARING_STATUS);

            JSONArray jOptions = jClearing.getJSONArray(CLEARING_OPTIONS);

            options = new Option[jOptions.length()];

            for (int i = 0; i < options.length; i++) {
                options[i] = Option.create(jOptions.getJSONObject(i));
            }
        }

    }

    public static class Option {

        private static Option create(JSONObject jOption) {
            try {
                return new Option(jOption);
            } catch (JSONException e) {
                return null;
            }
        }

        public final String type;
        public final float amount;
        public final Recipient recipient;
        public final String purpose;

        private Option(JSONObject jOption) throws JSONException {
            type = jOption.getString(OPTION_TYPE);
            amount = (float) jOption.getDouble(OPTION_AMOUNT);
            recipient = new Recipient(jOption.getJSONObject(OPTION_RECIPIENT));
            purpose = jOption.getString(OPTION_PURPOSE);
        }

    }

    public static class Recipient {

        public final String name;
        public final String iban;
        public final String bic;

        private Recipient(JSONObject jRecipient) throws JSONException {
            name = jRecipient.getString(RECIPIENT_NAME);
            iban = jRecipient.getString(RECIPIENT_IBAN);
            bic = jRecipient.getString(RECIPIENT_BIC);
        }

    }

}
