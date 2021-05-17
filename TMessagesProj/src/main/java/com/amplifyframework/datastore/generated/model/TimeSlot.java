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

/** This is an auto generated class representing the TimeSlot type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "TimeSlots", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class TimeSlot implements Model {
  public static final QueryField ID = field("TimeSlot", "id");
  public static final QueryField OFFER_ID = field("TimeSlot", "offerId");
  public static final QueryField USER_ID = field("TimeSlot", "userId");
  public static final QueryField START_TIME = field("TimeSlot", "startTime");
  public static final QueryField END_TIME = field("TimeSlot", "endTime");
  public static final QueryField USER_FCM_TOKEN = field("TimeSlot", "userFCMToken");
  public static final QueryField MAXIMUM_RESERVATIONS = field("TimeSlot", "maximumReservations");
  public static final QueryField COMPLETED_RESERVATIONS = field("TimeSlot", "completedReservations");
  public static final QueryField REMAINING_RESERVATIONS = field("TimeSlot", "remainingReservations");
  public static final QueryField MEETING_TYPE = field("TimeSlot", "meetingType");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String offerId;
  private final @ModelField(targetType="String") String userId;
  private final @ModelField(targetType="Int") Integer startTime;
  private final @ModelField(targetType="Int") Integer endTime;
  private final @ModelField(targetType="String") String userFCMToken;
  private final @ModelField(targetType="Int") Integer maximumReservations;
  private final @ModelField(targetType="Int") Integer completedReservations;
  private final @ModelField(targetType="Int") Integer remainingReservations;
  private final @ModelField(targetType="String") String meetingType;
  public String getId() {
      return id;
  }
  
  public String getOfferId() {
      return offerId;
  }
  
  public String getUserId() {
      return userId;
  }
  
  public Integer getStartTime() {
      return startTime;
  }
  
  public Integer getEndTime() {
      return endTime;
  }
  
  public String getUserFcmToken() {
      return userFCMToken;
  }
  
  public Integer getMaximumReservations() {
      return maximumReservations;
  }
  
  public Integer getCompletedReservations() {
      return completedReservations;
  }
  
  public Integer getRemainingReservations() {
      return remainingReservations;
  }
  
  public String getMeetingType() {
      return meetingType;
  }
  
  private TimeSlot(String id, String offerId, String userId, Integer startTime, Integer endTime, String userFCMToken, Integer maximumReservations, Integer completedReservations, Integer remainingReservations, String meetingType) {
    this.id = id;
    this.offerId = offerId;
    this.userId = userId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.userFCMToken = userFCMToken;
    this.maximumReservations = maximumReservations;
    this.completedReservations = completedReservations;
    this.remainingReservations = remainingReservations;
    this.meetingType = meetingType;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      TimeSlot timeSlot = (TimeSlot) obj;
      return ObjectsCompat.equals(getId(), timeSlot.getId()) &&
              ObjectsCompat.equals(getOfferId(), timeSlot.getOfferId()) &&
              ObjectsCompat.equals(getUserId(), timeSlot.getUserId()) &&
              ObjectsCompat.equals(getStartTime(), timeSlot.getStartTime()) &&
              ObjectsCompat.equals(getEndTime(), timeSlot.getEndTime()) &&
              ObjectsCompat.equals(getUserFcmToken(), timeSlot.getUserFcmToken()) &&
              ObjectsCompat.equals(getMaximumReservations(), timeSlot.getMaximumReservations()) &&
              ObjectsCompat.equals(getCompletedReservations(), timeSlot.getCompletedReservations()) &&
              ObjectsCompat.equals(getRemainingReservations(), timeSlot.getRemainingReservations()) &&
              ObjectsCompat.equals(getMeetingType(), timeSlot.getMeetingType());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getOfferId())
      .append(getUserId())
      .append(getStartTime())
      .append(getEndTime())
      .append(getUserFcmToken())
      .append(getMaximumReservations())
      .append(getCompletedReservations())
      .append(getRemainingReservations())
      .append(getMeetingType())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("TimeSlot {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("offerId=" + String.valueOf(getOfferId()) + ", ")
      .append("userId=" + String.valueOf(getUserId()) + ", ")
      .append("startTime=" + String.valueOf(getStartTime()) + ", ")
      .append("endTime=" + String.valueOf(getEndTime()) + ", ")
      .append("userFCMToken=" + String.valueOf(getUserFcmToken()) + ", ")
      .append("maximumReservations=" + String.valueOf(getMaximumReservations()) + ", ")
      .append("completedReservations=" + String.valueOf(getCompletedReservations()) + ", ")
      .append("remainingReservations=" + String.valueOf(getRemainingReservations()) + ", ")
      .append("meetingType=" + String.valueOf(getMeetingType()))
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
  public static TimeSlot justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new TimeSlot(
      id,
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
      userId,
      startTime,
      endTime,
      userFCMToken,
      maximumReservations,
      completedReservations,
      remainingReservations,
      meetingType);
  }
  public interface BuildStep {
    TimeSlot build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep offerId(String offerId);
    BuildStep userId(String userId);
    BuildStep startTime(Integer startTime);
    BuildStep endTime(Integer endTime);
    BuildStep userFcmToken(String userFcmToken);
    BuildStep maximumReservations(Integer maximumReservations);
    BuildStep completedReservations(Integer completedReservations);
    BuildStep remainingReservations(Integer remainingReservations);
    BuildStep meetingType(String meetingType);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private String offerId;
    private String userId;
    private Integer startTime;
    private Integer endTime;
    private String userFCMToken;
    private Integer maximumReservations;
    private Integer completedReservations;
    private Integer remainingReservations;
    private String meetingType;
    @Override
     public TimeSlot build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new TimeSlot(
          id,
          offerId,
          userId,
          startTime,
          endTime,
          userFCMToken,
          maximumReservations,
          completedReservations,
          remainingReservations,
          meetingType);
    }
    
    @Override
     public BuildStep offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }
    
    @Override
     public BuildStep userId(String userId) {
        this.userId = userId;
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
     public BuildStep userFcmToken(String userFcmToken) {
        this.userFCMToken = userFcmToken;
        return this;
    }
    
    @Override
     public BuildStep maximumReservations(Integer maximumReservations) {
        this.maximumReservations = maximumReservations;
        return this;
    }
    
    @Override
     public BuildStep completedReservations(Integer completedReservations) {
        this.completedReservations = completedReservations;
        return this;
    }
    
    @Override
     public BuildStep remainingReservations(Integer remainingReservations) {
        this.remainingReservations = remainingReservations;
        return this;
    }
    
    @Override
     public BuildStep meetingType(String meetingType) {
        this.meetingType = meetingType;
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
    private CopyOfBuilder(String id, String offerId, String userId, Integer startTime, Integer endTime, String userFcmToken, Integer maximumReservations, Integer completedReservations, Integer remainingReservations, String meetingType) {
      super.id(id);
      super.offerId(offerId)
        .userId(userId)
        .startTime(startTime)
        .endTime(endTime)
        .userFcmToken(userFcmToken)
        .maximumReservations(maximumReservations)
        .completedReservations(completedReservations)
        .remainingReservations(remainingReservations)
        .meetingType(meetingType);
    }
    
    @Override
     public CopyOfBuilder offerId(String offerId) {
      return (CopyOfBuilder) super.offerId(offerId);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
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
     public CopyOfBuilder userFcmToken(String userFcmToken) {
      return (CopyOfBuilder) super.userFcmToken(userFcmToken);
    }
    
    @Override
     public CopyOfBuilder maximumReservations(Integer maximumReservations) {
      return (CopyOfBuilder) super.maximumReservations(maximumReservations);
    }
    
    @Override
     public CopyOfBuilder completedReservations(Integer completedReservations) {
      return (CopyOfBuilder) super.completedReservations(completedReservations);
    }
    
    @Override
     public CopyOfBuilder remainingReservations(Integer remainingReservations) {
      return (CopyOfBuilder) super.remainingReservations(remainingReservations);
    }
    
    @Override
     public CopyOfBuilder meetingType(String meetingType) {
      return (CopyOfBuilder) super.meetingType(meetingType);
    }
  }
  
}
