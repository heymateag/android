package org.telegram.ui.Heymate.AmplifyModels;


import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the OfferCalendarConnection type in your schema. */
public final class OfferCalendarConnection {
  private final List<OfferCalendar> items;
  private final String nextToken;
  public List<OfferCalendar> getItems() {
      return items;
  }
  
  public String getNextToken() {
      return nextToken;
  }
  
  private OfferCalendarConnection(List<OfferCalendar> items, String nextToken) {
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
      OfferCalendarConnection offerCalendarConnection = (OfferCalendarConnection) obj;
      return ObjectsCompat.equals(getItems(), offerCalendarConnection.getItems()) &&
              ObjectsCompat.equals(getNextToken(), offerCalendarConnection.getNextToken());
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
    OfferCalendarConnection build();
    BuildStep items(List<OfferCalendar> items);
    BuildStep nextToken(String nextToken);
  }
  

  public static class Builder implements BuildStep {
    private List<OfferCalendar> items;
    private String nextToken;
    @Override
     public OfferCalendarConnection build() {
        
        return new OfferCalendarConnection(
          items,
          nextToken);
    }
    
    @Override
     public BuildStep items(List<OfferCalendar> items) {
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
    private CopyOfBuilder(List<OfferCalendar> items, String nextToken) {
      super.items(items)
        .nextToken(nextToken);
    }
    
    @Override
     public CopyOfBuilder items(List<OfferCalendar> items) {
      return (CopyOfBuilder) super.items(items);
    }
    
    @Override
     public CopyOfBuilder nextToken(String nextToken) {
      return (CopyOfBuilder) super.nextToken(nextToken);
    }
  }
  
}
