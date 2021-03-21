package org.telegram.ui.Heymate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.Heymate.AmplifyModels.Offer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OfferDto {

    private int id;
    private String title;
    private String description;
    private String rate;
    private String rateType;
    private String currency;
    private String location;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimeSlotsAsJson(){
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        for(int i = 0; i < dateSlots.size(); i += 2){
            array.put("" + dateSlots.get(i) + " - " + dateSlots.get(i + 1));
        }
        try {
            json.put("timeZone", Calendar.getInstance().getTimeZone().getDisplayName());
            json.put("times", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
