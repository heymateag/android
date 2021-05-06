package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the ModelOfferConnection type in your schema. */
public final class ModelOfferConnection {
  private final List<Offer> items;
  private final String nextToken;
  private final Temporal.Timestamp startedAt;
  public List<Offer> getItems() {
      return items;
  }
  
  public String getNextToken() {
      return nextToken;
  }
  
  public Temporal.Timestamp getStartedAt() {
      return startedAt;
  }
  
  private ModelOfferConnection(List<Offer> items, String nextToken, Temporal.Timestamp startedAt) {
    this.items = items;
    this.nextToken = nextToken;
    this.startedAt = startedAt;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      ModelOfferConnection modelOfferConnection = (ModelOfferConnection) obj;
      return ObjectsCompat.equals(getItems(), modelOfferConnection.getItems()) &&
              ObjectsCompat.equals(getNextToken(), modelOfferConnection.getNextToken()) &&
              ObjectsCompat.equals(getStartedAt(), modelOfferConnection.getStartedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getItems())
      .append(getNextToken())
      .append(getStartedAt())
      .toString()
      .hashCode();
  }
  
  public static BuildStep builder() {
      return new Builder();
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(items,
      nextToken,
      startedAt);
  }
  public interface BuildStep {
    ModelOfferConnection build();
    BuildStep items(List<Offer> items);
    BuildStep nextToken(String nextToken);
    BuildStep startedAt(Temporal.Timestamp startedAt);
  }
  

  public static class Builder implements BuildStep {
    private List<Offer> items;
    private String nextToken;
    private Temporal.Timestamp startedAt;
    @Override
     public ModelOfferConnection build() {
        
        return new ModelOfferConnection(
          items,
          nextToken,
          startedAt);
    }
    
    @Override
     public BuildStep items(List<Offer> items) {
        this.items = items;
        return this;
    }
    
    @Override
     public BuildStep nextToken(String nextToken) {
        this.nextToken = nextToken;
        return this;
    }
    
    @Override
     public BuildStep startedAt(Temporal.Timestamp startedAt) {
        this.startedAt = startedAt;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(List<Offer> items, String nextToken, Temporal.Timestamp startedAt) {
      super.items(items)
        .nextToken(nextToken)
        .startedAt(startedAt);
    }
    
    @Override
     public CopyOfBuilder items(List<Offer> items) {
      return (CopyOfBuilder) super.items(items);
    }
    
    @Override
     public CopyOfBuilder nextToken(String nextToken) {
      return (CopyOfBuilder) super.nextToken(nextToken);
    }
    
    @Override
     public CopyOfBuilder startedAt(Temporal.Timestamp startedAt) {
      return (CopyOfBuilder) super.startedAt(startedAt);
    }
  }
  
}
