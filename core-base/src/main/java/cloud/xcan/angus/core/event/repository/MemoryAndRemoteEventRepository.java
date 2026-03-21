package cloud.xcan.angus.core.event.repository;

import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.event.AbstractEvent;
import cloud.xcan.angus.core.event.EventRemote;
import cloud.xcan.angus.core.event.EventRepository;
import cloud.xcan.angus.spec.thread.DefaultThreadFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.util.CollectionUtils;

/**
 * Remote and In-memory {@link EventRepository} implementation.
 *
 * @author XiaoLong Liu
 * @see AuditEventsEndpoint
 * @see InMemoryAuditEventRepository
 * @since 1.0.0
 */
@Slf4j
public class MemoryAndRemoteEventRepository<T extends AbstractEvent<?>> implements
    EventRepository<T> {

  public static final int DEFAULT_MEMORY_CAPACITY = 1024;
  public static final int DEFAULT_SEND_BUFFER_CAPACITY = 10 * 1024;
  private final Object monitor = new Object();

  private final ScheduledExecutorService timer;

  private final EventRemote eventRemote;

  private final ObjectMapper objectMapper;

  /**
   * Circular buffer of the event with tail pointing to the last element.
   */
  private Object[] events;

  /**
   * Note: This is a blocking queue with low performance.
   */
  private final LinkedBlockingQueue<T> queue;

  private volatile int tail = -1;

  public MemoryAndRemoteEventRepository() {
    this("", DEFAULT_MEMORY_CAPACITY, DEFAULT_SEND_BUFFER_CAPACITY, null, null);
  }

  public MemoryAndRemoteEventRepository(String name, int memoryCapacity, int sendBufferCapacity,
      EventRemote eventRemote, ObjectMapper objectMapper) {
    this.events = new Object[memoryCapacity];
    this.queue = new LinkedBlockingQueue<>(sendBufferCapacity);
    this.eventRemote = eventRemote;
    this.objectMapper = objectMapper;
    this.timer = Executors.newSingleThreadScheduledExecutor(
        new DefaultThreadFactory("XCan-Event-Send" + name, Thread.NORM_PRIORITY));
    timer.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        batchSendEvents();
      }
    }, 3, 1, TimeUnit.SECONDS);
  }

  /**
   * Set the capacity of this event repository.
   *
   * @param capacity the capacity
   */
  public void setCapacity(int capacity) {
    synchronized (this.monitor) {
      this.events = new Object[capacity];
    }
  }

  @Override
  public void add(T event) {
    assertNotNull(event, "SimpleEvent must not be null");
    synchronized (this.monitor) {
      this.tail = (this.tail + 1) % this.events.length;
      this.events[this.tail] = event;
      if (!this.queue.offer(event)) {
        batchSendEvents();
      }
    }
  }

  @Override
  public void add(List<T> events) {
    assertNotNull(events, "SimpleEvent must not be null");
    events.forEach(this::add);
  }

  @Override
  public List<T> find(String name, Instant after, String type) {
    LinkedList<T> events = new LinkedList<>();
    synchronized (this.monitor) {
      for (int i = 0; i < this.events.length; i++) {
        T event = resolveTailEvent(i);
        if (event != null && isMatch(name, after, type, event)) {
          events.addFirst(event);
        }
      }
    }
    return events;
  }

  private boolean isMatch(String name, Instant after, String type, T event) {
    boolean match;
    long timeInSeconds = event.getTimestamp().toEpochSecond(ZoneOffset.UTC);
    Instant instant = Instant.ofEpochSecond(timeInSeconds);
    match = name == null || event.getCode().equals(name);
    match = match && (after == null || instant.isAfter(after));
    match = match && type == null;
    return match;
  }

  private T resolveTailEvent(int offset) {
    int index = ((this.tail + this.events.length - offset) % this.events.length);
    return (T) this.events[index];
  }

  private synchronized int batchSendEvents() {
    if (isNull(eventRemote)) {
      return 0;
    }
    List<Object> eventSources = new ArrayList<>();
    int i = 0;
    T event = queue.poll();
    while (Objects.nonNull(event) && Objects.nonNull(event.getSource()) && i < 500) {
      if (log.isDebugEnabled()) {
        log.debug("Polling send event({}) : {}", event.getCode(), event.getDescription());
      }
      eventSources.add(event.getSource());
      event = queue.poll();
      i++;
    }
    try {
      if (!CollectionUtils.isEmpty(eventSources)) {
        if (log.isTraceEnabled()) {
          log.trace("Send event to remote: {}", objectMapper.writeValueAsString(eventSources));
        }
        eventRemote.sendEvents(eventSources);
        if (log.isDebugEnabled()) {
          log.debug("Send {} event to remote", i);
        }
      }
    } catch (Exception e) {
      log.error("Send {} event to remote exception, cause: ", i, e);
    } finally {
      eventSources = null;
    }
    if (i < 500) {
      return i;
    }
    return batchSendEvents();
  }
}
