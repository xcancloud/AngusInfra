package cloud.xcan.angus.core.event.remote;

import static cloud.xcan.angus.remote.ApiConstant.Service.LOGGER_SERVICE_ARTIFACT_ID;

import cloud.xcan.angus.core.event.EventRemote;
import cloud.xcan.angus.core.event.source.ApiLog;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${xcan.api-log.loggerService:" + LOGGER_SERVICE_ARTIFACT_ID + "}")
public interface ApiLogEventRemote extends EventRemote<ApiLog> {

  @Override
  @PostMapping(value = "/innerapi/v1/interface/logs/batch")
  void sendEvents(@RequestBody List<ApiLog> apiLogs);

}
