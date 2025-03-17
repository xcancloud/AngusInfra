package cloud.xcan.sdf.core.event;

import java.time.Instant;
import java.util.List;

/**
 * Repository for {@link AbstractEvent}s.
 *
 * @author XiaoLong Liu
 */
public interface EventRepository<T extends AbstractEvent> {

  /**
   * SystemRequest an event.
   *
   * @param event the audit event to log
   */
  void add(T event);

  /**
   * SystemRequest events.
   *
   * @param events the audit event to log
   */
  void add(List<T> events);

  /**
   * Find audit events of specified type relating to the specified principal that occurred {@link
   * Instant#isAfter(Instant) after} the time provided.
   *
   * @param name  the event name to search for (or {@code null} if unrestricted)
   * @param after time after which an event must have occurred (or {@code null} if unrestricted)
   * @param type  the event type to search for (or {@code null} if unrestricted)
   * @return audit events of specified type relating to the principal
   * @since 1.4.0
   */
  List<T> find(String name, Instant after, String type);

}
