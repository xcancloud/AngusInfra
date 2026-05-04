package cloud.xcan.angus.core.disruptor;

import cloud.xcan.angus.spec.EventObject;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.List;

public class DisruptorQueueManager<T> {

  private Disruptor<EventObject<T>> disruptor;
  private RingBuffer<EventObject<T>> ringBuffer;

  public DisruptorQueueManager(Disruptor<EventObject<T>> disruptor) {
    this.disruptor = disruptor;
    this.ringBuffer = disruptor.getRingBuffer();
    this.disruptor.start();
  }

  /**
   * Add event source
   *
   * @param t SimpleEvent source object
   */
  public void add(T t) {
    if (t != null) {
      long sequence = this.ringBuffer.next();

      try {
        EventObject<T> event = this.ringBuffer.get(sequence);
        event.setSource(t);
      } finally {
        this.ringBuffer.publish(sequence);
      }
    }
  }

  /**
   * Batch add event source
   *
   * @param ts SimpleEvent source objects
   */
  public void addAll(List<T> ts) {
    if (ts != null) {
      for (T t : ts) {
        if (t != null) {
          this.add(t);
        }
      }
    }
  }

  public long cursor() {
    return this.disruptor.getRingBuffer().getCursor();
  }

  public void shutdown() {
    this.disruptor.shutdown();
  }

  public Disruptor<EventObject<T>> getDisruptor() {
    return this.disruptor;
  }

  public void setDisruptor(Disruptor<EventObject<T>> disruptor) {
    this.disruptor = disruptor;
  }

  public RingBuffer<EventObject<T>> getRingBuffer() {
    return this.ringBuffer;
  }

  public void setRingBuffer(RingBuffer<EventObject<T>> ringBuffer) {
    this.ringBuffer = ringBuffer;
  }

}
