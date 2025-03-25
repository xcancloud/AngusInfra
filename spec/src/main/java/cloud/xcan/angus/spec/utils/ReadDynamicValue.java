package cloud.xcan.angus.spec.utils;

public interface ReadDynamicValue extends Cloneable {

  String readNext();

  Object clone() throws CloneNotSupportedException;
}
