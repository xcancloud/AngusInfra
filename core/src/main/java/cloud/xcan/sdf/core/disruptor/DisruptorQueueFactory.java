package cloud.xcan.sdf.core.disruptor;

import cloud.xcan.sdf.core.event.EventListener;
import cloud.xcan.sdf.spec.EventObject;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;

/**
 * @author XiaoLong Liu
 */
public class DisruptorQueueFactory {

  public DisruptorQueueFactory() {
  }

  /**
   * Create a single consumer DisruptorQueueManager.
   * <p>
   * Set up a {@link WorkerPool} to distribute an event to one of a pool of work handler threads.
   * Each event will only be processed by one of the work handlers.
   * <p>
   * The message is processed by only one of the consumers.
   */
  public static <T> DisruptorQueueManager<T> createWorkPoolQueue(int queueSize,
      boolean multiProducer, ThreadFactory threadFactory, EventListener<T>... consumers) {
    Disruptor<EventObject<T>> disruptor = new Disruptor<>(
        new EventObjectFactory<>(), queueSize, threadFactory,
        multiProducer ? ProducerType.MULTI : ProducerType.SINGLE, new BlockingWaitStrategy());
    disruptor.handleEventsWithWorkerPool(consumers);
    return new DisruptorQueueManager<>(disruptor);
  }

  /**
   * Create a parallel consumer DisruptorQueueManager.
   * <p>
   * Set up event handlers to handle events from the ring buffer. These handlers will process events
   * as soon as they become available, in parallel.
   * <p>
   * Messages are processed by multiple consumers.
   */
  public static <T> DisruptorQueueManager<T> createHandleEventsQueue(int queueSize,
      boolean multiProducer, ThreadFactory threadFactory, EventListener<T>... consumers) {
    Disruptor<EventObject<T>> disruptor = new Disruptor<>(
        new EventObjectFactory<>(), queueSize, threadFactory,
        multiProducer ? ProducerType.MULTI : ProducerType.SINGLE, new BlockingWaitStrategy());
    disruptor.handleEventsWith(consumers);
    return new DisruptorQueueManager<>(disruptor);
  }

}
