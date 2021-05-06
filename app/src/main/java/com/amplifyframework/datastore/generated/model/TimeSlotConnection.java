package com.amplifyframework.datastore.generated.model;


import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the TimeSlotConnection type in your schema. */
public final class TimeSlotConnection {
  private final List<TimeSlot> items;
  private final String nextToken;
  public List<TimeSlot> getItems() {
      return items;
  }
  
  public String getNextToken() {
      return nextToken;
  }
  
  private TimeSlotConnection(List<TimeSlot> items, String nextToken) {
    this.items = items;
    this.nextToken = nextToken;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      TimeSlotConnection timeSlotConnection = (TimeSlotConnection) obj;
      return ObjectsCompat.equals(getItems(), timeSlotConnection.getItems()) &&
              ObjectsCompat.equals(getNextToken(), timeSlotConnection.getNextToken());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getItems())
      .append(getNextToken())
      .toString()
      .hashCode();
  }
  
  public static BuildStep builder() {
      return new Builder();
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(items,
      nextToken);
  }
  public interface BuildStep {
    TimeSlotConnection build();
    BuildStep items(List<TimeSlot> items);
    BuildStep nextToken(String nextToken);
  }
  

  public static class Builder implements BuildStep {
    private List<TimeSlot> items;
    private String nextToken;
    @Override
     public TimeSlotConnection build() {
        
        return new TimeSlotConnection(
          items,
          nextToken);
    }
    
    @Override
     public BuildStep items(List<TimeSlot> items) {
        this.items = items;
        return this;
    }
    
    @Override
     public BuildStep nextToken(String nextToken) {
        this.nextToken = nextToken;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(List<TimeSlot> items, String nextToken) {
      super.items(items)
        .nextToken(nextToken);
    }
    
    @Override
     public CopyOfBuilder items(List<TimeSlot> items) {
      return (CopyOfBuilder) super.items(items);
    }
    
    @Override
     public CopyOfBuilder nextToken(String nextToken) {
      return (CopyOfBuilder) super.nextToken(nextToken);
    }
  }
  
}
