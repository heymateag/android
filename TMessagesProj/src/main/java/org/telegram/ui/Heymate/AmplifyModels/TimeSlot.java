package org.telegram.ui.Heymate.AmplifyModels;


import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the TimeSlot type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "TimeSlots")
public final class TimeSlot implements Model {
  public static final QueryField ID = field("TimeSlot", "id");
  public static final QueryField CLIENT_USER_ID = field("TimeSlot", "clientUserId");
  public static final QueryField END_TIME = field("TimeSlot", "endTime");
  public static final QueryField OFFER_ID = field("TimeSlot", "offerId");
  public static final QueryField START_TIME = field("TimeSlot", "startTime");
  public static final QueryField STATUS = field("TimeSlot", "status");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String clientUserId;
  private final @ModelField(targetType="Int") Long endTime;
  private final @ModelField(targetType="String") String offerId;
  private final @ModelField(targetType="Int") Long startTime;
  private final @ModelField(targetType="Int") Integer status;
  public String getId() {
      return id;
  }
  
  public String getClientUserId() {
      return clientUserId;
  }
  
  public Long getEndTime() {
      return endTime;
  }
  
  public String getOfferId() {
      return offerId;
  }
  
  public Long getStartTime() {
      return startTime;
  }
  
  public Integer getStatus() {
      return status;
  }
  
  private TimeSlot(String id, String clientUserId, Long endTime, String offerId, Long startTime, Integer status) {
    this.id = id;
    this.clientUserId = clientUserId;
    this.endTime = endTime;
    this.offerId = offerId;
    this.startTime = startTime;
    this.status = status;
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
              ObjectsCompat.equals(getClientUserId(), timeSlot.getClientUserId()) &&
              ObjectsCompat.equals(getEndTime(), timeSlot.getEndTime()) &&
              ObjectsCompat.equals(getOfferId(), timeSlot.getOfferId()) &&
              ObjectsCompat.equals(getStartTime(), timeSlot.getStartTime()) &&
              ObjectsCompat.equals(getStatus(), timeSlot.getStatus());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getClientUserId())
      .append(getEndTime())
      .append(getOfferId())
      .append(getStartTime())
      .append(getStatus())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("TimeSlot {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("clientUserId=" + String.valueOf(getClientUserId()) + ", ")
      .append("endTime=" + String.valueOf(getEndTime()) + ", ")
      .append("offerId=" + String.valueOf(getOfferId()) + ", ")
      .append("startTime=" + String.valueOf(getStartTime()) + ", ")
      .append("status=" + String.valueOf(getStatus()))
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
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      clientUserId,
      endTime,
      offerId,
      startTime,
      status);
  }
  public interface BuildStep {
    TimeSlot build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep clientUserId(String clientUserId);
    BuildStep endTime(Long endTime);
    BuildStep offerId(String offerId);
    BuildStep startTime(Long startTime);
    BuildStep status(Integer status);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private String clientUserId;
    private Long endTime;
    private String offerId;
    private Long startTime;
    private Integer status;
    @Override
     public TimeSlot build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new TimeSlot(
          id,
          clientUserId,
          endTime,
          offerId,
          startTime,
          status);
    }
    
    @Override
     public BuildStep clientUserId(String clientUserId) {
        this.clientUserId = clientUserId;
        return this;
    }
    
    @Override
     public BuildStep endTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }
    
    @Override
     public BuildStep offerId(String offerId) {
        this.offerId = offerId;
        return this;
    }
    
    @Override
     public BuildStep startTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }
    
    @Override
     public BuildStep status(Integer status) {
        this.status = status;
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
    private CopyOfBuilder(String id, String clientUserId, Long endTime, String offerId, Long startTime, Integer status) {
      super.id(id);
      super.clientUserId(clientUserId)
        .endTime(endTime)
        .offerId(offerId)
        .startTime(startTime)
        .status(status);
    }
    
    @Override
     public CopyOfBuilder clientUserId(String clientUserId) {
      return (CopyOfBuilder) super.clientUserId(clientUserId);
    }
    
    @Override
     public CopyOfBuilder endTime(Long endTime) {
      return (CopyOfBuilder) super.endTime(endTime);
    }
    
    @Override
     public CopyOfBuilder offerId(String offerId) {
      return (CopyOfBuilder) super.offerId(offerId);
    }
    
    @Override
     public CopyOfBuilder startTime(Long startTime) {
      return (CopyOfBuilder) super.startTime(startTime);
    }
    
    @Override
     public CopyOfBuilder status(Integer status) {
      return (CopyOfBuilder) super.status(status);
    }
  }
  
}
