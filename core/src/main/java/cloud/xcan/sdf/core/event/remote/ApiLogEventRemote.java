package cloud.xcan.sdf.core.event.remote;

import cloud.xcan.sdf.core.event.EventRemote;
import cloud.xcan.sdf.core.event.source.ApiLog;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "XCAN-LOGGER.BOOT")
public interface ApiLogEventRemote extends EventRemote<ApiLog> {

  @Override
  @PostMapping(value = "/doorapi/v1/log/api")
  void sendEvents(@RequestBody List<ApiLog> apiLogs);

}
