package com.amplifyframework.datastore.generated.model;


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

/** This is an auto generated class representing the Reservation type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Reservations", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class Reservation implements Model {
  public static final QueryField ID = field("Reservation", "id");
  public static final QueryField OFFER_ID = field("Reservation", "offerId");
  public static final QueryField TIME_SLOT_ID = field("Reservation", "timeSlotId");
  public static final QueryField PURCHASED_PLAN_ID = field("Reservation", "purchasedPlanId");
  public static final QueryField PURCHASED_PLAN_TYPE = field("Reservation", "purchasedPlanType");
  public static final QueryField START_TIME = field("Reservation", "startTime");
  public static final QueryField END_TIME = field("Reservation", "endTime");
  public static final QueryField REFERRAL_ID = field("Reservation", "referralId");
  public static final QueryField REFERRERS = field("Reservation", "referrers");
  public static final QueryField SERVICE_PROVIDER_ID = field("Reservation", "serviceProviderId");
  public static final QueryField SERVICE_PROVIDER_FCM_TOKEN = field("Reservation", "serviceProviderFCMToken");
  public static final QueryField CONSUMER_ID = field("Reservation", "consumerId");
  public static final QueryField CONSUMER_FCM_TOKEN = field("Reservation", "consumerFCMToken");
  public static final QueryField STATUS = field("Reservation", "status");
  public static final QueryField MEETING_TYPE = field("Reservation", "meetingType");
  public static final QueryField MEETING_ID = field("Reservation", "meetingId");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String offerId;
  private final @ModelField(targetType="String") String timeSlotId;
  private final @ModelField(targetType="String") String purchasedPlanId;
  private final @ModelField(targetType="String") String purchasedPlanType;
  private final @ModelField(targetType="Int") Integer startTime;
  private final @ModelField(targetType="Int") Integer endTime;
  private final @ModelField(targetType="String") String referralId;
  private final @ModelField(targetType="String") String referrers;
  private final @ModelField(targetType="String") String serviceProviderId;
  private final @ModelField(targetType="String") String serviceProviderFCMToken;
  private final @ModelField(targetType="String") String consumerId;
  private final @ModelField(targetType="String") String consumerFCMToken;
  private final @ModelField(targetType="String") String status;
  private final @ModelField(targetType="String") String meetingType;
  private final @ModelField(targetType="String") String meetingId;
  public String getId() {
      return id;
  }
  
  public String getOfferId() {
      return offerId;
  }
  
  public String getTimeSlotId() {
      return timeSlotId;
  }
  
  public String getPurchasedPlanId() {
      return purchasedPlanId;
  }
  
  public String getPurchasedPlanType() {
      return purchasedPlanType;
  }
  
  public Integer getStartTime() {
      return startTime;
  }
  
  public Integer getEndTime() {
      return endTime;
  }
  
  public String getReferralId() {
      return referralId;
  }
  
  public String getReferrers() {
      return referrers;
  }
  
  public String getServiceProviderId() {
      return serviceProviderId;
  }
  
  public String getServiceProviderFcmToken() {
      return serviceProviderFCMToken;
  }
  
  public String getConsumerId() {
      return consumerId;
  }
  
  public String getConsumerFcmToken() {
      return consumerFCMToken;
  }
  
  public String getStatus() {
      return status;
  }
  
  public String getMeetingType() {
      return meetingType;
  }
  
  public String getMeetingId() {
      return meetingId;
  }
  
  private Reservation(String id, String offerId, String timeSlotId, String purchasedPlanId, String purchasedPlanType, Integer startTime, Integer endTime, String referralId, String referrers, String serviceProviderId, String serviceProviderFCMToken, String consumerId, String consumerFCMToken, String status, String meetingType, String meetingId) {
    this.id = id;
    this.offerId = offerId;
    this.timeSlotId = timeSlotId;
    this.purchasedPlanId = purchasedPlanId;
    this.purchasedPlanType = purchasedPlanType;
    this.startTime = startTime;
    this.endTime = endTime;
    this.referralId = referralId;
    this.referrers = referrers;
    this.serviceProviderId = serviceProviderId;
    this.serviceProviderFCMToken = serviceProviderFCMToken;
    this.consumerId = consumerId;
    this.consumerFCMToken = consumerFCMToken;
    this.status = status;
    this.meetingType = meetingType;
    this.meetingId = meetingId;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Reservation reservation = (Reservation) obj;
      return ObjectsCompat.equals(getId(), reservation.getId()) &&
              ObjectsCompat.equals(getOfferId(), reservation.getOfferId()) &&
              ObjectsCompat.equals(getTimeSlotId(), reservation.getTimeSlotId()) &&
              ObjectsCompat.equals(getPurchasedPlanId(), reservation.getPurchasedPlanId()) &&
              ObjectsCompat.equals(getPurchasedPlanType(), reservation.getPurchasedPlanType()) &&
              ObjectsCompat.equals(getStartTime(), reservation.getStartTime()) &&
              ObjectsCompat.equals(getEndTime(), reservation.getEndTime()) &&
              ObjectsCompat.equals(getReferralId(), reservation.getReferralId()) &&
              ObjectsCompat.equals(getReferrers(), reservation.getReferrers()) &&
              ObjectsCompat.equals(getServiceProviderId(), reservation.getServiceProviderId()) &&
              ObjectsCompat.equals(getServiceProviderFcmToken(), reservation.getServiceProviderFcmToken()) &&
              ObjectsCompat.equals(getConsumerId(), reservation.getConsumerId()) &&
              ObjectsCompat.equals(getConsumerFcmToken(), reservation.getConsumerFcmToken()) &&
              ObjectsCompat.equals(getStatus(), reservation.getStatus()) &&
              ObjectsCompat.equals(getMeetingType(), reservation.getMeetingType()) &&
              ObjectsCompat.equals(getMeetingId(), reservation.getMeetingId());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getOfferId())
      .append(getTimeSlotId())
      .append(getPurchasedPlanId())
      .append(getPurchasedPlanType())
      .append(getStartTime())
      .append(getEndTime())
      .append(getReferralId())
      .append(getReferrers())
      .append(getServiceProviderId())
      .append(getServiceProviderFcmToken())
      .append(getConsumerId())
      .append(getConsumerFcmToken())
      .append(getStatus())
      .append(getMeetingType())
      .append(getMeetingId())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Reservation {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("offerId=" + String.valueOf(getOfferId()) + ", ")
      .append("timeSlotId=" + String.valueOf(getTimeSlotId()) + ", ")
      .append("purchasedPlanId=" + String.valueOf(getPurchasedPlanId()) + ", ")
      .append("purchasedPlanType=" + String.valueOf(getPurchasedPlanType()) + ", ")
      .append("startTime=" + String.valueOf(getStartTime()) + ", ")
      .append("endTime=" + String.valueOf(getEndTime()) + ", ")
      .append("referralId=" + String.valueOf(getReferralId()) + ", ")
      .append("referrers=" + String.valueOf(getReferrers()) + ", ")
      .append("serviceProviderId=" + String.valueOf(getServiceProviderId()) + ", ")
      .append("serviceProviderFCMToken=" + String.valueOf(getServiceProviderFcmToken()) + ", ")
      .append("consumerId=" + String.valueOf(getConsumerId()) + ", ")
      .append("consumerFCMToken=" + String.valueOf(getConsumerFcmToken()) + ", ")
      .append("status=" + String.valueOf(getStatus()) + ", ")
      .append("meetingType=" + String.valueOf(getMeetingType()) + ", ")
      .append("meetingId=" + String.valueOf(getMeetingId()))
      .append("}")
      .toString();
  }
  
  public static BuildStep builder() {
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
  public static Reservation justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Reservation(
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
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      offerId,
      timeSlotId,
      purchasedPlanId,
      purchasedPlanType,
      startTime,
      endTime,
      referralId,
      referrers,
      serviceProviderId,
      serviceProviderFCMToken,
      consumerId,
      consumerFCMToken,
      status,
      meetingType,
      meetingId);
  }
  public interface BuildStep {
    Reservation build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep offerId(String offerId);
    BuildStep timeSlotId(String timeSlotId);
    BuildStep purchasedPlanId(String purchasedPlanId);
    BuildStep purchasedPlanType(String purchasedPlanType);
    BuildStep startTime(Integer startTime);
    BuildStep endTime(Integer endTime);
    BuildStep referralId(String referralId);
    BuildStep referrers(String referrers);
    BuildStep serviceProviderId(String serviceProviderId);
    BuildStep serviceProviderFcmToken(String serviceProviderFcmToken);
    BuildStep consumerId(String consumerId);
    BuildStep consumerFcmToken(String consumerFcmToken);
    BuildStep status(String status);
    BuildStep meetingType(String meetingType);
    BuildStep meetingId(String meetingId);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private String offerId;
    private String timeSlotId;
    private String purchasedPlanId;
    private String purchasedPlanType;
    private Integer startTime;
    private Integer endTime;
    private String referralId;
    private String referrers;
    private String serviceProviderId;
    private String serviceProviderFCMToken;
    private String consumerId;
    private String consumerFCMToken;
    private String status;
    private String meetingType;
    private String meetingId;
    @Override
     public Reservation build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Reservation(
          id,
          offerId,
          timeSlotId,
          purchasedPlanId,
          purchasedPlanType,
          startTime,
          endTime,
          referralId,
          referrers,
          serviceProviderId,
          serviceProviderFCMToken,
          consumerId,
          consumerFCMToken,
          status,
          meetingType,
          meetingId);
    }
    
    @Override
     public BuildStep offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }
    
    @Override
     public BuildStep timeSlotId(String timeSlotId) {
        this.timeSlotId = timeSlotId;
        return this;
    }
    
    @Override
     public BuildStep purchasedPlanId(String purchasedPlanId) {
        this.purchasedPlanId = purchasedPlanId;
        return this;
    }
    
    @Override
     public BuildStep purchasedPlanType(String purchasedPlanType) {
        this.purchasedPlanType = purchasedPlanType;
        return this;
    }
    
    @Override
     public BuildStep startTime(Integer startTime) {
        this.startTime = startTime;
        return this;
    }
    
    @Override
     public BuildStep endTime(Integer endTime) {
        this.endTime = endTime;
        return this;
    }
    
    @Override
     public BuildStep referralId(String referralId) {
        this.referralId = referralId;
        return this;
    }
    
    @Override
     public BuildStep referrers(String referrers) {
        this.referrers = referrers;
        return this;
    }
    
    @Override
     public BuildStep serviceProviderId(String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
        return this;
    }
    
    @Override
     public BuildStep serviceProviderFcmToken(String serviceProviderFcmToken) {
        this.serviceProviderFCMToken = serviceProviderFcmToken;
        return this;
    }
    
    @Override
     public BuildStep consumerId(String consumerId) {
        this.consumerId = consumerId;
        return this;
    }
    
    @Override
     public BuildStep consumerFcmToken(String consumerFcmToken) {
        this.consumerFCMToken = consumerFcmToken;
        return this;
    }
    
    @Override
     public BuildStep status(String status) {
        this.status = status;
        return this;
    }
    
    @Override
     public BuildStep meetingType(String meetingType) {
        this.meetingType = meetingType;
        return this;
    }
    
    @Override
     public BuildStep meetingId(String meetingId) {
        this.meetingId = meetingId;
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
    private CopyOfBuilder(String id, String offerId, String timeSlotId, String purchasedPlanId, String purchasedPlanType, Integer startTime, Integer endTime, String referralId, String referrers, String serviceProviderId, String serviceProviderFcmToken, String consumerId, String consumerFcmToken, String status, String meetingType, String meetingId) {
      super.id(id);
      super.offerId(offerId)
        .timeSlotId(timeSlotId)
        .purchasedPlanId(purchasedPlanId)
        .purchasedPlanType(purchasedPlanType)
        .startTime(startTime)
        .endTime(endTime)
        .referralId(referralId)
        .referrers(referrers)
        .serviceProviderId(serviceProviderId)
        .serviceProviderFcmToken(serviceProviderFcmToken)
        .consumerId(consumerId)
        .consumerFcmToken(consumerFcmToken)
        .status(status)
        .meetingType(meetingType)
        .meetingId(meetingId);
    }
    
    @Override
     public CopyOfBuilder offerId(String offerId) {
      return (CopyOfBuilder) super.offerId(offerId);
    }
    
    @Override
     public CopyOfBuilder timeSlotId(String timeSlotId) {
      return (CopyOfBuilder) super.timeSlotId(timeSlotId);
    }
    
    @Override
     public CopyOfBuilder purchasedPlanId(String purchasedPlanId) {
      return (CopyOfBuilder) super.purchasedPlanId(purchasedPlanId);
    }
    
    @Override
     public CopyOfBuilder purchasedPlanType(String purchasedPlanType) {
      return (CopyOfBuilder) super.purchasedPlanType(purchasedPlanType);
    }
    
    @Override
     public CopyOfBuilder startTime(Integer startTime) {
      return (CopyOfBuilder) super.startTime(startTime);
    }
    
    @Override
     public CopyOfBuilder endTime(Integer endTime) {
      return (CopyOfBuilder) super.endTime(endTime);
    }
    
    @Override
     public CopyOfBuilder referralId(String referralId) {
      return (CopyOfBuilder) super.referralId(referralId);
    }
    
    @Override
     public CopyOfBuilder referrers(String referrers) {
      return (CopyOfBuilder) super.referrers(referrers);
    }
    
    @Override
     public CopyOfBuilder serviceProviderId(String serviceProviderId) {
      return (CopyOfBuilder) super.serviceProviderId(serviceProviderId);
    }
    
    @Override
     public CopyOfBuilder serviceProviderFcmToken(String serviceProviderFcmToken) {
      return (CopyOfBuilder) super.serviceProviderFcmToken(serviceProviderFcmToken);
    }
    
    @Override
     public CopyOfBuilder consumerId(String consumerId) {
      return (CopyOfBuilder) super.consumerId(consumerId);
    }
    
    @Override
     public CopyOfBuilder consumerFcmToken(String consumerFcmToken) {
      return (CopyOfBuilder) super.consumerFcmToken(consumerFcmToken);
    }
    
    @Override
     public CopyOfBuilder status(String status) {
      return (CopyOfBuilder) super.status(status);
    }
    
    @Override
     public CopyOfBuilder meetingType(String meetingType) {
      return (CopyOfBuilder) super.meetingType(meetingType);
    }
    
    @Override
     public CopyOfBuilder meetingId(String meetingId) {
      return (CopyOfBuilder) super.meetingId(meetingId);
    }
  }
  
}
