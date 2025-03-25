package cloud.xcan.angus.api.pojo.instance;


import cloud.xcan.angus.spec.ValuedEnum;

/**
 * InstanceType
 *
 * <li>CONTAINER: Such as Docker
 * <li>HOST: Actual machine
 *
 * @author liuxiaolong
 */
public enum InstanceType implements ValuedEnum<Integer> {

  CONTAINER(1), HOST(2);

  /**
   * Lock type
   */
  private final Integer type;

  /**
   * Constructor with field of type
   */
  InstanceType(Integer type) {
    this.type = type;
  }

  @Override
  public Integer value() {
    return type;
  }

}
