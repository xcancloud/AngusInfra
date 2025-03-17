package cloud.xcan.sdf.spec.experimental;

import java.io.Serializable;

/**
 * Note: Extends Serializable is for fix warn: JavaTypeDescriptorRegistry  -> HHH000481: Encountered Java type [interface cloud.xcan.sdf.spec.experimental.CsvConverter] for which we could not locate a JavaTypeDescriptor and which does not appear to implement equals and/or hashCode.  This can lead to significant performance problems when performing equality/dirty checking involving this Java type.  Consider registering a custom JavaTypeDescriptor or at least implementing equals/hashCode.
 * @param <T>
 */
public interface CsvConverter<T> extends Serializable {

  T fromString(String... cvs);

}
