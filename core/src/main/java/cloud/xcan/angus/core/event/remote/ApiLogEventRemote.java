package cloud.xcan.angus.core.event.remote;

import cloud.xcan.angus.core.event.EventRemote;
import cloud.xcan.angus.core.event.source.ApiLog;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "XCAN-LOGGER.BOOT")
public interface ApiLogEventRemote extends EventRemote<ApiLog> {

  @Override
  @PostMapping(value = "/innerapi/v1/log/api")
  void sendEvents(@RequestBody List<ApiLog> apiLogs);

}
