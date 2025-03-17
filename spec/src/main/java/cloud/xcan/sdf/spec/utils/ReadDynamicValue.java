package cloud.xcan.sdf.spec.utils;

public interface ReadDynamicValue extends Cloneable {

  String readNext();

  Object clone() throws CloneNotSupportedException;
}
