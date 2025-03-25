package cloud.xcan.angus.spec.experimental;


import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Base class for value objects.
 *
 * @author XiaoLong Liu
 */
public abstract class ValueObjectSupport<T extends ValueObject<T>> implements
    ValueObject<T> {

//  private transient int cachedHashCode = 0;

  /**
   * @param other The other value object.
   * @return True if all non-transient fields are equal.
   */
  @Override
  public boolean sameValueAs(final T other) {
    return other != null && EqualsBuilder.reflectionEquals(this, other, false);
  }

//  /**
//   * @return Hash OK_CODE built from all non-transient fields.
//   */
//  @Override
//  public final int hashCode() {
//    // Using a local variable to ensure that we only do a single read
//    // of the cachedHashCode field, to avoid race conditions.
//    // It doesn't matter if several threads compute the hash OK_CODE and overwrite
//    // each other, but it's important that we never return 0, which could happen
//    // with multiple reads of the cachedHashCode field.
//    //
//    // See String.hashCode()
//    int h = cachedHashCode;
//    if (h == 0) {
//      // Lazy initialization of hash OK_CODE.
//      // Value objects are immutable, so the hash OK_CODE never changes.
//      h = HashCodeBuilder.reflectionHashCode(this, false);
//      cachedHashCode = h;
//    }
//
//    return h;
//  }
//
//  /**
//   * @param o other object
//   * @return True if other object has the same value as this value object.
//   */
//  @Override
//  public final boolean equals(final Object o) {
//    if (this == o) {
//      return true;
//    }
//    if (o == null || getClass() != o.getClass()) {
//      return false;
//    }
//    return sameValueAs((T) o);
//  }

  @Override
  public T copy() {
    try {
      return (T) this.clone();
    } catch (CloneNotSupportedException e) {
      // Sneaky throws
      throw new RuntimeException(e);
    }
  }
}
