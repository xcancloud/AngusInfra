package cloud.xcan.sdf.spec.experimental;

import java.io.Serializable;

/**
 * An entity, as explained in the DDD sdf.
 */
public interface Entity<T extends Entity<T, ID>, ID> extends Serializable {

  /**
   * Entities compare by identity, not by attributes.
   *
   * @param other The other entity.
   * @return true if the identities are the same, regardles of other attributes.
   */
  boolean sameIdentityAs(T other);

  /**
   * Entities have an identity.
   *
   * @return The identity of this entity.
   */
  ID identity();

}