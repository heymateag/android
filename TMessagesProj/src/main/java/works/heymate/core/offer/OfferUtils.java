package works.heymate.core.offer;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.core.Texts;
import works.heymate.core.URLs;
import works.heymate.core.Utils;
import works.heymate.model.Offer;
import works.heymate.model.Pricing;
import works.heymate.util.DefaultObjectBuilder;
import works.heymate.util.DefaultObjectProvider;
import works.heymate.util.Template;

public class OfferUtils {

    private static final String TAG = "OfferUtils";

    private static final String PARAMETER_LANGUAGE = "l";
    private static final String PARAMETER_PHRASE_NAME = "p";
    private static final String PARAMETER_DATA = "d";

    public static final String TITLE = "t";
    public static final String DESCRIPTION = "d";
    public static final String CATEGORY = "c";
    public static final String SUB_CATEGORY = "sc";
    public static final String PRICE = "p";
    public static final String CURRENCY = "pc";
    public static final String PAYMENT_TYPE = "pt";
    public static final String ADDRESS = "a";
    public static final String LATITUDE = "la";
    public static final String LONGITUDE = "lo";
    public static final String EXPIRY = "e";
    public static final String INITIAL_DEPOSIT = "de";
    public static final String DELAY_PERCENT = "dp";
    public static final String DELAY_TIME = "dt";
    public static final String CANCEL_HOURS1 = "c1";
    public static final String CANCEL_PERCENT1 = "p1";
    public static final String CANCEL_HOURS2 = "c2";
    public static final String CANCEL_PERCENT2 = "p2";
    public static final String PROMOTION_RATE = "pr";
    public static final String TERMS_CONFIG = "tc";
    public static final String TERMS = "te";

    public static class PhraseInfo {

        public String urlType;
        public String url;
        public JSONObject urlParameters;

        public String offerId;
        public String referralId;

        public String languageCode;
        public String phraseName;

        public APIObject offer;

    }

    /*
    Heymate Offer

    {title}
    {description}

    {name/@username} will provide {sub category} for {price} {currency} {payment_type}.

    Address: {address}

    {initialDeposit}% is paid upfront. {delayPercent}% is returned if they delay more than {delayTime} minutes. ...
    If you cancel {cancelHour1} hours before the appointment {cancelPercent1}% is deposited back to you. ...
    If you cancel {cancelHour2} hours before the appointment {cancelPercent2}% is deposited back to you.

    Learn more here: {url}
     */
    public static String serializeBeautiful(APIObject offer, String referralId, String username, String... additionalFields) {
        String phraseName = Texts.get(Texts.OFFER_PHRASE_NAME).toString();

        String url = deepLinkForOffer(referralId, offer, additionalFields) +
                (additionalFields.length > 0 ? "&" : "?") + PARAMETER_LANGUAGE + "=" + Texts.getLanguageCode() +
                "&" + PARAMETER_PHRASE_NAME + "=" + Base64.encodeToString(phraseName.getBytes(), 0);

        return Template.parse(Texts.get(phraseName).toString()).apply(new DefaultObjectProvider() {

            @Override
            protected Object getRootObject(String name) {
                switch (name) {
                    case "offer":
                        return offer;
                    case "name":
                        return username;
                    case "url":
                        return url;
                }

                return null;
            }

        });
    }

