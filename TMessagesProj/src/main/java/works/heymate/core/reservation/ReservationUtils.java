package works.heymate.core.reservation;

import android.net.Uri;
import android.util.Base64;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Offer;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import works.heymate.core.Texts;
import works.heymate.core.URLs;
import works.heymate.core.Utils;

public class ReservationUtils {

    private static final String TAG = "OfferUtils";

    private static final String PARAMETER_LANGUAGE = "l";
    private static final String PARAMETER_PHRASE_NAME = "p";
    private static final String PARAMETER_DATA = "d";

    private static final String PLACEHOLDER_TITLE = "{title}";
    private static final String PLACEHOLDER_DESCRIPTION = "{description}";
    private static final String PLACEHOLDER_NAME = "{name}";
    private static final String PLACEHOLDER_CATEGORY = "{category}";
    private static final String PLACEHOLDER_SUB_CATEGORY = "{sub_category}";
    private static final String PLACEHOLDER_PRICE = "{price}";
    private static final String PLACEHOLDER_CURRENCY = "{currency}";
    private static final String PLACEHOLDER_PAYMENT_TYPE = "{payment_type}";
    private static final String PLACEHOLDER_ADDRESS = "{address}";
    private static final String PLACEHOLDER_EXPIRY = "{expiry}";
    private static final String PLACEHOLDER_INITIAL_DEPOSIT = "{initial_deposit}";
    private static final String PLACEHOLDER_DELAY_PERCENT = "{delay_percent}";
    private static final String PLACEHOLDER_DELAY_TIME = "{delay_time}";
    private static final String PLACEHOLDER_CANCEL_HOURS1 = "{cancel_hours1}";
    private static final String PLACEHOLDER_CANCEL_PERCENT1 = "{cancel_percent1}";
    private static final String PLACEHOLDER_CANCEL_HOURS2 = "{cancel_hours2}";
    private static final String PLACEHOLDER_CANCEL_PERCENT2 = "{cancel_percent2}";
    private static final String PLACEHOLDER_URL = "{url}";

    private static final String[] PLACEHOLDERS = {
            PLACEHOLDER_TITLE, PLACEHOLDER_DESCRIPTION, PLACEHOLDER_NAME, PLACEHOLDER_CATEGORY,
            PLACEHOLDER_SUB_CATEGORY, PLACEHOLDER_PRICE, PLACEHOLDER_CURRENCY,
            PLACEHOLDER_PAYMENT_TYPE, PLACEHOLDER_ADDRESS, PLACEHOLDER_EXPIRY,
            PLACEHOLDER_INITIAL_DEPOSIT, PLACEHOLDER_DELAY_PERCENT, PLACEHOLDER_DELAY_TIME,
            PLACEHOLDER_CANCEL_HOURS1, PLACEHOLDER_CANCEL_PERCENT1, PLACEHOLDER_CANCEL_HOURS2,
            PLACEHOLDER_CANCEL_PERCENT2, PLACEHOLDER_URL
    };

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

        JSONObject terms;

        try {
            terms = new JSONObject(offer.getTermsConfig());
        } catch (JSONException e) {
            terms = new JSONObject();
        }

        String price;
        String currency;
        String rateType;

