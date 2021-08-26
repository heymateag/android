package works.heymate.core.offer;

import android.net.Uri;
import android.util.Base64;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Offer;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import works.heymate.core.Currency;
import works.heymate.core.Texts;
import works.heymate.core.URLs;
import works.heymate.core.Utils;
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

        public Offer offer;

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
    public static String serializeBeautiful(Offer offer, String referralId, String username, String... additionalFields) {
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

        Offer.Builder builder = offerForDeepLink(phraseInfo);

        if (builder == null) {
            return null;
        }

        try {
            JSONObject jOffer = objectBuilder.getJSON().getJSONObject("offer");

             try {
                 builder.termsConfig(jOffer.getJSONObject("termsConfig").toString());
             } catch (Throwable t) { }

            try {
                builder.pricingInfo(jOffer.getJSONObject("pricingInfo").toString());
            } catch (Throwable t) { }

            builder.title(Utils.getOrNull(jOffer, "title"));
            builder.description(Utils.getOrNull(jOffer, "description"));
            builder.category(Utils.getOrNull(jOffer, "category"));
            builder.subCategory(Utils.getOrNull(jOffer, "subCategory"));
            builder.locationData(Utils.getOrNull(jOffer, "locationData"));
        } catch (JSONException e) { }

        phraseInfo.offer = builder.build();

        return phraseInfo;
    }

    public static String deepLinkForOffer(String referralId, Offer offer, String... additionalFields) {
        StringBuilder sb = new StringBuilder();

        if (referralId == null) {
            sb.append(URLs.getBaseURL(URLs.PATH_OFFER)).append('/').append(offer.getId());
        }
        else {
            sb.append(URLs.getBaseURL(URLs.PATH_REFERRAL)).append('/').append(referralId);
        }

        if (additionalFields.length > 0) {
            PricingInfo pricingInfo = null;

            try {
                pricingInfo = new PricingInfo(new JSONObject(offer.getPricingInfo()));
            } catch (Throwable t) { }

            JSONObject termsConfig;

            try {
                termsConfig = new JSONObject(offer.getTermsConfig());
            } catch (JSONException e) {
                termsConfig = new JSONObject();
            }

            sb.append('?').append(PARAMETER_DATA).append('=');

            JSONObject data = new JSONObject();

            for (String additionalField: additionalFields) {
                switch (additionalField) {
                    case TITLE:
                        Utils.putValues(data, TITLE, offer.getTitle());
                        continue;
                    case DESCRIPTION:
                        Utils.putValues(data, DESCRIPTION, offer.getDescription());
                        continue;
                    case CATEGORY:
                        Utils.putValues(data, CATEGORY, offer.getCategory());
                        continue;
                    case SUB_CATEGORY:
                        Utils.putValues(data, SUB_CATEGORY, offer.getSubCategory());
                        continue;
                    case PRICE:
                        Utils.putValues(data, PRICE, pricingInfo != null ? String.valueOf(pricingInfo.price) : null);
                        continue;
                    case CURRENCY:
                        Utils.putValues(data, CURRENCY, pricingInfo != null ? pricingInfo.currency.name() : null);
                        continue;
                    case PAYMENT_TYPE:
                        Utils.putValues(data, PAYMENT_TYPE, pricingInfo != null ? pricingInfo.rateType : null);
                        continue;
                    case ADDRESS:
                        Utils.putValues(data, ADDRESS, offer.getLocationData());
                        continue;
                    case LATITUDE:
                        Utils.putValues(data, LATITUDE, offer.getLatitude());
                        continue;
                    case LONGITUDE:
                        Utils.putValues(data, LONGITUDE, offer.getLongitude());
                        continue;
                    case EXPIRY:
                        Utils.putValues(data, EXPIRY, offer.getExpiry().format());
                        continue;
                    case INITIAL_DEPOSIT:
                        Utils.putValues(data, INITIAL_DEPOSIT, Utils.getOrNull(termsConfig, INITIAL_DEPOSIT));
                        continue;
                    case DELAY_TIME:
                        Utils.putValues(data, DELAY_TIME, Utils.getOrNull(termsConfig, DELAY_TIME));
                        continue;
                    case DELAY_PERCENT:
                        Utils.putValues(data, DELAY_PERCENT, Utils.getOrNull(termsConfig, DELAY_PERCENT));
                        continue;
                    case CANCEL_HOURS1:
                        Utils.putValues(data, CANCEL_HOURS1, Utils.getOrNull(termsConfig, CANCEL_HOURS1));
                        continue;
                    case CANCEL_PERCENT1:
                        Utils.putValues(data, CANCEL_PERCENT1, Utils.getOrNull(termsConfig, CANCEL_PERCENT1));
                        continue;
                    case CANCEL_HOURS2:
                        Utils.putValues(data, CANCEL_HOURS2, Utils.getOrNull(termsConfig, CANCEL_HOURS2));
                        continue;
                    case CANCEL_PERCENT2:
                        Utils.putValues(data, CANCEL_PERCENT2, Utils.getOrNull(termsConfig, CANCEL_PERCENT2));
                        continue;
                    case TERMS_CONFIG:
                        Utils.putValues(data, TERMS_CONFIG, termsConfig.toString());
                        continue;
                    case TERMS:
                        Utils.putValues(data, TERMS, offer.getTerms());
                        continue;
                }
            }

            sb.append(Base64.encodeToString(data.toString().getBytes(), Base64.NO_WRAP | Base64.URL_SAFE));
        }

        return sb.toString();
    }

    public static Offer.Builder offerForDeepLink(PhraseInfo phraseInfo) {
        Offer.Builder builder = new Offer.Builder();

        if (phraseInfo.offerId != null) {
            builder.id(phraseInfo.offerId);
        }

        if (phraseInfo.urlParameters != null) {
            JSONObject parameters = phraseInfo.urlParameters;

            if (parameters != null && parameters.has(PARAMETER_DATA) && !parameters.isNull(PARAMETER_DATA)) {
                try {
                    JSONObject data = new JSONObject(new String(Base64.decode(parameters.getString(PARAMETER_DATA), Base64.URL_SAFE)));

                    String price = null;
                    String currency = null;
                    String rateType = null;

                    String termsConfigStr = null;
                    JSONObject termsConfig = new JSONObject();

                    Iterator<String> keys = data.keys();

                    while (keys.hasNext()) {
                        String key = keys.next();

                        switch (key) {
                            case TITLE:
                                builder.title(Utils.getOrNull(data, TITLE));
                                continue;
                            case DESCRIPTION:
                                builder.description(Utils.getOrNull(data, DESCRIPTION));
                                continue;
                            case CATEGORY:
                                builder.category(Utils.getOrNull(data, CATEGORY));
                                continue;
                            case SUB_CATEGORY:
                                builder.subCategory(Utils.getOrNull(data, SUB_CATEGORY));
                                continue;
                            case PRICE:
                                price = Utils.getOrNull(data, PRICE);
                                continue;
                            case CURRENCY:
                                currency = Utils.getOrNull(data, CURRENCY);
                                continue;
                            case PAYMENT_TYPE:
                                rateType = Utils.getOrNull(data, PAYMENT_TYPE);
                                continue;
                            case ADDRESS:
                                builder.locationData(Utils.getOrNull(data, ADDRESS));
                                continue;
                            case LATITUDE:
                                builder.latitude(Utils.getOrNull(data, LATITUDE));
                                continue;
                            case LONGITUDE:
                                builder.longitude(Utils.getOrNull(data, LONGITUDE));
                                continue;
                            case EXPIRY:
                                String expiry = Utils.getOrNull(data, EXPIRY);

                                if (expiry != null) {
                                    builder.expiry(new Temporal.Date(expiry)); // TODO Might crash
                                }
                            case INITIAL_DEPOSIT:
                                Utils.putValues(termsConfig, INITIAL_DEPOSIT, Utils.getOrNull(data, INITIAL_DEPOSIT));
                                continue;
                            case DELAY_TIME:
                                Utils.putValues(termsConfig, DELAY_TIME, Utils.getOrNull(data, DELAY_TIME));
                                continue;
                            case DELAY_PERCENT:
                                Utils.putValues(termsConfig, DELAY_PERCENT, Utils.getOrNull(data, DELAY_PERCENT));
                                continue;
                            case CANCEL_HOURS1:
                                Utils.putValues(termsConfig, CANCEL_HOURS1, Utils.getOrNull(data, CANCEL_HOURS1));
                                continue;
                            case CANCEL_PERCENT1:
                                Utils.putValues(termsConfig, CANCEL_PERCENT1, Utils.getOrNull(data, CANCEL_PERCENT1));
                                continue;
                            case CANCEL_HOURS2:
                                Utils.putValues(termsConfig, CANCEL_HOURS2, Utils.getOrNull(data, CANCEL_HOURS2));
                                continue;
                            case CANCEL_PERCENT2:
                                Utils.putValues(termsConfig, CANCEL_PERCENT2, Utils.getOrNull(data, CANCEL_PERCENT2));
                                continue;
                            case TERMS_CONFIG:
                                termsConfigStr = Utils.getOrNull(data, TERMS_CONFIG);
                                continue;
                            case TERMS:
                                builder.terms(Utils.getOrNull(data, TERMS));
                                continue;
                        }
                    }

                    if (price != null && currency != null && rateType != null) {
                        PricingInfo pricingInfo = new PricingInfo(
                                Integer.parseInt(price),
                                Currency.forName(currency),
                                rateType,
                                0,
                                0,
                                null,
                                0
                        );
                        builder.pricingInfo(pricingInfo.asJSON().toString());
                    }

                    if (termsConfigStr != null) {
                        builder.termsConfig(termsConfigStr);
                    }
                    else if (termsConfig.length() > 0) {
                        builder.termsConfig(termsConfig.toString());
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
