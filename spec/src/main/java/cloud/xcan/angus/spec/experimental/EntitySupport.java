package cloud.xcan.angus.spec.experimental;


public abstract class EntitySupport<T extends Entity<T, ID>, ID> implements Entity<T, ID> {

  public EntitySupport() {
  }

  @Override
  public abstract ID identity();

  @Override
  public boolean sameIdentityAs(T other) {
    return other != null && this.identity().equals(other.identity());
  }
}
