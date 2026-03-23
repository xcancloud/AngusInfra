package cloud.xcan.angus.spec.unit;

import cloud.xcan.angus.spec.experimental.Value;

/**
 * A measured quantity with a unit (e.g. {@link DataSize}, {@link TimeValue}).
 *
 * @param <V> numeric or scalar value type
 * @param <U> unit enum or descriptor type
 */
public interface ValueUnit<V, U> extends Value<V> {

  U getUnit();

  /** Compact, human-oriented representation (often rounded). */
  String toHumanString();
}
