package cloud.xcan.sdf.core.event.remote;

import cloud.xcan.sdf.core.event.EventRemote;
import cloud.xcan.sdf.core.event.source.EventContent;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "XCAN-EVENT.BOOT")
public interface CommonEventRemote extends EventRemote<EventContent> {

  @Override
  @PostMapping(value = "/doorapi/v1/event")
  void sendEvents(@RequestBody List<EventContent> events);

}
