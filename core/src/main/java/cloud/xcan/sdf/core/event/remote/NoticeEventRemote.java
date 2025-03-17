package cloud.xcan.sdf.core.event.remote;

import cloud.xcan.sdf.core.event.EventRemote;
import cloud.xcan.sdf.core.event.SimpleEvent;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "XCAN-EVENT.BOOT")
public interface NoticeEventRemote extends EventRemote<SimpleEvent> {

  @Override
  @PostMapping(value = "/doorapi/v1/event/notice")
  void sendEvents(@RequestBody List<SimpleEvent> events);

}