        try {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));

            price = String.valueOf(pricingInfo.price);
            currency = String.valueOf(pricingInfo.currency);
            rateType = String.valueOf(pricingInfo.rateType);
        } catch (JSONException | NullPointerException e) {
            return url;
        }

        String phrase = Texts.get(phraseName).toString()
                .replace(PLACEHOLDER_TITLE, offer.getTitle())
                .replace(PLACEHOLDER_DESCRIPTION, offer.getDescription())
                .replace(PLACEHOLDER_NAME, username)
                .replace(PLACEHOLDER_CATEGORY, offer.getCategory())
                .replace(PLACEHOLDER_SUB_CATEGORY, offer.getSubCategory())
                .replace(PLACEHOLDER_PRICE, price)
                .replace(PLACEHOLDER_CURRENCY, currency)
                .replace(PLACEHOLDER_PAYMENT_TYPE, rateType)
                .replace(PLACEHOLDER_ADDRESS, offer.getLocationData() == null ? "Online meeting" : offer.getLocationData()) // TODO More flexibility needed for phrase.
                .replace(PLACEHOLDER_EXPIRY, offer.getExpiry().format());

        try {
            phrase = phrase
                    .replace(PLACEHOLDER_INITIAL_DEPOSIT, terms.getString(INITIAL_DEPOSIT))
                    .replace(PLACEHOLDER_DELAY_PERCENT, terms.getString(DELAY_PERCENT))
                    .replace(PLACEHOLDER_DELAY_TIME, terms.getString(DELAY_TIME))
                    .replace(PLACEHOLDER_CANCEL_HOURS1, terms.getString(CANCEL_HOURS1))
                    .replace(PLACEHOLDER_CANCEL_PERCENT1, terms.getString(CANCEL_PERCENT1))
                    .replace(PLACEHOLDER_CANCEL_HOURS2, terms.getString(CANCEL_HOURS2))
                    .replace(PLACEHOLDER_CANCEL_PERCENT2, terms.getString(CANCEL_PERCENT2))
                    .replace(PLACEHOLDER_URL, url);
        } catch (JSONException e) { }

        return phrase;
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

        class Part implements Comparable<Part> {

            int index;
            String placeHolder;
            String value = null;

            Part(int index, String placeHolder) {
                this.index = index;
                this.placeHolder = placeHolder;
            }

            @Override
            public int compareTo(Part o) {
                return index - o.index;
            }

        }

        List<Part> parts = new ArrayList<>(PLACEHOLDERS.length);

        for (String placeHolder: PLACEHOLDERS) {
            int index = rawPhrase.indexOf(placeHolder);

            if (index >= 0) {
                parts.add(new Part(index, placeHolder));
            }
        }

        Collections.sort(parts);

        List<String> separators = new ArrayList<>(parts.size() + 1);
        int index = 0;

        for (Part part: parts) {
            separators.add(rawPhrase.substring(index, part.index));

            index = part.index + part.placeHolder.length();
        }

        separators.add(rawPhrase.substring(index));

        index = 0;

        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);

            int start = phrase.indexOf(separators.get(i), index) + separators.get(i).length();
            int end = phrase.indexOf(separators.get(i + 1), start);

            if (start >= 0 && end >= 0) {
                part.value = phrase.substring(start, end);
            }

            index = end;
        }

        Offer.Builder builder = offerForDeepLink(phraseInfo);

        if (builder == null) {
            return null;
        }

        String price = null;
        String currency = null;
        String rateType = null;

        JSONObject termsConfig = new JSONObject();

        for (Part part: parts) {
            if (part.value == null) {
                continue;
            }

            switch (part.placeHolder) {
                case PLACEHOLDER_TITLE:
                    builder.title(part.value);
                    continue;
                case PLACEHOLDER_DESCRIPTION:
                    builder.description(part.value);
                    continue;
                case PLACEHOLDER_CATEGORY:
                    builder.category(part.value);
                    continue;
                case PLACEHOLDER_SUB_CATEGORY:
                    builder.subCategory(part.value);
                    continue;
                case PLACEHOLDER_PRICE:
                    price = part.value;
                    continue;
                case PLACEHOLDER_CURRENCY:
                    currency = part.value;
                    continue;
                case PLACEHOLDER_PAYMENT_TYPE:
                    rateType = part.value;
                    continue;
                case PLACEHOLDER_ADDRESS:
                    builder.locationData(part.value);
                    continue;
                case PLACEHOLDER_EXPIRY:
                    builder.expiry(new Temporal.Date(part.value));
                case PLACEHOLDER_INITIAL_DEPOSIT:
                    Utils.putValues(termsConfig, INITIAL_DEPOSIT, part.value);
                    continue;
                case PLACEHOLDER_DELAY_TIME:
                    Utils.putValues(termsConfig, DELAY_TIME, part.value);
                    continue;
                case PLACEHOLDER_DELAY_PERCENT:
                    Utils.putValues(termsConfig, DELAY_PERCENT, part.value);
                    continue;
                case PLACEHOLDER_CANCEL_HOURS1:
                    Utils.putValues(termsConfig, CANCEL_HOURS1, part.value);
                    continue;
                case PLACEHOLDER_CANCEL_PERCENT1:
                    Utils.putValues(termsConfig, CANCEL_PERCENT1, part.value);
                    continue;
                case PLACEHOLDER_CANCEL_HOURS2:
                    Utils.putValues(termsConfig, CANCEL_HOURS2, part.value);
                    continue;
                case PLACEHOLDER_CANCEL_PERCENT2:
                    Utils.putValues(termsConfig, CANCEL_PERCENT2, part.value);
                    continue;
            }
        }

        if (price != null && currency != null && rateType != null) {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(
                    Integer.parseInt(price),
                    currency,
                    rateType,
                    0,
                    0,
                    null,
                    0
            );
            builder.pricingInfo(pricingInfo.asJSON().toString());
        }

        if (termsConfig.length() > 0) {
            Offer offer = builder.build();
            String rawTermsConfig = offer.getTermsConfig();

            try {
                JSONObject existingTermsConfig = new JSONObject(rawTermsConfig);

                Iterator<String> keys  = termsConfig.keys();

                while (keys.hasNext()) {
                    String key = keys.next();

                    String value = Utils.getOrNull(termsConfig, key);

                    if (value != null) {
                        Utils.putValues(existingTermsConfig, key, value);
                    }
                }

                builder.termsConfig(existingTermsConfig.toString());
            } catch (Throwable t) {
                builder.termsConfig(termsConfig.toString());
            }
        }

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
            PriceInputItem.PricingInfo pricingInfo = null;

            try {
                pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));
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
                        Utils.putValues(data, CURRENCY, pricingInfo != null ? pricingInfo.currency : null);
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
                        PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(
                                Integer.parseInt(price),
                                currency,
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
