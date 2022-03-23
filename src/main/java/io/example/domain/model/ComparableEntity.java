package io.example.domain.model;

import java.io.Serializable;

import org.bson.types.ObjectId;

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
