package works.heymate.core.offer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.MeetingType;
import org.telegram.ui.Heymate.widget.LocationInputItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OfferInfo {

    private static final String LOCATION_INFO = "location_info";
    private static final String MEETING_TYPE = "meeting_type";
    private static final String MAXIMUM_PARTICIPANTS = "maximum_participants";
    private static final String CONFIG = "config";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String TERMS = "terms";
    private static final String CATEGORY = "category";
    private static final String SUB_CATEGORY = "sub_Category";
    private static final String EXPIRE_DATE = "expire_date";
    private static final String CURRENCY = "currency";
    private static final String RATE_TYPE = "rate_type";
    private static final String RATE = "rate";
    private static final String DATE_SLOTS = "date_slots";

    private LocationInputItem.LocationInfo mLocationInfo;
    private String mMeetingType;
    private int mMaximumParticipants = 1;
    private JSONObject mConfig;
    private String mTitle;
    private String mDescription;
    private String mTerms;
    private String mCategory;
    private String mSubCategory;
    private long mExpireDate;
    private String mCurrency;
    private String mRateType;
    private String mRate;
    private List<Long> mDateSlots;

    public OfferInfo() {

    }

    public OfferInfo(JSONObject json) {
        try {
            if (hasProperty(json, LOCATION_INFO)) {
                mLocationInfo = new LocationInputItem.LocationInfo(json.getJSONObject(LOCATION_INFO));
            }
            if (hasProperty(json, MEETING_TYPE)) {
                mMeetingType = json.getString(MEETING_TYPE);
            }
            if (hasProperty(json, MAXIMUM_PARTICIPANTS)) {
                mMaximumParticipants = json.getInt(MAXIMUM_PARTICIPANTS);
            }
            if (hasProperty(json, CONFIG)) {
                mConfig = json.getJSONObject(CONFIG);
            }
            if (hasProperty(json, TITLE)) {
                mTitle = json.getString(TITLE);
            }
            if (hasProperty(json, DESCRIPTION)) {
                mDescription = json.getString(DESCRIPTION);
            }
            if (hasProperty(json, TERMS)) {
                mTerms = json.getString(TERMS);
            }
            if (hasProperty(json, CATEGORY)) {
                mCategory = json.getString(CATEGORY);
            }
            if (hasProperty(json, SUB_CATEGORY)) {
                mSubCategory = json.getString(SUB_CATEGORY);
            }
            if (hasProperty(json, EXPIRE_DATE)) {
                mExpireDate = json.getLong(EXPIRE_DATE);
            }
            if (hasProperty(json, CURRENCY)) {
                mCurrency = json.getString(CURRENCY);
            }
            if (hasProperty(json, RATE_TYPE)) {
                mRateType = json.getString(RATE_TYPE);
            }
            if (hasProperty(json, RATE)) {
                mRate = json.getString(RATE);
            }
            if (hasProperty(json, DATE_SLOTS)) {
                JSONArray jDateSlots = json.getJSONArray(DATE_SLOTS);
                mDateSlots = new ArrayList<>(jDateSlots.length());
                for (int i = 0; i < jDateSlots.length(); i++) {
                    mDateSlots.add(jDateSlots.getLong(i));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public LocationInputItem.LocationInfo getLocationInfo() {
        return mLocationInfo;
    }

    public String getMeetingType() {
        return mMeetingType == null ? MeetingType.DEFAULT : mMeetingType;
    }

    public int getMaximumParticipants() {
        return mMaximumParticipants;
    }

    public JSONObject getConfig() {
        return mConfig;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getTerms() {
        return mTerms;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getSubCategory() {
        return mSubCategory;
    }

    public Date getExpireDate() {
        return mExpireDate == 0 ? null : new Date(mExpireDate);
    }

    public String getCurrency() {
        return mCurrency;
    }

    public String getRateType() {
        return mRateType;
    }

    public String getRate() {
        return mRate;
    }

    public List<Long> getDateSlots() {
        return mDateSlots;
    }

    public void setLocationInfo(LocationInputItem.LocationInfo locationInfo) {
        mLocationInfo = locationInfo;
    }

    public void setMeetingType(String meetingType) {
        mMeetingType = meetingType;
    }

    public void setMaximumParticipants(int participants) {
        mMaximumParticipants = participants;
    }

    public void setConfig(JSONObject config) {
        mConfig = config;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setTerms(String terms) {
        mTerms = terms;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public void setSubCategory(String subCategory) {
        mSubCategory = subCategory;
    }

    public void setExpireDate(Date expireDate) {
        mExpireDate = expireDate == null ? 0 : expireDate.getTime();
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    public void setRateType(String rateType) {
        mRateType = rateType;
    }

    public void setRate(String rate) {
        mRate = rate;
    }

    public void setDateSlots(List<Long> dateSlots) {
        mDateSlots = dateSlots;
    }

    public JSONObject asJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put(LOCATION_INFO, mLocationInfo == null ? JSONObject.NULL : mLocationInfo.asJSON());
            json.put(MEETING_TYPE, mMeetingType == null ? MeetingType.DEFAULT : mMeetingType);
            json.put(MAXIMUM_PARTICIPANTS, mMaximumParticipants);
            json.put(CONFIG, mConfig);
            json.put(TITLE, mTitle);
            json.put(DESCRIPTION, mDescription);
            json.put(TERMS, mTerms);
            json.put(CATEGORY, mCategory);
            json.put(SUB_CATEGORY, mSubCategory);
            json.put(EXPIRE_DATE, mExpireDate);
            json.put(CURRENCY, mCurrency);
            json.put(RATE_TYPE, mRateType);
            json.put(RATE, mRate);

            if (mDateSlots != null) {
                JSONArray jDateSlots = new JSONArray();

                for (Long dateSlot: mDateSlots) {
                    jDateSlots.put(dateSlot);
                }

                json.put(DATE_SLOTS, jDateSlots);
            }
        } catch (JSONException e) { }

        return json;
    }

    private static boolean hasProperty(JSONObject json, String name) {
        return json.has(name) && !json.isNull(name);
    }

}
