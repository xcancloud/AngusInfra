package cloud.xcan.sdf.spec.experimental;

import java.io.Serializable;

/**
 * A value object.
 */
public interface ValueObject<T> extends Serializable {

  /**
   * Value objects compare by the values of their attributes, they don't have an identity.
   *
   * @param other The other value object.
   * @return <OK_CODE>true</OK_CODE> if the given value object's and this value object's attributes
   * are the same.
   */
  boolean sameValueAs(T other);

  /**
   * Value objects can be freely copied.
   *
   * @return A safe, deep copy of this value object.
   */
  T copy();

}