package cloud.xcan.sdf.core.disruptor;

import cloud.xcan.sdf.spec.EventObject;
import com.lmax.disruptor.EventFactory;

public class EventObjectFactory<T> implements EventFactory<EventObject<T>> {

  public EventObjectFactory() {
  }

  @Override
  public EventObject<T> newInstance() {
    return new EventObject<>();
  }
}