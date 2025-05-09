package cloud.xcan.angus.web.endpoint;

import cloud.xcan.angus.core.enums.EnumStore;
import cloud.xcan.angus.spec.experimental.Value;
import java.util.List;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

@Endpoint(id = "enums")
public class EnumEndpoint {

  private EnumStore enumStore;

  public EnumEndpoint(EnumStore enumStore) {
    this.enumStore = enumStore;
  }

  @ReadOperation
  public Map<String, Value[]> enumAll() {
    return enumStore.getEndpointRegister();
  }

  @ReadOperation
  public List<Value> enumOne(@Selector String name) {
    return enumStore.get(name);
  }

}
