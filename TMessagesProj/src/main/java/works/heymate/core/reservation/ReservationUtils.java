package works.heymate.core.reservation;

import android.net.Uri;
import android.util.Base64;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;

import java.util.Iterator;

import works.heymate.core.Texts;
import works.heymate.core.URLs;
import works.heymate.core.Utils;
import works.heymate.util.DefaultObjectBuilder;
import works.heymate.util.DefaultObjectProvider;
import works.heymate.util.Template;

public class ReservationUtils {

    private static final String TAG = "ReservationUtils";

    private static final String PARAMETER_LANGUAGE = "l";
    private static final String PARAMETER_PHRASE_NAME = "p";
    private static final String PARAMETER_DATA = "d";

    public static final String OFFER_ID = "o";
    public static final String TIME_SLOT_ID = "ti";
    public static final String PURCHASED_PLAN_ID = "pi";
    public static final String PURCHASED_PLAN_TYPE = "pt";
    public static final String START_TIME = "st";
    public static final String END_TIME = "et";
    public static final String SERVICE_PROVIDER_ID = "si";
    public static final String CONSUMER_ID = "ci";
    public static final String MEETING_TYPE = "mt";
    public static final String MEETING_ID = "mi";

    public static class PhraseInfo {

        public String urlType;
        public String url;
        public JSONObject urlParameters;

        public String reservationId;

        public String languageCode;
        public String phraseName;

        public Reservation reservation;

    }

    /*
    heymate reservation

    {offer.title}
    Has already started!

    Click the link below to join:
    {url}
     */
    public static String serializeBeautiful(Reservation reservation, Offer ofer, String... additionalFields) {
        String phraseName = Texts.get(Texts.RESERVATION_PHRASE_NAME).toString();

        String url = deepLinkForReservation(reservation, additionalFields) +
                (additionalFields.length > 0 ? "&" : "?") + PARAMETER_LANGUAGE + "=" + Texts.getLanguageCode() +
                "&" + PARAMETER_PHRASE_NAME + "=" + Base64.encodeToString(phraseName.getBytes(), 0);

        return Template.parse(Texts.get(phraseName).toString()).apply(new DefaultObjectProvider() {

            @Override
            protected Object getRootObject(String name) {
                switch (name) {
                    case "reservation":
                        return reservation;
                    case "offer":
                        return ofer;
                    case "url":
                        return url;
                }

                return null;
            }

        });
    }

    public static PhraseInfo urlFromPhrase(String phrase) {
        PhraseInfo phraseInfo = new PhraseInfo();

        int urlStart = phrase.indexOf(URLs.getBaseURL(URLs.PATH_RESERVATION));

        if (urlStart == -1) {
                return null;
        }
        else {
            phraseInfo.urlType = URLs.PATH_RESERVATION;
        }

        int urlEnd = phrase.indexOf(" ", urlStart);

        if (urlEnd == -1) {
            urlEnd = phrase.length();
        }

        phraseInfo.url = phrase.substring(urlStart, urlEnd);

        String id = Uri.parse(phraseInfo.url).getLastPathSegment();

        if (id == null || id.length() == 0 || phraseInfo.urlType.equals(id)) {
            return null;
        }

        phraseInfo.reservationId = id;

        return phraseInfo;
    }

    // TODO We have a fail case where the phrase is edited. Which is unhandled. Should return null in case of value types not matching. Otherwise will cause crashes.
    public static PhraseInfo readBeautiful(String phrase) {
        PhraseInfo phraseInfo = urlFromPhrase(phrase);

        if (phraseInfo == null) {
            return null;
        }

        phraseInfo.urlParameters = URLs.getParameters(phraseInfo.url);

        if (phraseInfo.urlParameters == null) {
            return null;
        }

        phraseInfo.languageCode = Utils.getOrNull(phraseInfo.urlParameters, PARAMETER_LANGUAGE);
        phraseInfo.phraseName = Utils.getOrNull(phraseInfo.urlParameters, PARAMETER_PHRASE_NAME);

        if (phraseInfo.phraseName == null) {
            return null;
        }

        phraseInfo.phraseName = new String(Base64.decode(phraseInfo.phraseName, 0));

        String rawPhrase;

        try {
            rawPhrase = Texts.get(phraseInfo.phraseName, phraseInfo.languageCode).toString();
        } catch (Throwable t) {
            return null;
        }

        DefaultObjectBuilder objectBuilder = new DefaultObjectBuilder();
        Template.parse(rawPhrase).build(phrase, objectBuilder);

        Reservation.Builder builder = reservationForDeepLink(phraseInfo);

        if (builder == null) {
            return null;
        }

        try {
            JSONObject jReservation = objectBuilder.getJSON().getJSONObject("reservation");

//            builder.title(Utils.getOrNull(jReservation, "title"));
//            builder.description(Utils.getOrNull(jReservation, "description"));
//            builder.category(Utils.getOrNull(jReservation, "category"));
//            builder.subCategory(Utils.getOrNull(jReservation, "subCategory"));
//            builder.locationData(Utils.getOrNull(jReservation, "locationData"));
        } catch (JSONException e) { }

        phraseInfo.reservation = builder.build();

        return phraseInfo;
    }

