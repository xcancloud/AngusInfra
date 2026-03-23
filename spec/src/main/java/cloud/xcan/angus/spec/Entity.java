package cloud.xcan.angus.spec;

/**
 * An entity, as explained in Domain-Driven Design (identity over attribute equality).
 */
public interface Entity<T> {

  /**
   * Entities compare by identity, not by attributes.
   *
   * @param other The other entity.
   * @return true if the identities are the same, regardless of other attributes.
   */
  boolean sameIdentityAs(T other);

}
