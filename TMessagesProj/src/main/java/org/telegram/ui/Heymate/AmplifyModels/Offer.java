package org.telegram.ui.Heymate.AmplifyModels;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Offer type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Offers", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
@Index(name = "byUser", fields = {"userID"})
public final class Offer implements Model {
  public static final QueryField ID = field("Offer", "id");
  public static final QueryField TITLE = field("Offer", "title");
  public static final QueryField DESCRIPTION = field("Offer", "description");
  public static final QueryField CATEGORY = field("Offer", "category");
  public static final QueryField SUB_CATEGORY = field("Offer", "subCategory");
  public static final QueryField RATE_TYPE = field("Offer", "rateType");
  public static final QueryField CURRENCY = field("Offer", "currency");
  public static final QueryField RATE = field("Offer", "rate");
  public static final QueryField LATITUDE = field("Offer", "latitude");
  public static final QueryField LONGITUDE = field("Offer", "longitude");
  public static final QueryField LOCATION_DATA = field("Offer", "locationData");
  public static final QueryField AVAILABILITY_SLOT = field("Offer", "availabilitySlot");
  public static final QueryField TERMS = field("Offer", "terms");
  public static final QueryField TERMS_CONFIG = field("Offer", "termsConfig");
  public static final QueryField EXPIRY = field("Offer", "expiry");
  public static final QueryField IS_ACTIVE = field("Offer", "isActive");
  public static final QueryField USER_ID = field("Offer", "userID");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String title;
  private final @ModelField(targetType="String") String description;
  private final @ModelField(targetType="String") String category;
  private final @ModelField(targetType="String") String subCategory;
  private final @ModelField(targetType="String") String rateType;
  private final @ModelField(targetType="String") String currency;
  private final @ModelField(targetType="String") String rate;
  private final @ModelField(targetType="String") String latitude;
  private final @ModelField(targetType="String") String longitude;
  private final @ModelField(targetType="String") String locationData;
  private final @ModelField(targetType="AWSJSON") String availabilitySlot;
  private final @ModelField(targetType="String") String terms;
  private final @ModelField(targetType="AWSJSON") String termsConfig;
  private final @ModelField(targetType="AWSDate") Temporal.Date expiry;
  private final @ModelField(targetType="Boolean") Boolean isActive;
  private final @ModelField(targetType="ID", isRequired = true) String userID;
  public String getId() {
      return id;
  }
  
  public String getTitle() {
      return title;
  }
  
  public String getDescription() {
      return description;
  }
  
  public String getCategory() {
      return category;
  }
  
  public String getSubCategory() {
      return subCategory;
  }
  
  public String getRateType() {
      return rateType;
  }
  
  public String getCurrency() {
      return currency;
  }
  
  public String getRate() {
      return rate;
  }
  
  public String getLatitude() {
      return latitude;
  }
  
  public String getLongitude() {
      return longitude;
  }
  
  public String getLocationData() {
      return locationData;
  }
  
  public String getAvailabilitySlot() {
      return availabilitySlot;
  }
  
  public String getTerms() {
      return terms;
  }
  
  public String getTermsConfig() {
      return termsConfig;
  }
  
  public Temporal.Date getExpiry() {
      return expiry;
  }
  
  public Boolean getIsActive() {
      return isActive;
  }
  
  public String getUserId() {
      return userID;
  }
  