    public static String deepLinkForReservation(Reservation reservation, String... additionalFields) {
        StringBuilder sb = new StringBuilder();

        sb.append(URLs.getBaseURL(URLs.PATH_RESERVATION)).append('/').append(reservation.getId());

        if (additionalFields.length > 0) {
            sb.append('?').append(PARAMETER_DATA).append('=');

            JSONObject data = new JSONObject();

            for (String additionalField: additionalFields) {
                switch (additionalField) {
                    case OFFER_ID:
                        Utils.putValues(data, OFFER_ID, reservation.getOfferId());
                        continue;
                    case TIME_SLOT_ID:
                        Utils.putValues(data, TIME_SLOT_ID, reservation.getTimeSlotId());
                        continue;
                    case PURCHASED_PLAN_ID:
                        Utils.putValues(data, PURCHASED_PLAN_ID, reservation.getPurchasedPlanId());
                        continue;
                    case PURCHASED_PLAN_TYPE:
                        Utils.putValues(data, PURCHASED_PLAN_TYPE, reservation.getPurchasedPlanType());
                        continue;
                    case START_TIME:
                        Utils.putValues(data, START_TIME, reservation.getStartTime().toString());
                        continue;
                    case END_TIME:
                        Utils.putValues(data, END_TIME, reservation.getEndTime().toString());
                        continue;
                    case SERVICE_PROVIDER_ID:
                        Utils.putValues(data, SERVICE_PROVIDER_ID, reservation.getServiceProviderId());
                        continue;
                    case CONSUMER_ID:
                        Utils.putValues(data, CONSUMER_ID, reservation.getConsumerId());
                        continue;
                    case MEETING_TYPE:
                        Utils.putValues(data, MEETING_TYPE, reservation.getMeetingType());
                        continue;
                    case MEETING_ID:
                        Utils.putValues(data, MEETING_ID, reservation.getMeetingId());
                        continue;
                }
            }

            sb.append(Base64.encodeToString(data.toString().getBytes(), Base64.NO_WRAP | Base64.URL_SAFE));
        }

        return sb.toString();
    }

    public static Reservation.Builder reservationForDeepLink(PhraseInfo phraseInfo) {
        Reservation.Builder builder = new Reservation.Builder();

        if (phraseInfo.reservationId != null) {
            builder.id(phraseInfo.reservationId);
        }

        if (phraseInfo.urlParameters != null) {
            JSONObject parameters = phraseInfo.urlParameters;

            if (parameters != null && parameters.has(PARAMETER_DATA) && !parameters.isNull(PARAMETER_DATA)) {
                try {
                    JSONObject data = new JSONObject(new String(Base64.decode(parameters.getString(PARAMETER_DATA), Base64.URL_SAFE)));

                    Iterator<String> keys = data.keys();

                    while (keys.hasNext()) {
                        String key = keys.next();

                        switch (key) {
                            case OFFER_ID:
                                builder.offerId(Utils.getOrNull(data, OFFER_ID));
                                continue;
                            case TIME_SLOT_ID:
                                builder.timeSlotId(Utils.getOrNull(data, TIME_SLOT_ID));
                                continue;
                            case PURCHASED_PLAN_ID:
                                builder.purchasedPlanId(Utils.getOrNull(data, PURCHASED_PLAN_ID));
                                continue;
                            case PURCHASED_PLAN_TYPE:
                                builder.purchasedPlanType(Utils.getOrNull(data, PURCHASED_PLAN_TYPE));
                                continue;
                            case START_TIME:
                                String startTime = Utils.getOrNull(data, START_TIME);
                                builder.startTime(startTime == null ? null : Integer.parseInt(startTime));
                                continue;
                            case END_TIME:
                                String endTime = Utils.getOrNull(data, END_TIME);
                                builder.endTime(endTime == null ? null : Integer.parseInt(endTime));
                                continue;
                            case SERVICE_PROVIDER_ID:
                                builder.serviceProviderId(Utils.getOrNull(data, SERVICE_PROVIDER_ID));
                                continue;
                            case CONSUMER_ID:
                                builder.consumerId(Utils.getOrNull(data, CONSUMER_ID));
                                continue;
                            case MEETING_TYPE:
                                builder.meetingType(Utils.getOrNull(data, MEETING_TYPE));
                                continue;
                            case MEETING_ID:
                                builder.meetingId(Utils.getOrNull(data, MEETING_ID));
                                continue;
                        }
                    }
                } catch (JSONException e) {
                    try {
                        Log.e(TAG, "Failed to read data json: " + parameters.get(PARAMETER_DATA), e);
                    } catch (JSONException jsonException) { }
                }
            }
        }

        return builder;
    }

}
