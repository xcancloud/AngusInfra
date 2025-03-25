package cloud.xcan.angus.core.event.remote;

import cloud.xcan.angus.core.event.EventRemote;
import cloud.xcan.angus.core.event.source.UserOperation;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "XCAN-LOGGER.BOOT")
public interface OperationEventRemote extends EventRemote<UserOperation> {

  @Override
  @PostMapping(value = "/doorapi/v1/log/operation")
  void sendEvents(@RequestBody List<UserOperation> events);

}
