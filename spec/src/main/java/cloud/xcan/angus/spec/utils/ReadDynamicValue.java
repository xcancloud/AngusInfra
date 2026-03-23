package cloud.xcan.angus.spec.utils;

/**
 * Sequential string source (for example a circular list) with {@link Cloneable} support.
 */
public interface ReadDynamicValue extends Cloneable {

  String readNext();

  Object clone() throws CloneNotSupportedException;
}
