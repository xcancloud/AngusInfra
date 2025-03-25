package cloud.xcan.angus.spec.experimental;


import cloud.xcan.angus.spec.ValueObject;

public enum ModifyType implements ValueObject<ModifyType>, Value<String> {
  UPDATE, REPLACE;

  @Override
  public String getValue() {
    return this.name();
  }
}
