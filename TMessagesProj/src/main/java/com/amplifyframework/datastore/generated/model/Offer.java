package com.amplifyframework.datastore.generated.model;

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
  public static final QueryField AVAILABILITY_SLOT = field("Offer", "availabilitySlot");
  public static final QueryField CATEGORY = field("Offer", "category");
  public static final QueryField CREATED_AT = field("Offer", "createdAt");
  public static final QueryField PRICING_INFO = field("Offer", "pricingInfo");
  public static final QueryField DESCRIPTION = field("Offer", "description");
  public static final QueryField EDITED_AT = field("Offer", "editedAt");
  public static final QueryField EXPIRY = field("Offer", "expiry");
  public static final QueryField LATITUDE = field("Offer", "latitude");
  public static final QueryField LOCATION_DATA = field("Offer", "locationData");
  public static final QueryField LONGITUDE = field("Offer", "longitude");
  public static final QueryField MEETING_TYPE = field("Offer", "meetingType");
  public static final QueryField MAXIMUM_RESERVATIONS = field("Offer", "maximumReservations");
  public static final QueryField WALLET_ADDRESS = field("Offer", "walletAddress");
  public static final QueryField PRICE_SIGNATURE = field("Offer", "priceSignature");
  public static final QueryField BUNDLE_SIGNATURE = field("Offer", "bundleSignature");
  public static final QueryField SUBSCRIPTION_SIGNATURE = field("Offer", "subscriptionSignature");
  public static final QueryField STATUS = field("Offer", "status");
  public static final QueryField SUB_CATEGORY = field("Offer", "subCategory");
  public static final QueryField TERMS = field("Offer", "terms");
  public static final QueryField TERMS_CONFIG = field("Offer", "termsConfig");
  public static final QueryField TITLE = field("Offer", "title");
  public static final QueryField USER_ID = field("Offer", "userID");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="AWSJSON") String availabilitySlot;
  private final @ModelField(targetType="String") String category;
  private final @ModelField(targetType="Int") Integer createdAt;
  private final @ModelField(targetType="String") String pricingInfo;
  private final @ModelField(targetType="String") String description;
  private final @ModelField(targetType="Int") Integer editedAt;
  private final @ModelField(targetType="AWSDate") Temporal.Date expiry;
  private final @ModelField(targetType="String") String latitude;
  private final @ModelField(targetType="String") String locationData;
  private final @ModelField(targetType="String") String longitude;
  private final @ModelField(targetType="String") String meetingType;
  private final @ModelField(targetType="Int") Integer maximumReservations;
  private final @ModelField(targetType="String") String walletAddress;
  private final @ModelField(targetType="String") String priceSignature;
  private final @ModelField(targetType="String") String bundleSignature;
  private final @ModelField(targetType="String") String subscriptionSignature;
  private final @ModelField(targetType="Int") Integer status;
  private final @ModelField(targetType="String") String subCategory;
  private final @ModelField(targetType="String") String terms;
  private final @ModelField(targetType="AWSJSON") String termsConfig;
  private final @ModelField(targetType="String") String title;
  private final @ModelField(targetType="ID", isRequired = true) String userID;
  public String getId() {
      return id;
  }
  
  public String getAvailabilitySlot() {
      return availabilitySlot;
  }
  
  public String getCategory() {
      return category;
  }
  
  public Integer getCreatedAt() {
      return createdAt;
  }
  
  public String getPricingInfo() {
      return pricingInfo;
  }
  
  public String getDescription() {
      return description;
  }
  
  public Integer getEditedAt() {
      return editedAt;
  }
  
  public Temporal.Date getExpiry() {
      return expiry;
  }
  
  public String getLatitude() {
      return latitude;
  }
  
  public String getLocationData() {
      return locationData;
  }
  
  public String getLongitude() {
      return longitude;
  }
  
  public String getMeetingType() {
      return meetingType;
  }
  
  public Integer getMaximumReservations() {
      return maximumReservations;
  }
  
  public String getWalletAddress() {
      return walletAddress;
  }
  
  public String getPriceSignature() {
      return priceSignature;
  }
  
  public String getBundleSignature() {
      return bundleSignature;
  }
  
  public String getSubscriptionSignature() {
      return subscriptionSignature;
  }
  
  public Integer getStatus() {
      return status;
  }
  
  public String getSubCategory() {
      return subCategory;
  }
  
  public String getTerms() {
      return terms;
  }
  
  public String getTermsConfig() {
      return termsConfig;
  }
  
  public String getTitle() {
      return title;
  }
  
  public String getUserId() {
      return userID;
  }
  
  private Offer(String id, String availabilitySlot, String category, Integer createdAt, String pricingInfo, String description, Integer editedAt, Temporal.Date expiry, String latitude, String locationData, String longitude, String meetingType, Integer maximumReservations, String walletAddress, String priceSignature, String bundleSignature, String subscriptionSignature, Integer status, String subCategory, String terms, String termsConfig, String title, String userID) {
    this.id = id;
    this.availabilitySlot = availabilitySlot;
    this.category = category;
    this.createdAt = createdAt;
    this.pricingInfo = pricingInfo;
    this.description = description;
    this.editedAt = editedAt;
    this.expiry = expiry;
    this.latitude = latitude;
    this.locationData = locationData;
    this.longitude = longitude;
    this.meetingType = meetingType;
    this.maximumReservations = maximumReservations;
    this.walletAddress = walletAddress;
    this.priceSignature = priceSignature;
    this.bundleSignature = bundleSignature;
    this.subscriptionSignature = subscriptionSignature;
    this.status = status;
    this.subCategory = subCategory;
    this.terms = terms;
    this.termsConfig = termsConfig;
    this.title = title;
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
              ObjectsCompat.equals(getAvailabilitySlot(), offer.getAvailabilitySlot()) &&
              ObjectsCompat.equals(getCategory(), offer.getCategory()) &&
              ObjectsCompat.equals(getCreatedAt(), offer.getCreatedAt()) &&
              ObjectsCompat.equals(getPricingInfo(), offer.getPricingInfo()) &&
              ObjectsCompat.equals(getDescription(), offer.getDescription()) &&
              ObjectsCompat.equals(getEditedAt(), offer.getEditedAt()) &&
              ObjectsCompat.equals(getExpiry(), offer.getExpiry()) &&
              ObjectsCompat.equals(getLatitude(), offer.getLatitude()) &&
              ObjectsCompat.equals(getLocationData(), offer.getLocationData()) &&
              ObjectsCompat.equals(getLongitude(), offer.getLongitude()) &&
              ObjectsCompat.equals(getMeetingType(), offer.getMeetingType()) &&
              ObjectsCompat.equals(getMaximumReservations(), offer.getMaximumReservations()) &&
              ObjectsCompat.equals(getWalletAddress(), offer.getWalletAddress()) &&
              ObjectsCompat.equals(getPriceSignature(), offer.getPriceSignature()) &&
              ObjectsCompat.equals(getBundleSignature(), offer.getBundleSignature()) &&
              ObjectsCompat.equals(getSubscriptionSignature(), offer.getSubscriptionSignature()) &&
              ObjectsCompat.equals(getStatus(), offer.getStatus()) &&
              ObjectsCompat.equals(getSubCategory(), offer.getSubCategory()) &&
              ObjectsCompat.equals(getTerms(), offer.getTerms()) &&
              ObjectsCompat.equals(getTermsConfig(), offer.getTermsConfig()) &&
              ObjectsCompat.equals(getTitle(), offer.getTitle()) &&
              ObjectsCompat.equals(getUserId(), offer.getUserId());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getAvailabilitySlot())
      .append(getCategory())
      .append(getCreatedAt())
      .append(getPricingInfo())
      .append(getDescription())
      .append(getEditedAt())
      .append(getExpiry())
      .append(getLatitude())
      .append(getLocationData())
      .append(getLongitude())
      .append(getMeetingType())
      .append(getMaximumReservations())
      .append(getWalletAddress())
      .append(getPriceSignature())
      .append(getBundleSignature())
      .append(getSubscriptionSignature())
      .append(getStatus())
      .append(getSubCategory())
      .append(getTerms())
      .append(getTermsConfig())
      .append(getTitle())
      .append(getUserId())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Offer {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("availabilitySlot=" + String.valueOf(getAvailabilitySlot()) + ", ")
      .append("category=" + String.valueOf(getCategory()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("pricingInfo=" + String.valueOf(getPricingInfo()) + ", ")
      .append("description=" + String.valueOf(getDescription()) + ", ")
      .append("editedAt=" + String.valueOf(getEditedAt()) + ", ")
      .append("expiry=" + String.valueOf(getExpiry()) + ", ")
      .append("latitude=" + String.valueOf(getLatitude()) + ", ")
      .append("locationData=" + String.valueOf(getLocationData()) + ", ")
      .append("longitude=" + String.valueOf(getLongitude()) + ", ")
      .append("meetingType=" + String.valueOf(getMeetingType()) + ", ")
      .append("maximumReservations=" + String.valueOf(getMaximumReservations()) + ", ")
      .append("walletAddress=" + String.valueOf(getWalletAddress()) + ", ")
      .append("priceSignature=" + String.valueOf(getPriceSignature()) + ", ")
      .append("bundleSignature=" + String.valueOf(getBundleSignature()) + ", ")
      .append("subscriptionSignature=" + String.valueOf(getSubscriptionSignature()) + ", ")
      .append("status=" + String.valueOf(getStatus()) + ", ")
      .append("subCategory=" + String.valueOf(getSubCategory()) + ", ")
      .append("terms=" + String.valueOf(getTerms()) + ", ")
      .append("termsConfig=" + String.valueOf(getTermsConfig()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
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
      availabilitySlot,
      category,
      createdAt,
      pricingInfo,
      description,
      editedAt,
      expiry,
      latitude,
      locationData,
      longitude,
      meetingType,
      maximumReservations,
      walletAddress,
      priceSignature,
      bundleSignature,
      subscriptionSignature,
      status,
      subCategory,
      terms,
      termsConfig,
      title,
      userID);
  }
  public interface UserIdStep {
    BuildStep userId(String userId);
  }
  

  public interface BuildStep {
    Offer build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep availabilitySlot(String availabilitySlot);
    BuildStep category(String category);
    BuildStep createdAt(Integer createdAt);
    BuildStep pricingInfo(String pricingInfo);
    BuildStep description(String description);
    BuildStep editedAt(Integer editedAt);
    BuildStep expiry(Temporal.Date expiry);
    BuildStep latitude(String latitude);
    BuildStep locationData(String locationData);
    BuildStep longitude(String longitude);
    BuildStep meetingType(String meetingType);
    BuildStep maximumReservations(Integer maximumReservations);
    BuildStep walletAddress(String walletAddress);
    BuildStep priceSignature(String priceSignature);
    BuildStep bundleSignature(String bundleSignature);
    BuildStep subscriptionSignature(String subscriptionSignature);
    BuildStep status(Integer status);
    BuildStep subCategory(String subCategory);
    BuildStep terms(String terms);
    BuildStep termsConfig(String termsConfig);
    BuildStep title(String title);
  }
  

  public static class Builder implements UserIdStep, BuildStep {
    private String id;
    private String userID;
    private String availabilitySlot;
    private String category;
    private Integer createdAt;
    private String pricingInfo;
    private String description;
    private Integer editedAt;
    private Temporal.Date expiry;
    private String latitude;
    private String locationData;
    private String longitude;
    private String meetingType;
    private Integer maximumReservations;
    private String walletAddress;
    private String priceSignature;
    private String bundleSignature;
    private String subscriptionSignature;
    private Integer status;
    private String subCategory;
    private String terms;
    private String termsConfig;
    private String title;
    @Override
     public Offer build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Offer(
          id,
          availabilitySlot,
          category,
          createdAt,
          pricingInfo,
          description,
          editedAt,
          expiry,
          latitude,
          locationData,
          longitude,
          meetingType,
          maximumReservations,
          walletAddress,
          priceSignature,
          bundleSignature,
          subscriptionSignature,
          status,
          subCategory,
          terms,
          termsConfig,
          title,
          userID);
    }
    
    @Override
     public BuildStep userId(String userId) {
        Objects.requireNonNull(userId);
        this.userID = userId;
        return this;
    }
    
    @Override
     public BuildStep availabilitySlot(String availabilitySlot) {
        this.availabilitySlot = availabilitySlot;
        return this;
    }
    
    @Override
     public BuildStep category(String category) {
        this.category = category;
        return this;
    }
    
    @Override
     public BuildStep createdAt(Integer createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    @Override
     public BuildStep pricingInfo(String pricingInfo) {
        this.pricingInfo = pricingInfo;
        return this;
    }
    
    @Override
     public BuildStep description(String description) {
        this.description = description;
        return this;
    }
    
    @Override
     public BuildStep editedAt(Integer editedAt) {
        this.editedAt = editedAt;
        return this;
    }
    
    @Override
     public BuildStep expiry(Temporal.Date expiry) {
        this.expiry = expiry;
        return this;
    }
    
    @Override
     public BuildStep latitude(String latitude) {
        this.latitude = latitude;
        return this;
    }
    
    @Override
     public BuildStep locationData(String locationData) {
        this.locationData = locationData;
        return this;
    }
    
    @Override
     public BuildStep longitude(String longitude) {
        this.longitude = longitude;
        return this;
    }
    
    @Override
     public BuildStep meetingType(String meetingType) {
        this.meetingType = meetingType;
        return this;
    }
    
    @Override
     public BuildStep maximumReservations(Integer maximumReservations) {
        this.maximumReservations = maximumReservations;
        return this;
    }
    
    @Override
     public BuildStep walletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
        return this;
    }
    
    @Override
     public BuildStep priceSignature(String priceSignature) {
        this.priceSignature = priceSignature;
        return this;
    }
    
    @Override
     public BuildStep bundleSignature(String bundleSignature) {
        this.bundleSignature = bundleSignature;
        return this;
    }
    
    @Override
     public BuildStep subscriptionSignature(String subscriptionSignature) {
        this.subscriptionSignature = subscriptionSignature;
        return this;
    }
    
    @Override
     public BuildStep status(Integer status) {
        this.status = status;
        return this;
    }
    
    @Override
     public BuildStep subCategory(String subCategory) {
        this.subCategory = subCategory;
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
     public BuildStep title(String title) {
        this.title = title;
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
    private CopyOfBuilder(String id, String availabilitySlot, String category, Integer createdAt, String pricingInfo, String description, Integer editedAt, Temporal.Date expiry, String latitude, String locationData, String longitude, String meetingType, Integer maximumReservations, String walletAddress, String priceSignature, String bundleSignature, String subscriptionSignature, Integer status, String subCategory, String terms, String termsConfig, String title, String userId) {
      super.id(id);
      super.userId(userId)
        .availabilitySlot(availabilitySlot)
        .category(category)
        .createdAt(createdAt)
        .pricingInfo(pricingInfo)
        .description(description)
        .editedAt(editedAt)
        .expiry(expiry)
        .latitude(latitude)
        .locationData(locationData)
        .longitude(longitude)
        .meetingType(meetingType)
        .maximumReservations(maximumReservations)
        .walletAddress(walletAddress)
        .priceSignature(priceSignature)
        .bundleSignature(bundleSignature)
        .subscriptionSignature(subscriptionSignature)
        .status(status)
        .subCategory(subCategory)
        .terms(terms)
        .termsConfig(termsConfig)
        .title(title);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
    }
    
    @Override
     public CopyOfBuilder availabilitySlot(String availabilitySlot) {
      return (CopyOfBuilder) super.availabilitySlot(availabilitySlot);
    }
    
    @Override
     public CopyOfBuilder category(String category) {
      return (CopyOfBuilder) super.category(category);
    }
    
    @Override
     public CopyOfBuilder createdAt(Integer createdAt) {
      return (CopyOfBuilder) super.createdAt(createdAt);
    }
    
    @Override
     public CopyOfBuilder pricingInfo(String pricingInfo) {
      return (CopyOfBuilder) super.pricingInfo(pricingInfo);
    }
    
    @Override
     public CopyOfBuilder description(String description) {
      return (CopyOfBuilder) super.description(description);
    }
    
    @Override
     public CopyOfBuilder editedAt(Integer editedAt) {
      return (CopyOfBuilder) super.editedAt(editedAt);
    }
    
    @Override
     public CopyOfBuilder expiry(Temporal.Date expiry) {
      return (CopyOfBuilder) super.expiry(expiry);
    }
    
    @Override
     public CopyOfBuilder latitude(String latitude) {
      return (CopyOfBuilder) super.latitude(latitude);
    }
    
    @Override
     public CopyOfBuilder locationData(String locationData) {
      return (CopyOfBuilder) super.locationData(locationData);
    }
    
    @Override
     public CopyOfBuilder longitude(String longitude) {
      return (CopyOfBuilder) super.longitude(longitude);
    }
    
    @Override
     public CopyOfBuilder meetingType(String meetingType) {
      return (CopyOfBuilder) super.meetingType(meetingType);
    }
    
    @Override
     public CopyOfBuilder maximumReservations(Integer maximumReservations) {
      return (CopyOfBuilder) super.maximumReservations(maximumReservations);
    }
    
    @Override
     public CopyOfBuilder walletAddress(String walletAddress) {
      return (CopyOfBuilder) super.walletAddress(walletAddress);
    }
    
    @Override
     public CopyOfBuilder priceSignature(String priceSignature) {
      return (CopyOfBuilder) super.priceSignature(priceSignature);
    }
    
    @Override
     public CopyOfBuilder bundleSignature(String bundleSignature) {
      return (CopyOfBuilder) super.bundleSignature(bundleSignature);
    }
    
    @Override
     public CopyOfBuilder subscriptionSignature(String subscriptionSignature) {
      return (CopyOfBuilder) super.subscriptionSignature(subscriptionSignature);
    }
    
    @Override
     public CopyOfBuilder status(Integer status) {
      return (CopyOfBuilder) super.status(status);
    }
    
    @Override
     public CopyOfBuilder subCategory(String subCategory) {
      return (CopyOfBuilder) super.subCategory(subCategory);
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
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
  }
  
}
