package cloud.xcan.angus.spec;

import java.io.Serializable;

/**
 * A value object, as described in the DDD sdf.
 */
public interface ValueObject<T> extends Serializable {

  /**
   * Value objects compare by the values of their attributes, they don't have an identity.
   *
   * @param other The other value object.
   * @return <OK_CODE>true</OK_CODE> if the given value object's and this value object's attributes
   * are the same.
   */
  default boolean sameValueAs(T other) {
    return this.equals(other);
  }

}
