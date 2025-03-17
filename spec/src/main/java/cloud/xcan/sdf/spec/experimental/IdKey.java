package cloud.xcan.sdf.spec.experimental;

public class IdKey<ID, Key> extends EntitySupport {

  private ID id;
  private Key key;

  @Override
  public ID identity() {
    return this.id;
  }

  public IdKey<ID, Key> setId(ID id) {
    this.id = id;
    return this;
  }

  public IdKey<ID, Key> setKey(Key key) {
    this.key = key;
    return this;
  }

  public ID getId() {
    return this.id;
  }

  public Key getKey() {
    return this.key;
  }

  public IdKey() {
  }

  public IdKey(ID id, Key key) {
    this.id = id;
    this.key = key;
  }
}

