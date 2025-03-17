package cloud.xcan.sdf.spec.experimental;

import java.io.Serializable;

public interface Value<V> extends Serializable {

  V getValue();

}
