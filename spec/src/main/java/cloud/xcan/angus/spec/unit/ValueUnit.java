package cloud.xcan.angus.spec.unit;

import cloud.xcan.angus.spec.experimental.Value;

public interface ValueUnit<V, U> extends Value<V> {

  U getUnit();

  String toHumanString();

}
