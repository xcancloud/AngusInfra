package cloud.xcan.angus.core.event;

public interface EventPublisher<T> {

  void publishEvent(T event);

}
