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

/** This is an auto generated class representing the PurchasedPlan type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "PurchasedPlans", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class PurchasedPlan implements Model {
  public static final QueryField ID = field("PurchasedPlan", "id");
  public static final QueryField OFFER_ID = field("PurchasedPlan", "offerId");
  public static final QueryField SERVICE_PROVIDER_ID = field("PurchasedPlan", "serviceProviderId");
  public static final QueryField CONSUMER_ID = field("PurchasedPlan", "consumerId");
  public static final QueryField PLAN_TYPE = field("PurchasedPlan", "planType");
  public static final QueryField TOTAL_RESERVATIONS_COUNT = field("PurchasedPlan", "totalReservationsCount");
  public static final QueryField FINISHED_RESERVATIONS_COUNT = field("PurchasedPlan", "finishedReservationsCount");
  public static final QueryField PENDING_RESERVATIONS_COUNT = field("PurchasedPlan", "pendingReservationsCount");
  public static final QueryField RESERVATION_IDS = field("PurchasedPlan", "reservationIds");
  public static final QueryField PURCHASE_TIME = field("PurchasedPlan", "purchaseTime");
  public static final QueryField RENEWAL_PERIOD = field("PurchasedPlan", "renewalPeriod");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String offerId;
  private final @ModelField(targetType="String") String serviceProviderId;
  private final @ModelField(targetType="String") String consumerId;
  private final @ModelField(targetType="String") String planType;
  private final @ModelField(targetType="Int") Integer totalReservationsCount;
  private final @ModelField(targetType="Int") Integer finishedReservationsCount;
  private final @ModelField(targetType="Int") Integer pendingReservationsCount;
  private final @ModelField(targetType="AWSJSON") String reservationIds;
  private final @ModelField(targetType="AWSDate") Temporal.Date purchaseTime;
  private final @ModelField(targetType="Int") Integer renewalPeriod;
  public String getId() {
      return id;
  }
  
  public String getOfferId() {
      return offerId;
  }
  
  public String getServiceProviderId() {
      return serviceProviderId;
  }
  
  public String getConsumerId() {
      return consumerId;
  }
  
  public String getPlanType() {
      return planType;
  }
  
  public Integer getTotalReservationsCount() {
      return totalReservationsCount;
  }
  
  public Integer getFinishedReservationsCount() {
      return finishedReservationsCount;
  }
  
  public Integer getPendingReservationsCount() {
      return pendingReservationsCount;
  }
  
  public String getReservationIds() {
      return reservationIds;
  }
  
  public Temporal.Date getPurchaseTime() {
      return purchaseTime;
  }
  
  public Integer getRenewalPeriod() {
      return renewalPeriod;
  }
  
  private PurchasedPlan(String id, String offerId, String serviceProviderId, String consumerId, String planType, Integer totalReservationsCount, Integer finishedReservationsCount, Integer pendingReservationsCount, String reservationIds, Temporal.Date purchaseTime, Integer renewalPeriod) {
    this.id = id;
    this.offerId = offerId;
    this.serviceProviderId = serviceProviderId;
    this.consumerId = consumerId;
    this.planType = planType;
    this.totalReservationsCount = totalReservationsCount;
    this.finishedReservationsCount = finishedReservationsCount;
    this.pendingReservationsCount = pendingReservationsCount;
    this.reservationIds = reservationIds;
    this.purchaseTime = purchaseTime;
    this.renewalPeriod = renewalPeriod;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      PurchasedPlan purchasedPlan = (PurchasedPlan) obj;
      return ObjectsCompat.equals(getId(), purchasedPlan.getId()) &&
              ObjectsCompat.equals(getOfferId(), purchasedPlan.getOfferId()) &&
              ObjectsCompat.equals(getServiceProviderId(), purchasedPlan.getServiceProviderId()) &&
              ObjectsCompat.equals(getConsumerId(), purchasedPlan.getConsumerId()) &&
              ObjectsCompat.equals(getPlanType(), purchasedPlan.getPlanType()) &&
              ObjectsCompat.equals(getTotalReservationsCount(), purchasedPlan.getTotalReservationsCount()) &&
              ObjectsCompat.equals(getFinishedReservationsCount(), purchasedPlan.getFinishedReservationsCount()) &&
              ObjectsCompat.equals(getPendingReservationsCount(), purchasedPlan.getPendingReservationsCount()) &&
              ObjectsCompat.equals(getReservationIds(), purchasedPlan.getReservationIds()) &&
              ObjectsCompat.equals(getPurchaseTime(), purchasedPlan.getPurchaseTime()) &&
              ObjectsCompat.equals(getRenewalPeriod(), purchasedPlan.getRenewalPeriod());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getOfferId())
      .append(getServiceProviderId())
      .append(getConsumerId())
      .append(getPlanType())
      .append(getTotalReservationsCount())
      .append(getFinishedReservationsCount())
      .append(getPendingReservationsCount())
      .append(getReservationIds())
      .append(getPurchaseTime())
      .append(getRenewalPeriod())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("PurchasedPlan {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("offerId=" + String.valueOf(getOfferId()) + ", ")
      .append("serviceProviderId=" + String.valueOf(getServiceProviderId()) + ", ")
      .append("consumerId=" + String.valueOf(getConsumerId()) + ", ")
      .append("planType=" + String.valueOf(getPlanType()) + ", ")
      .append("totalReservationsCount=" + String.valueOf(getTotalReservationsCount()) + ", ")
      .append("finishedReservationsCount=" + String.valueOf(getFinishedReservationsCount()) + ", ")
      .append("pendingReservationsCount=" + String.valueOf(getPendingReservationsCount()) + ", ")
      .append("reservationIds=" + String.valueOf(getReservationIds()) + ", ")
      .append("purchaseTime=" + String.valueOf(getPurchaseTime()) + ", ")
      .append("renewalPeriod=" + String.valueOf(getRenewalPeriod()))
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
  public static PurchasedPlan justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new PurchasedPlan(
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
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      offerId,
      serviceProviderId,
      consumerId,
      planType,
      totalReservationsCount,
      finishedReservationsCount,
      pendingReservationsCount,
      reservationIds,
      purchaseTime,
      renewalPeriod);
  }
  public interface BuildStep {
    PurchasedPlan build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep offerId(String offerId);
    BuildStep serviceProviderId(String serviceProviderId);
    BuildStep consumerId(String consumerId);
    BuildStep planType(String planType);
    BuildStep totalReservationsCount(Integer totalReservationsCount);
    BuildStep finishedReservationsCount(Integer finishedReservationsCount);
    BuildStep pendingReservationsCount(Integer pendingReservationsCount);
    BuildStep reservationIds(String reservationIds);
    BuildStep purchaseTime(Temporal.Date purchaseTime);
    BuildStep renewalPeriod(Integer renewalPeriod);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private String offerId;
    private String serviceProviderId;
    private String consumerId;
    private String planType;
    private Integer totalReservationsCount;
    private Integer finishedReservationsCount;
    private Integer pendingReservationsCount;
    private String reservationIds;
    private Temporal.Date purchaseTime;
    private Integer renewalPeriod;
    @Override
     public PurchasedPlan build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new PurchasedPlan(
          id,
          offerId,
          serviceProviderId,
          consumerId,
          planType,
          totalReservationsCount,
          finishedReservationsCount,
          pendingReservationsCount,
          reservationIds,
          purchaseTime,
          renewalPeriod);
    }
    
    @Override
     public BuildStep offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }
    
    @Override
     public BuildStep serviceProviderId(String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
        return this;
    }
    
    @Override
     public BuildStep consumerId(String consumerId) {
        this.consumerId = consumerId;
        return this;
    }
    
    @Override
     public BuildStep planType(String planType) {
        this.planType = planType;
        return this;
    }
    
    @Override
     public BuildStep totalReservationsCount(Integer totalReservationsCount) {
        this.totalReservationsCount = totalReservationsCount;
        return this;
    }
    
    @Override
     public BuildStep finishedReservationsCount(Integer finishedReservationsCount) {
        this.finishedReservationsCount = finishedReservationsCount;
        return this;
    }
    
    @Override
     public BuildStep pendingReservationsCount(Integer pendingReservationsCount) {
        this.pendingReservationsCount = pendingReservationsCount;
        return this;
    }
    
    @Override
     public BuildStep reservationIds(String reservationIds) {
        this.reservationIds = reservationIds;
        return this;
    }
    
    @Override
     public BuildStep purchaseTime(Temporal.Date purchaseTime) {
        this.purchaseTime = purchaseTime;
        return this;
    }
    
    @Override
     public BuildStep renewalPeriod(Integer renewalPeriod) {
        this.renewalPeriod = renewalPeriod;
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
    private CopyOfBuilder(String id, String offerId, String serviceProviderId, String consumerId, String planType, Integer totalReservationsCount, Integer finishedReservationsCount, Integer pendingReservationsCount, String reservationIds, Temporal.Date purchaseTime, Integer renewalPeriod) {
      super.id(id);
      super.offerId(offerId)
        .serviceProviderId(serviceProviderId)
        .consumerId(consumerId)
        .planType(planType)
        .totalReservationsCount(totalReservationsCount)
        .finishedReservationsCount(finishedReservationsCount)
        .pendingReservationsCount(pendingReservationsCount)
        .reservationIds(reservationIds)
        .purchaseTime(purchaseTime)
        .renewalPeriod(renewalPeriod);
    }
    
    @Override
     public CopyOfBuilder offerId(String offerId) {
      return (CopyOfBuilder) super.offerId(offerId);
    }
    
    @Override
     public CopyOfBuilder serviceProviderId(String serviceProviderId) {
      return (CopyOfBuilder) super.serviceProviderId(serviceProviderId);
    }
    
    @Override
     public CopyOfBuilder consumerId(String consumerId) {
      return (CopyOfBuilder) super.consumerId(consumerId);
    }
    
    @Override
     public CopyOfBuilder planType(String planType) {
      return (CopyOfBuilder) super.planType(planType);
    }
    
    @Override
     public CopyOfBuilder totalReservationsCount(Integer totalReservationsCount) {
      return (CopyOfBuilder) super.totalReservationsCount(totalReservationsCount);
    }
    
    @Override
     public CopyOfBuilder finishedReservationsCount(Integer finishedReservationsCount) {
      return (CopyOfBuilder) super.finishedReservationsCount(finishedReservationsCount);
    }
    
    @Override
     public CopyOfBuilder pendingReservationsCount(Integer pendingReservationsCount) {
      return (CopyOfBuilder) super.pendingReservationsCount(pendingReservationsCount);
    }
    
    @Override
     public CopyOfBuilder reservationIds(String reservationIds) {
      return (CopyOfBuilder) super.reservationIds(reservationIds);
    }
    
    @Override
     public CopyOfBuilder purchaseTime(Temporal.Date purchaseTime) {
      return (CopyOfBuilder) super.purchaseTime(purchaseTime);
    }
    
    @Override
     public CopyOfBuilder renewalPeriod(Integer renewalPeriod) {
      return (CopyOfBuilder) super.renewalPeriod(renewalPeriod);
    }
  }
  
}
