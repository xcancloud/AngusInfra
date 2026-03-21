package cloud.xcan.angus.core.event;

import cloud.xcan.angus.spec.EventObject;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString(callSuper = true)
public class OperationEventListener<OperationEvent extends AbstractEvent<?>> implements
    EventListener<OperationEvent> {

  private static final Logger logger = LoggerFactory.getLogger(OperationEventListener.class);

  private final EventRepository<OperationEvent> eventRepository;

  public OperationEventListener(EventRepository<OperationEvent> eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Override
  public void onEvent(EventObject<OperationEvent> event) {
    if (logger.isDebugEnabled()) {
      logger.debug(event.getSource().toString());
    }
    this.eventRepository.add(event.getSource());
  }

  @Override
  public void onEvent(EventObject<OperationEvent> event, long sequence, boolean endOfBatch)
      throws Exception {
    this.onEvent(event);
  }
}