    public static PhraseInfo urlFromPhrase(String phrase) {
        PhraseInfo phraseInfo = new PhraseInfo();

        int urlStart = phrase.indexOf(URLs.getBaseURL(URLs.PATH_OFFER));

        if (urlStart == -1) {
            urlStart = phrase.indexOf(URLs.getBaseURL(URLs.PATH_REFERRAL));

            if (urlStart == -1) {
                return null;
            }

            phraseInfo.urlType = URLs.PATH_REFERRAL;
        }
        else {
            phraseInfo.urlType = URLs.PATH_OFFER;
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

        if (phraseInfo.urlType.equals(URLs.PATH_OFFER)) {
            phraseInfo.offerId = id;
        }
        else {
            phraseInfo.referralId = id;
        }

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

        APIObject offer = offerForDeepLink(phraseInfo);

        try {
            JSONObject jOffer = objectBuilder.getJSON().getJSONObject("offer");

             try {
                 offer.set(Offer.PAYMENT_TERMS, new APIObject(jOffer.getJSONObject("termsConfig")));
             } catch (Throwable t) { }

            try {
                offer.set(Offer.PRICING, new APIObject(jOffer.getJSONObject("pricingInfo")));
            } catch (Throwable t) { }

            offer.set(Offer.TITLE, Utils.getOrNull(jOffer, "title"));
            offer.set(Offer.DESCRIPTION, Utils.getOrNull(jOffer, "description"));
            offer.set(Offer.CATEGORY + "." + Offer.Category.MAIN_CATEGORY, Utils.getOrNull(jOffer, "category"));
            offer.set(Offer.CATEGORY + "." + Offer.Category.SUB_CATEGORY, Utils.getOrNull(jOffer, "subCategory"));
            offer.set(Offer.LOCATION + "." + Offer.Location.ADDRESS, Utils.getOrNull(jOffer, "locationData"));
        } catch (JSONException e) { }

        phraseInfo.offer = offer;

        return phraseInfo;
    }

    public static String deepLinkForOffer(String referralId, APIObject offer, String... additionalFields) {
        StringBuilder sb = new StringBuilder();

        if (referralId == null) {
            sb.append(URLs.getBaseURL(URLs.PATH_OFFER)).append('/').append(offer.getString(Offer.ID));
        }
        else {
            sb.append(URLs.getBaseURL(URLs.PATH_REFERRAL)).append('/').append(referralId);
        }

        if (additionalFields.length > 0) {
            Pricing pricing = null;

            try {
                pricing = new Pricing(offer.getObject(Offer.PRICING).asJSON());
            } catch (Throwable t) { }

            APIObject terms = offer.getObject(Offer.PAYMENT_TERMS);
            APIArray cancellation = terms == null ? null : terms.getArray(Offer.PaymentTerms.CANCELLATION);

            sb.append('?').append(PARAMETER_DATA).append('=');

            JSONObject data = new JSONObject();

            for (String additionalField: additionalFields) {
                switch (additionalField) {
                    case TITLE:
                        Utils.putValues(data, TITLE, offer.getString(Offer.TITLE));
                        continue;
                    case DESCRIPTION:
                        Utils.putValues(data, DESCRIPTION, offer.getString(Offer.DESCRIPTION));
                        continue;
                    case CATEGORY:
                        Utils.putValues(data, CATEGORY, offer.getObject(Offer.CATEGORY).getString(Offer.Category.MAIN_CATEGORY));
                        continue;
                    case SUB_CATEGORY:
                        Utils.putValues(data, SUB_CATEGORY, offer.getObject(Offer.CATEGORY).getString(Offer.Category.SUB_CATEGORY));
                        continue;
                    case PRICE:
                        Utils.putValues(data, PRICE, pricing != null ? String.valueOf(pricing.getPrice()) : null);
                        continue;
                    case CURRENCY:
                        Utils.putValues(data, CURRENCY, pricing != null ? pricing.getCurrency() : null);
                        continue;
                    case PAYMENT_TYPE:
                        Utils.putValues(data, PAYMENT_TYPE, pricing != null ? pricing.getRateType() : null);
                        continue;
                    case ADDRESS:
                        Utils.putValues(data, ADDRESS, offer.getObject(Offer.LOCATION).getString(Offer.Location.ADDRESS));
                        continue;
                    case LATITUDE:
                        Utils.putValues(data, LATITUDE, offer.getObject(Offer.LOCATION).getString(Offer.Location.LATITUDE));
                        continue;
                    case LONGITUDE:
                        Utils.putValues(data, LONGITUDE, offer.getObject(Offer.LOCATION).getString(Offer.Location.LONGITUDE));
                        continue;
                    case EXPIRY:
                        Utils.putValues(data, EXPIRY, offer.getString(Offer.EXPIRATION));
                        continue;
                    case INITIAL_DEPOSIT:
                        Utils.putValues(data, INITIAL_DEPOSIT, terms.getString(Offer.PaymentTerms.DEPOSIT));
                        continue;
                    case DELAY_TIME:
                        Utils.putValues(data, DELAY_TIME, terms.getString(Offer.PaymentTerms.DELAY_IN_START + "." + Offer.PaymentTerms.DelayInStart.DURATION));
                        continue;
                    case DELAY_PERCENT:
                        Utils.putValues(data, DELAY_PERCENT, terms.getString(Offer.PaymentTerms.DELAY_IN_START + "." + Offer.PaymentTerms.DelayInStart.PENALTY));
                        continue;
                    case CANCEL_HOURS1:
                        Utils.putValues(data, CANCEL_HOURS1, cancellation != null && cancellation.size() > 0 ? cancellation.getObject(0).getString(Offer.PaymentTerms.Cancellation.RANGE) : null);
                        continue;
                    case CANCEL_PERCENT1:
                        Utils.putValues(data, CANCEL_PERCENT1, cancellation != null && cancellation.size() > 0 ? cancellation.getObject(0).getString(Offer.PaymentTerms.Cancellation.PENALTY) : null);
                        continue;
                    case CANCEL_HOURS2:
                        Utils.putValues(data, CANCEL_HOURS2, cancellation != null && cancellation.size() > 1 ? cancellation.getObject(1).getString(Offer.PaymentTerms.Cancellation.RANGE) : null);
                        continue;
                    case CANCEL_PERCENT2:
                        Utils.putValues(data, CANCEL_PERCENT2, cancellation != null && cancellation.size() > 1 ? cancellation.getObject(1).getString(Offer.PaymentTerms.Cancellation.PENALTY) : null);
                        continue;
                    case TERMS_CONFIG:
                        Utils.putValues(data, TERMS_CONFIG, terms == null ? null : terms.asJSON().toString());
                        continue;
                    case TERMS:
                        Utils.putValues(data, TERMS, offer.getString(Offer.TERMS_AND_CONDITIONS));
                        continue;
                }
            }

            sb.append(Base64.encodeToString(data.toString().getBytes(), Base64.NO_WRAP | Base64.URL_SAFE));
        }

        return sb.toString();
    }

    public static APIObject offerForDeepLink(PhraseInfo phraseInfo) {
        APIObject offer = new APIObject();

        if (phraseInfo.offerId != null) {
            offer.set(Offer.ID, phraseInfo.offerId);
        }

        if (phraseInfo.urlParameters != null) {
            JSONObject parameters = phraseInfo.urlParameters;

            if (parameters.has(PARAMETER_DATA) && !parameters.isNull(PARAMETER_DATA)) {
                try {
                    JSONObject data = new JSONObject(new String(Base64.decode(parameters.getString(PARAMETER_DATA), Base64.URL_SAFE)));

                    Iterator<String> keys = data.keys();

                    String cancelHours1 = null;
                    String cancelPenalty1 = null;
                    String cancelHours2 = null;
                    String cancelPenalty2 = null;

                    while (keys.hasNext()) {
                        String key = keys.next();

                        switch (key) {
                            case TITLE:
                                offer.set(Offer.TITLE, Utils.getOrNull(data, TITLE));
                                continue;
                            case DESCRIPTION:
                                offer.set(Offer.DESCRIPTION, Utils.getOrNull(data, DESCRIPTION));
                                continue;
                            case CATEGORY:
                                offer.set(Offer.CATEGORY + "." + Offer.Category.MAIN_CATEGORY, Utils.getOrNull(data, CATEGORY));
                                continue;
                            case SUB_CATEGORY:
                                offer.set(Offer.CATEGORY + "." + Offer.Category.SUB_CATEGORY, Utils.getOrNull(data, SUB_CATEGORY));
                                continue;
                            case PRICE:
                                offer.set(Offer.PRICING + "." + Pricing.PRICE, Utils.getOrNull(data, PRICE));
                                continue;
                            case CURRENCY:
                                offer.set(Offer.PRICING + "." + Pricing.CURRENCY, Utils.getOrNull(data, CURRENCY));
                                continue;
                            case PAYMENT_TYPE:
                                offer.set(Offer.PRICING + "." + Pricing.RATE_TYPE, Utils.getOrNull(data, PAYMENT_TYPE));
                                continue;
                            case ADDRESS:
                                offer.set(Offer.LOCATION + "." + Offer.Location.ADDRESS, Utils.getOrNull(data, ADDRESS));
                                continue;
                            case LATITUDE:
                                offer.set(Offer.LOCATION + "." + Offer.Location.LATITUDE, Utils.getOrNull(data, LATITUDE));
                                continue;
                            case LONGITUDE:
                                offer.set(Offer.LOCATION + "." + Offer.Location.LONGITUDE, Utils.getOrNull(data, LONGITUDE));
                                continue;
                            case EXPIRY:
                                offer.set(Offer.EXPIRATION, Utils.getOrNull(data, EXPIRY));
                            case INITIAL_DEPOSIT:
                                offer.set(Offer.PAYMENT_TERMS + "." + Offer.PaymentTerms.DEPOSIT, Utils.getOrNull(data, INITIAL_DEPOSIT));
                                continue;
                            case DELAY_TIME:
                                offer.set(Offer.PAYMENT_TERMS + "." + Offer.PaymentTerms.DELAY_IN_START + "." + Offer.PaymentTerms.DelayInStart.DURATION, Utils.getOrNull(data, DELAY_TIME));
                                continue;
                            case DELAY_PERCENT:
                                offer.set(Offer.PAYMENT_TERMS + "." + Offer.PaymentTerms.DELAY_IN_START + "." + Offer.PaymentTerms.DelayInStart.PENALTY, Utils.getOrNull(data, DELAY_PERCENT));
                                continue;
                            case CANCEL_HOURS1:
                                cancelHours1 = Utils.getOrNull(data, CANCEL_HOURS1);
                                continue;
                            case CANCEL_PERCENT1:
                                cancelPenalty1 = Utils.getOrNull(data, CANCEL_PERCENT1);
                                continue;
                            case CANCEL_HOURS2:
                                cancelHours2 = Utils.getOrNull(data, CANCEL_HOURS2);
                                continue;
                            case CANCEL_PERCENT2:
                                cancelPenalty2 = Utils.getOrNull(data, CANCEL_PERCENT2);
                                continue;
                            case TERMS_CONFIG:
                                String termsStr = Utils.getOrNull(data, TERMS_CONFIG);

                                if (termsStr != null) {
                                    offer.set(Offer.PAYMENT_TERMS, new APIObject(new JSONObject(termsStr)));
                                }
                                continue;
                            case TERMS:
                                offer.set(Offer.TERMS_AND_CONDITIONS, Utils.getOrNull(data, TERMS));
                                continue;
                        }
                    }

                    if (cancelHours1 != null && cancelPenalty1 != null) {
                        APIArray cancellation = new APIArray();

                        APIObject cancel1 = new APIObject(Utils.quickJSON(
                                Offer.PaymentTerms.Cancellation.RANGE, cancelHours1,
                                Offer.PaymentTerms.Cancellation.PENALTY, cancelPenalty1
                        ));
                        cancellation.add(cancel1);

                        if (cancelHours2 != null && cancelPenalty2 != null) {
                            APIObject cancel2 = new APIObject(Utils.quickJSON(
                                    Offer.PaymentTerms.Cancellation.RANGE, cancelHours2,
                                    Offer.PaymentTerms.Cancellation.PENALTY, cancelPenalty2
                            ));
                            cancellation.add(cancel2);
                        }

                        offer.set(Offer.PAYMENT_TERMS + "." + Offer.PaymentTerms.CANCELLATION, cancellation);
                    }
                } catch (JSONException e) {
                    try {
                        Log.e(TAG, "Failed to read data json: " + parameters.get(PARAMETER_DATA), e);
                    } catch (JSONException jsonException) { }
                }
            }
        }

        return offer;
    }

}
