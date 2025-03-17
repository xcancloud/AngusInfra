package cloud.xcan.sdf.core.event;

import static cloud.xcan.sdf.core.spring.SpringContextHolder.getBean;

import cloud.xcan.sdf.core.disruptor.DisruptorQueueManager;
import cloud.xcan.sdf.core.event.source.ApiLog;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.core.event.source.UserOperation;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventSender {

  public static class CommonQueue {

    public static final String QUEUE_NAME = "commonEventDisruptorQueue";

    public static DisruptorQueueManager<CommonEvent> queue;

    public static void send(EventContent source) {
      if (Objects.isNull(queue)) {
        loadQueue();
      }
      if (Objects.isNull(queue)) {
        log.error("[Common]Event queue {} not found, ignore event source: {}", QUEUE_NAME,
            source.toString());
        return;
      }
      queue.add(new CommonEvent(source));
      if (log.isDebugEnabled()) {
        log.debug("[Common]Event queue send event `{}` source", source.getCode());
      }
    }

    public static void send(CommonEvent event) {
      if (Objects.isNull(queue)) {
        loadQueue();
      }
      if (Objects.isNull(queue)) {
        log.error("[Common]Event queue {} not found, ignore event: {}", QUEUE_NAME,
            event.toString());
        return;
      }
      queue.add(event);
      if (log.isDebugEnabled()) {
        log.debug("[Common]Event queue send event `{}` source", event.getCode());
      }
    }

    public synchronized static void loadQueue() {
      try {
        queue = getBean(QUEUE_NAME, DisruptorQueueManager.class);
      } catch (Exception e) {
        // noop
      }
    }
  }

  public static class OperationQueue {

    public static final String QUEUE_NAME = "operationEventDisruptorQueue";

    public static DisruptorQueueManager<OperationEvent> queue;

    public static void send(UserOperation source) {
      if (Objects.isNull(queue)) {
        loadQueue();
      }
      if (Objects.isNull(queue)) {
        log.error("[Operation]Event queue {} not found, ignore event source: {}", QUEUE_NAME,
            source.toString());
        return;
      }
      queue.add(new OperationEvent(source));
      if (log.isDebugEnabled()) {
        log.debug("[Operation]Event queue send event `{}` source", source.getCode());
      }
    }

    public static void send(OperationEvent event) {
      if (Objects.isNull(queue)) {
        loadQueue();
      }
      if (Objects.isNull(queue)) {
        log.error("[Operation]Event queue {} not found, ignore event: {}", QUEUE_NAME,
            event.toString());
        return;
      }
      queue.add(event);
      if (log.isDebugEnabled()) {
        log.debug("[Operation]Event queue send event `{}` source", event.getCode());
      }
    }

    public synchronized static void loadQueue() {
      try {
        queue = getBean(QUEUE_NAME, DisruptorQueueManager.class);
      } catch (Exception e) {
        // noop
      }
    }
  }

  public static class ApiLogQueue {

    public static final String QUEUE_NAME = "apiLogEventDisruptorQueue";

    public static DisruptorQueueManager<ApiLogEvent> queue;

    public static void send(ApiLog source) {
      if (Objects.isNull(queue)) {
        loadQueue();
      }
      if (Objects.isNull(queue)) {
        log.error("[ApiLog]Event queue {} not found, ignore event source: {}", QUEUE_NAME,
            source.toString());
        return;
      }
      queue.add(new ApiLogEvent(source));
      if (log.isDebugEnabled()) {
        log.debug("[ApiLog]Event queue send event `{}` source", source.getCode());
      }
    }

    public static void send(ApiLogEvent event) {
      if (Objects.isNull(queue)) {
        loadQueue();
      }
      if (Objects.isNull(queue)) {
        log.error("[ApiLog]Event queue {} not found, ignore event: {}", QUEUE_NAME,
            event.toString());
        return;
      }
      queue.add(event);
      if (log.isDebugEnabled()) {
        log.debug("[ApiLog]Event queue send event `{}` source", event.getCode());
      }
    }

    public synchronized static void loadQueue() {
      try {
        queue = getBean(QUEUE_NAME, DisruptorQueueManager.class);
      } catch (Exception e) {
        // noop
      }
    }
  }
}
