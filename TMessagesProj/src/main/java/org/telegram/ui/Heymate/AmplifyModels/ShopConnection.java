package org.telegram.ui.Heymate.AmplifyModels;


import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the ShopConnection type in your schema. */
public final class ShopConnection {
  private final List<Shop> items;
  private final String nextToken;
  public List<Shop> getItems() {
      return items;
  }
  
  public String getNextToken() {
      return nextToken;
  }
  
  private ShopConnection(List<Shop> items, String nextToken) {
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
      ShopConnection shopConnection = (ShopConnection) obj;
      return ObjectsCompat.equals(getItems(), shopConnection.getItems()) &&
              ObjectsCompat.equals(getNextToken(), shopConnection.getNextToken());
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
    ShopConnection build();
    BuildStep items(List<Shop> items);
    BuildStep nextToken(String nextToken);
  }
  

  public static class Builder implements BuildStep {
    private List<Shop> items;
    private String nextToken;
    @Override
     public ShopConnection build() {
        
        return new ShopConnection(
          items,
          nextToken);
    }
    
    @Override
     public BuildStep items(List<Shop> items) {
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
    private CopyOfBuilder(List<Shop> items, String nextToken) {
      super.items(items)
        .nextToken(nextToken);
    }
    
    @Override
     public CopyOfBuilder items(List<Shop> items) {
      return (CopyOfBuilder) super.items(items);
    }
    
    @Override
     public CopyOfBuilder nextToken(String nextToken) {
      return (CopyOfBuilder) super.nextToken(nextToken);
    }
  }
  
}
