package cloud.xcan.sdf.spec.experimental;


public abstract class EntitySupport<T extends Entity<T, ID>, ID> implements Entity<T, ID> {

  public EntitySupport() {
  }

  @Override
  public abstract ID identity();

  @Override
  public boolean sameIdentityAs(T other) {
    return other != null && this.identity().equals(other.identity());
  }

// Fix:: Incomplete attribute comparison results in incorrect judgment of JPA modification.
//  /**
//   * @param object to compare
//   * @return True if they have the same identity
//   * @see #sameIdentityAs(T)
//   */
//  public boolean equals(final T object) {
//    if (this == object) {
//      return true;
//    }
//    if (object == null || getClass() != object.getClass()) {
//      return false;
//    }
//    return sameIdentityAs((T) object);
//  }

}
