package cloud.xcan.sdf.spec.unit;

import cloud.xcan.sdf.spec.experimental.Value;

public interface ValueUnit<V, U> extends Value<V> {

  U getUnit();

  String toHumanString();

}
