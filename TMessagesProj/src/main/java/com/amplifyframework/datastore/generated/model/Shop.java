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

/** This is an auto generated class representing the Shop type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Shops", authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class Shop implements Model {
  public static final QueryField ID = field("Shop", "id");
  public static final QueryField TG_ID = field("Shop", "tgId");
  public static final QueryField TITLE = field("Shop", "title");
  public static final QueryField TYPE = field("Shop", "type");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="Int", isRequired = true) Long tgId;
  private final @ModelField(targetType="String", isRequired = true) String title;
  private final @ModelField(targetType="Int", isRequired = true) Integer type;
  public String getId() {
      return id;
  }
  
  public Long getTgId() {
      return tgId;
  }
  
  public String getTitle() {
      return title;
  }
  
  public Integer getType() {
      return type;
  }
  
  private Shop(String id, Long tgId, String title, Integer type) {
    this.id = id;
    this.tgId = tgId;
    this.title = title;
    this.type = type;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Shop shop = (Shop) obj;
      return ObjectsCompat.equals(getId(), shop.getId()) &&
              ObjectsCompat.equals(getTgId(), shop.getTgId()) &&
              ObjectsCompat.equals(getTitle(), shop.getTitle()) &&
              ObjectsCompat.equals(getType(), shop.getType());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTgId())
      .append(getTitle())
      .append(getType())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Shop {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("tgId=" + String.valueOf(getTgId()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
      .append("type=" + String.valueOf(getType()))
      .append("}")
      .toString();
  }
  
  public static TgIdStep builder() {
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
  public static Shop justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Shop(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      tgId,
      title,
      type);
  }
  public interface TgIdStep {
    TitleStep tgId(Long tgId);
  }
  

  public interface TitleStep {
    TypeStep title(String title);
  }
  

  public interface TypeStep {
    BuildStep type(Integer type);
  }
  

  public interface BuildStep {
    Shop build();
    BuildStep id(String id) throws IllegalArgumentException;
  }
  

  public static class Builder implements TgIdStep, TitleStep, TypeStep, BuildStep {
    private String id;
    private Long tgId;
    private String title;
    private Integer type;
    @Override
     public Shop build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Shop(
          id,
          tgId,
          title,
          type);
    }
    
    @Override
     public TitleStep tgId(Long tgId) {
        Objects.requireNonNull(tgId);
        this.tgId = tgId;
        return this;
    }
    
    @Override
     public TypeStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep type(Integer type) {
        Objects.requireNonNull(type);
        this.type = type;
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
    private CopyOfBuilder(String id, Long tgId, String title, Integer type) {
      super.id(id);
      super.tgId(tgId)
        .title(title)
        .type(type);
    }
    
    @Override
     public CopyOfBuilder tgId(Long tgId) {
      return (CopyOfBuilder) super.tgId(tgId);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder type(Integer type) {
      return (CopyOfBuilder) super.type(type);
    }
  }
  
}
