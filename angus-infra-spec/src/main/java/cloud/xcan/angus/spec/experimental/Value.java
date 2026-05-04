package cloud.xcan.angus.spec.experimental;

import java.io.Serializable;

public interface Value<V> extends Serializable {

  V getValue();

}
