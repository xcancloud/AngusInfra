package cloud.xcan.sdf.spec.experimental;


public enum ModifyType implements cloud.xcan.sdf.spec.ValueObject<ModifyType>, Value<String> {
  UPDATE, REPLACE;

  @Override
  public String getValue() {
    return this.name();
  }
}