  private Offer(String id, String title, String description, String category, String subCategory, String rateType, String currency, String rate, String latitude, String longitude, String locationData, String availabilitySlot, String terms, String termsConfig, Temporal.Date expiry, Boolean isActive, String userID) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.category = category;
    this.subCategory = subCategory;
    this.rateType = rateType;
    this.currency = currency;
    this.rate = rate;
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationData = locationData;
    this.availabilitySlot = availabilitySlot;
    this.terms = terms;
    this.termsConfig = termsConfig;
    this.expiry = expiry;
    this.isActive = isActive;
    this.userID = userID;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Offer offer = (Offer) obj;
      return ObjectsCompat.equals(getId(), offer.getId()) &&
              ObjectsCompat.equals(getTitle(), offer.getTitle()) &&
              ObjectsCompat.equals(getDescription(), offer.getDescription()) &&
              ObjectsCompat.equals(getCategory(), offer.getCategory()) &&
              ObjectsCompat.equals(getSubCategory(), offer.getSubCategory()) &&
              ObjectsCompat.equals(getRateType(), offer.getRateType()) &&
              ObjectsCompat.equals(getCurrency(), offer.getCurrency()) &&
              ObjectsCompat.equals(getRate(), offer.getRate()) &&
              ObjectsCompat.equals(getLatitude(), offer.getLatitude()) &&
              ObjectsCompat.equals(getLongitude(), offer.getLongitude()) &&
              ObjectsCompat.equals(getLocationData(), offer.getLocationData()) &&
              ObjectsCompat.equals(getAvailabilitySlot(), offer.getAvailabilitySlot()) &&
              ObjectsCompat.equals(getTerms(), offer.getTerms()) &&
              ObjectsCompat.equals(getTermsConfig(), offer.getTermsConfig()) &&
              ObjectsCompat.equals(getExpiry(), offer.getExpiry()) &&
              ObjectsCompat.equals(getIsActive(), offer.getIsActive()) &&
              ObjectsCompat.equals(getUserId(), offer.getUserId());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTitle())
      .append(getDescription())
      .append(getCategory())
      .append(getSubCategory())
      .append(getRateType())
      .append(getCurrency())
      .append(getRate())
      .append(getLatitude())
      .append(getLongitude())
      .append(getLocationData())
      .append(getAvailabilitySlot())
      .append(getTerms())
      .append(getTermsConfig())
      .append(getExpiry())
      .append(getIsActive())
      .append(getUserId())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Offer {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
      .append("description=" + String.valueOf(getDescription()) + ", ")
      .append("category=" + String.valueOf(getCategory()) + ", ")
      .append("subCategory=" + String.valueOf(getSubCategory()) + ", ")
      .append("rateType=" + String.valueOf(getRateType()) + ", ")
      .append("currency=" + String.valueOf(getCurrency()) + ", ")
      .append("rate=" + String.valueOf(getRate()) + ", ")
      .append("latitude=" + String.valueOf(getLatitude()) + ", ")
      .append("longitude=" + String.valueOf(getLongitude()) + ", ")
      .append("locationData=" + String.valueOf(getLocationData()) + ", ")
      .append("availabilitySlot=" + String.valueOf(getAvailabilitySlot()) + ", ")
      .append("terms=" + String.valueOf(getTerms()) + ", ")
      .append("termsConfig=" + String.valueOf(getTermsConfig()) + ", ")
      .append("expiry=" + String.valueOf(getExpiry()) + ", ")
      .append("isActive=" + String.valueOf(getIsActive()) + ", ")
      .append("userID=" + String.valueOf(getUserId()))
      .append("}")
      .toString();
  }
  
  public static UserIdStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Offer justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Offer(
      id,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      title,
      description,
      category,
      subCategory,
      rateType,
      currency,
      rate,
      latitude,
      longitude,
      locationData,
      availabilitySlot,
      terms,
      termsConfig,
      expiry,
      isActive,
      userID);
  }
  public interface UserIdStep {
    BuildStep userId(String userId);
  }
  

  public interface BuildStep {
    Offer build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep title(String title);
    BuildStep description(String description);
    BuildStep category(String category);
    BuildStep subCategory(String subCategory);
    BuildStep rateType(String rateType);
    BuildStep currency(String currency);
    BuildStep rate(String rate);
    BuildStep latitude(String latitude);
    BuildStep longitude(String longitude);
    BuildStep locationData(String locationData);
    BuildStep availabilitySlot(String availabilitySlot);
    BuildStep terms(String terms);
    BuildStep termsConfig(String termsConfig);
    BuildStep expiry(Temporal.Date expiry);
    BuildStep isActive(Boolean isActive);
  }
  

  public static class Builder implements UserIdStep, BuildStep {
    private String id;
    private String userID;
    private String title;
    private String description;
    private String category;
    private String subCategory;
    private String rateType;
    private String currency;
    private String rate;
    private String latitude;
    private String longitude;
    private String locationData;
    private String availabilitySlot;
    private String terms;
    private String termsConfig;
    private Temporal.Date expiry;
    private Boolean isActive;
    @Override
     public Offer build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Offer(
          id,
          title,
          description,
          category,
          subCategory,
          rateType,
          currency,
          rate,
          latitude,
          longitude,
          locationData,
          availabilitySlot,
          terms,
          termsConfig,
          expiry,
          isActive,
          userID);
    }
    
    @Override
     public BuildStep userId(String userId) {
        Objects.requireNonNull(userId);
        this.userID = userId;
        return this;
    }
    
    @Override
     public BuildStep title(String title) {
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep description(String description) {
        this.description = description;
        return this;
    }
    
    @Override
     public BuildStep category(String category) {
        this.category = category;
        return this;
    }
    
    @Override
     public BuildStep subCategory(String subCategory) {
        this.subCategory = subCategory;
        return this;
    }
    
    @Override
     public BuildStep rateType(String rateType) {
        this.rateType = rateType;
        return this;
    }
    
    @Override
     public BuildStep currency(String currency) {
        this.currency = currency;
        return this;
    }
    
    @Override
     public BuildStep rate(String rate) {
        this.rate = rate;
        return this;
    }
    
    @Override
     public BuildStep latitude(String latitude) {
        this.latitude = latitude;
        return this;
    }
    
    @Override
     public BuildStep longitude(String longitude) {
        this.longitude = longitude;
        return this;
    }
    
    @Override
     public BuildStep locationData(String locationData) {
        this.locationData = locationData;
        return this;
    }
    
    @Override
     public BuildStep availabilitySlot(String availabilitySlot) {
        this.availabilitySlot = availabilitySlot;
        return this;
    }
    
    @Override
     public BuildStep terms(String terms) {
        this.terms = terms;
        return this;
    }
    
    @Override
     public BuildStep termsConfig(String termsConfig) {
        this.termsConfig = termsConfig;
        return this;
    }
    
    @Override
     public BuildStep expiry(Temporal.Date expiry) {
        this.expiry = expiry;
        return this;
    }
    
    @Override
     public BuildStep isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String title, String description, String category, String subCategory, String rateType, String currency, String rate, String latitude, String longitude, String locationData, String availabilitySlot, String terms, String termsConfig, Temporal.Date expiry, Boolean isActive, String userId) {
      super.id(id);
      super.userId(userId)
        .title(title)
        .description(description)
        .category(category)
        .subCategory(subCategory)
        .rateType(rateType)
        .currency(currency)
        .rate(rate)
        .latitude(latitude)
        .longitude(longitude)
        .locationData(locationData)
        .availabilitySlot(availabilitySlot)
        .terms(terms)
        .termsConfig(termsConfig)
        .expiry(expiry)
        .isActive(isActive);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder description(String description) {
      return (CopyOfBuilder) super.description(description);
    }
    
    @Override
     public CopyOfBuilder category(String category) {
      return (CopyOfBuilder) super.category(category);
    }
    
    @Override
     public CopyOfBuilder subCategory(String subCategory) {
      return (CopyOfBuilder) super.subCategory(subCategory);
    }
    
    @Override
     public CopyOfBuilder rateType(String rateType) {
      return (CopyOfBuilder) super.rateType(rateType);
    }
    
    @Override
     public CopyOfBuilder currency(String currency) {
      return (CopyOfBuilder) super.currency(currency);
    }
    
    @Override
     public CopyOfBuilder rate(String rate) {
      return (CopyOfBuilder) super.rate(rate);
    }
    
    @Override
     public CopyOfBuilder latitude(String latitude) {
      return (CopyOfBuilder) super.latitude(latitude);
    }
    
    @Override
     public CopyOfBuilder longitude(String longitude) {
      return (CopyOfBuilder) super.longitude(longitude);
    }
    
    @Override
     public CopyOfBuilder locationData(String locationData) {
      return (CopyOfBuilder) super.locationData(locationData);
    }
    
    @Override
     public CopyOfBuilder availabilitySlot(String availabilitySlot) {
      return (CopyOfBuilder) super.availabilitySlot(availabilitySlot);
    }
    
    @Override
     public CopyOfBuilder terms(String terms) {
      return (CopyOfBuilder) super.terms(terms);
    }
    
    @Override
     public CopyOfBuilder termsConfig(String termsConfig) {
      return (CopyOfBuilder) super.termsConfig(termsConfig);
    }
    
    @Override
     public CopyOfBuilder expiry(Temporal.Date expiry) {
      return (CopyOfBuilder) super.expiry(expiry);
    }
    
    @Override
     public CopyOfBuilder isActive(Boolean isActive) {
      return (CopyOfBuilder) super.isActive(isActive);
    }
  }
  
}
