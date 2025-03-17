package cloud.xcan.sdf.core.event;

import cloud.xcan.sdf.spec.EventObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsListener<CommonEvent extends AbstractEvent<?>> implements
    EventListener<CommonEvent> {

  private static final Logger logger = LoggerFactory.getLogger(EventsListener.class);

  private final EventRepository<CommonEvent> eventRepository;

  public EventsListener(EventRepository<CommonEvent> eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Override
  public void onEvent(EventObject<CommonEvent> event) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug(event.getSource().toString());
    }
    this.eventRepository.add(event.getSource());
  }

  @Override
  public void onEvent(EventObject<CommonEvent> event, long sequence, boolean endOfBatch)
      throws Exception {
    this.onEvent(event);
  }
}
