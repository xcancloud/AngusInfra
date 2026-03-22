package cloud.xcan.angus.idgen.uid.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard rejection policies for RingBuffer put operations when buffer is full.
 * <p>
 * These policies provide different strategies for handling rejected UID generation requests when
 * the ring buffer reaches capacity.
 */
public class RejectedPutBufferPolicies {

  private static final Logger LOGGER = LoggerFactory.getLogger(RejectedPutBufferPolicies.class);

  /**
   * Discard policy: Simply logs and discards the UID without raising an exception. Use this only
   * for non-critical scenarios where losing some UIDs is acceptable.
   * <p>
   * Trade-off: Lower latency but potential data loss.
   */
  public static class DiscardPolicy implements RejectedPutBufferHandler {

    @Override
    public void rejectPutBuffer(RingBuffer ringBuffer, long uid) {
      LOGGER.warn("Rejected putting UID into buffer (buffer full). UID={}, BufferSize={}, "
              + "Tail={}, Cursor={}", uid, ringBuffer.getBufferSize(),
          ringBuffer.getTail(), ringBuffer.getCursor());
    }
  }

  /**
   * Exception policy: Throws an exception immediately when buffer is full. This forces the caller
   * to handle the rejection explicitly, providing better error awareness.
   * <p>
   * Trade-off: Higher reliability but may cause cascading failures.
   */
  public static class ExceptionPolicy implements RejectedPutBufferHandler {

    @Override
    public void rejectPutBuffer(RingBuffer ringBuffer, long uid) {
      throw new IllegalStateException(
          String.format("RingBuffer is full. Unable to put UID. BufferSize=%d, Tail=%d, Cursor=%d",
              ringBuffer.getBufferSize(), ringBuffer.getTail(), ringBuffer.getCursor()));
    }
  }

  /**
   * Block policy: Blocks the caller thread until buffer has space available. This ensures no UIDs
   * are lost but may increase latency.
   * <p>
   * Trade-off: Higher reliability but potential thread blocking.
   */
  public static class BlockPolicy implements RejectedPutBufferHandler {

    private static final int BACKOFF_INTERVAL_MS = 10;

    @Override
    public void rejectPutBuffer(RingBuffer ringBuffer, long uid) {
      LOGGER.debug("RingBuffer full, blocking until space available. UID={}", uid);

      // Exponential backoff to avoid busy-waiting
      while (ringBuffer.getTail() - ringBuffer.getCursor() >= ringBuffer.getBufferSize() - 1) {
        try {
          Thread.sleep(BACKOFF_INTERVAL_MS);
        } catch (InterruptedException e) {
          LOGGER.warn("Thread interrupted while waiting for buffer space", e);
          Thread.currentThread().interrupt();
          throw new RuntimeException("Interrupted while waiting for buffer space", e);
        }
      }

      LOGGER.debug("RingBuffer has space available, retrying put. UID={}", uid);
    }
  }
}
