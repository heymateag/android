package com.amplifyframework.datastore.generated.model;


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

/** This is an auto generated class representing the User type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Users")
public final class User implements Model {
  public static final QueryField ID = field("User", "id");
  public static final QueryField OFFERS = field("User", "Offers");
  public static final QueryField NAME = field("User", "name");
  public static final QueryField PHONE_NO = field("User", "phoneNo");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="ModelOfferConnection") ModelOfferConnection Offers;
  private final @ModelField(targetType="String") String name;
  private final @ModelField(targetType="String") String phoneNo;
  public String getId() {
      return id;
  }
  
  public ModelOfferConnection getOffers() {
      return Offers;
  }
  
  public String getName() {
      return name;
  }
  
  public String getPhoneNo() {
      return phoneNo;
  }
  
  private User(String id, ModelOfferConnection Offers, String name, String phoneNo) {
    this.id = id;
    this.Offers = Offers;
    this.name = name;
    this.phoneNo = phoneNo;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      User user = (User) obj;
      return ObjectsCompat.equals(getId(), user.getId()) &&
              ObjectsCompat.equals(getOffers(), user.getOffers()) &&
              ObjectsCompat.equals(getName(), user.getName()) &&
              ObjectsCompat.equals(getPhoneNo(), user.getPhoneNo());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getOffers())
      .append(getName())
      .append(getPhoneNo())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("User {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("Offers=" + String.valueOf(getOffers()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("phoneNo=" + String.valueOf(getPhoneNo()))
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
  public static User justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new User(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      Offers,
      name,
      phoneNo);
  }
  public interface BuildStep {
    User build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep offers(ModelOfferConnection offers);
    BuildStep name(String name);
    BuildStep phoneNo(String phoneNo);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private ModelOfferConnection Offers;
    private String name;
    private String phoneNo;
    @Override
     public User build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new User(
          id,
          Offers,
          name,
          phoneNo);
    }
    
    @Override
     public BuildStep offers(ModelOfferConnection offers) {
        this.Offers = offers;
        return this;
    }
    
    @Override
     public BuildStep name(String name) {
        this.name = name;
        return this;
    }
    
    @Override
     public BuildStep phoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
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
    private CopyOfBuilder(String id, ModelOfferConnection offers, String name, String phoneNo) {
      super.id(id);
      super.offers(offers)
        .name(name)
        .phoneNo(phoneNo);
    }
    
    @Override
     public CopyOfBuilder offers(ModelOfferConnection offers) {
      return (CopyOfBuilder) super.offers(offers);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder phoneNo(String phoneNo) {
      return (CopyOfBuilder) super.phoneNo(phoneNo);
    }
  }
  
}