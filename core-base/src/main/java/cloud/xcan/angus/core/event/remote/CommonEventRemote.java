package cloud.xcan.angus.core.event.remote;

import cloud.xcan.angus.core.event.EventRemote;
import cloud.xcan.angus.core.event.source.EventContent;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "XCAN-EVENT.BOOT")
public interface CommonEventRemote extends EventRemote<EventContent> {

  @Override
  @PostMapping(value = "/innerapi/v1/event")
  void sendEvents(@RequestBody List<EventContent> events);

}
