package cloud.xcan.angus.core.disruptor;

import cloud.xcan.angus.spec.EventObject;
import com.lmax.disruptor.EventFactory;

public class EventObjectFactory<T> implements EventFactory<EventObject<T>> {

  public EventObjectFactory() {
  }

  @Override
  public EventObject<T> newInstance() {
    return new EventObject<>();
  }
}
