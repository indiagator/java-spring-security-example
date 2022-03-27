package io.example.domain.model;

import org.bson.types.ObjectId;

import java.io.Serializable;

public abstract class ComparableEntity implements Serializable {

  public abstract ObjectId getId();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    ComparableEntity that = (ComparableEntity) o;
    return getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }
}
