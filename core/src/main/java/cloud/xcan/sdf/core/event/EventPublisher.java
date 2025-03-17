package cloud.xcan.sdf.core.event;

public interface EventPublisher<T> {

  void publishEvent(T event);

}
