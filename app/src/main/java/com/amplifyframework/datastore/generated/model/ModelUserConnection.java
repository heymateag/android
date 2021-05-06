package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the ModelUserConnection type in your schema. */
public final class ModelUserConnection {
  private final List<User> items;
  private final String nextToken;
  private final Temporal.Timestamp startedAt;
  public List<User> getItems() {
      return items;
  }
  
  public String getNextToken() {
      return nextToken;
  }
  
  public Temporal.Timestamp getStartedAt() {
      return startedAt;
  }
  
  private ModelUserConnection(List<User> items, String nextToken, Temporal.Timestamp startedAt) {
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
      ModelUserConnection modelUserConnection = (ModelUserConnection) obj;
      return ObjectsCompat.equals(getItems(), modelUserConnection.getItems()) &&
              ObjectsCompat.equals(getNextToken(), modelUserConnection.getNextToken()) &&
              ObjectsCompat.equals(getStartedAt(), modelUserConnection.getStartedAt());
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
    ModelUserConnection build();
    BuildStep items(List<User> items);
    BuildStep nextToken(String nextToken);
    BuildStep startedAt(Temporal.Timestamp startedAt);
  }
  

  public static class Builder implements BuildStep {
    private List<User> items;
    private String nextToken;
    private Temporal.Timestamp startedAt;
    @Override
     public ModelUserConnection build() {
        
        return new ModelUserConnection(
          items,
          nextToken,
          startedAt);
    }
    
    @Override
     public BuildStep items(List<User> items) {
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
    private CopyOfBuilder(List<User> items, String nextToken, Temporal.Timestamp startedAt) {
      super.items(items)
        .nextToken(nextToken)
        .startedAt(startedAt);
    }
    
    @Override
     public CopyOfBuilder items(List<User> items) {
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
