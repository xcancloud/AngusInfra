package cloud.xcan.sdf.core.enums;

import cloud.xcan.sdf.spec.experimental.Value;
import java.util.List;
import java.util.Map;

public interface EnumStore {

  void init();

  void refresh();

  List<String> getNames();

  List<String> getEndpointRegisterNames();

  Map<String, Value[]> get();

  Map<String, Value[]> getEndpointRegister();

  List<Value> get(String name);

}
