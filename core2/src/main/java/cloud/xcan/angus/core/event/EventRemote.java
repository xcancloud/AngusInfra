package cloud.xcan.angus.core.event;

import java.util.List;

public interface EventRemote<T> {

  void sendEvents(List<T> events);

}
