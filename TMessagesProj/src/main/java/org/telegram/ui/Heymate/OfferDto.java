package org.telegram.ui.Heymate;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.TimeSlot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class OfferDto {

    private int id;
    private String title;
    private String description;
    private String rate;
    private String rateType;
    private String currency;
    private String location;
    private String meetingType;
    private int maximumReservations;
    private String time;
    private String category;
    private String subCategory;
    private String configText;
    private String terms;
    private OfferStatus status;
    private int userId;
    private Date expire;
    private double longitude;
    private double latitude;
    private ArrayList<Long> dateSlots;
    private String serverUUID;
    private ArrayList<TimeSlot> timeSlots;
    private int createdAt;
    private int editedAt;

    public int getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(int createdAt) {
        this.createdAt = createdAt;
    }

    public int getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(int editedAt) {
        this.editedAt = editedAt;
    }

    public Offer asOffer() {
        Offer.BuildStep builder = new Offer.Builder()
                .id(serverUUID)
                .title(title)
                .description(description)
                .category(category)
                .subCategory(subCategory)
                .rate(rate)
                .currency(currency)
                .rateType(rateType)
                .locationData(location)
                .latitude(String.valueOf(latitude))
                .longitude(String.valueOf(longitude))
                .meetingType(meetingType)
                .maximumReservations(maximumReservations)
                .termsConfig(configText)
                .terms(terms)
                .expiry(new Temporal.Date(new Date(time))) // Remove At Last
                .createdAt(createdAt)
                .editedAt(editedAt);

        if (expire != null) {
            builder.expiry(new Temporal.Date(expire));
        }

        return builder.build();
    }

    public ArrayList<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(ArrayList<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
    }

    public ArrayList<Long> getDateSlots() {
        return dateSlots;
    }

    public void setDateSlots(ArrayList<Long> dateSlots) {
        this.dateSlots = dateSlots;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Date getExpire() {
        return expire;
    }

    public void setExpire(Date expire) {
        this.expire = expire;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getConfigText() {
        return configText;
    }

    public void setConfigText(String configText) {
        this.configText = configText;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus OfferStatus) {
        this.status = OfferStatus;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public int getMaximumReservations() {
        return maximumReservations;
    }

    public void setMaximumReservations(int maximumReservations) {
        this.maximumReservations = maximumReservations;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimeSlotsAsJson(){
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        if (dateSlots != null) {
            for(int i = 0; i < dateSlots.size(); i += 2){
                array.put("" + dateSlots.get(i) + " - " + dateSlots.get(i + 1));
            }
        }
        try {
            json.put("timeZone", Calendar.getInstance().getTimeZone().getID());
            json.put("times", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
