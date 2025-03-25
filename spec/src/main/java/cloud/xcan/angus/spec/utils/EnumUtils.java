package cloud.xcan.angus.spec.utils;

import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;

import cloud.xcan.angus.spec.ValuedEnum;
import cloud.xcan.angus.spec.locale.EnumValueMessage;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;

public final class EnumUtils {

  private EnumUtils() { /* no instance */ }

  public static <E extends EnumValueMessage<?>> E of(Class<E> classType, String value) {
    for (E enumConstant : classType.getEnumConstants()) {
      if (value.equalsIgnoreCase(String.valueOf(enumConstant.getValue()))) {
        return enumConstant;
      }
    }
    return null;
  }

  /**
   * Parse the bounded value into ValuedEnum
   */
  public static <T extends ValuedEnum<V>, V> T parse(Class<T> clz,
      V value) {
    assertNotNull(clz, "clz can not be null");
    if (value == null) {
      return null;
    }

    for (T t : clz.getEnumConstants()) {
      if (value.equals(t.value())) {
        return t;
      }
    }
    return null;
  }

  /**
   * Null-safe valueOf function
   */
  public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
    return name == null ? null : Enum.valueOf(enumType, name);
  }

  /**
   * Create a map that indexes all enum values by a given index function. This can offer a faster
   * runtime complexity compared to iterating an enum's {@code values()}.
   *
   * @see ObjectUtils#uniqueIndex(Iterable, Function)
   */
  public static <K, V extends Enum<V>> Map<K, V> uniqueIndex(Class<V> enumType,
      Function<? super V, K> indexFunction) {
    return ObjectUtils.uniqueIndex(EnumSet.allOf(enumType), indexFunction);
  }
}
