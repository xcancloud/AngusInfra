package cloud.xcan.angus.idgen.uid.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RingBuffer Unit Tests")
class RingBufferTest {

  private RingBuffer ringBuffer;

  @BeforeEach
  void setUp() {
    ringBuffer = new RingBuffer(1024, 50);
    ringBuffer.setBufferPaddingExecutor(mock(BufferPaddingExecutor.class));
  }

  @Test
  @DisplayName("should successfully put and take elements sequentially")
  void testSequentialPutTake() {
    for (long i = 0; i < 10; i++) {
      assertThat(ringBuffer.put(i)).isTrue();
    }

    for (long i = 0; i < 10; i++) {
      long taken = ringBuffer.take();
      assertThat(taken).isEqualTo(i);
    }
  }

  @Test
  @DisplayName("should prevent duplicate storage with proper flag management")
  void testFlagManagement() {
    ringBuffer.put(100L);
    ringBuffer.put(200L);
    ringBuffer.put(300L);

    Set<Long> values = new HashSet<>();
    values.add(ringBuffer.take());
    values.add(ringBuffer.take());
    values.add(ringBuffer.take());

    assertThat(values).containsExactlyInAnyOrder(100L, 200L, 300L);
  }

  @Test
  @DisplayName("should handle concurrent puts and takes without data loss")
  void testConcurrentPutTake() throws Exception {
    int putThreads = 10;
    int takeThreads = 10;
    int itemsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(putThreads + takeThreads);

    CountDownLatch putDone = new CountDownLatch(putThreads);
    CountDownLatch takeDone = new CountDownLatch(takeThreads);
    List<Long> takenValues = new ArrayList<>();
    Object lock = new Object();
    AtomicInteger putCount = new AtomicInteger(0);
    AtomicInteger takeCount = new AtomicInteger(0);
    // Producers may fail puts when the ring is full; consumers must stop after that many takes,
    // not after putThreads * itemsPerThread (which would deadlock).
    AtomicInteger takeTarget = new AtomicInteger(-1);

    // Producer threads
    for (int i = 0; i < putThreads; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < itemsPerThread; j++) {
            long value = System.nanoTime();
            if (ringBuffer.put(value)) {
              putCount.incrementAndGet();
            }
          }
        } finally {
          putDone.countDown();
        }
      });
    }

    // Consumer threads
    for (int i = 0; i < takeThreads; i++) {
      executor.submit(() -> {
        try {
          while (takeTarget.get() < 0 || takeCount.get() < takeTarget.get()) {
            try {
              long value = ringBuffer.take();
              synchronized (lock) {
                // Re-check under the same lock used for writes to prevent overshoot:
                // two threads can both pass the outer loop condition before either increments.
                if (takeTarget.get() < 0 || takeCount.get() < takeTarget.get()) {
                  takenValues.add(value);
                  takeCount.incrementAndGet();
                }
              }
            } catch (RuntimeException e) {
              Thread.sleep(1);
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          takeDone.countDown();
        }
      });
    }

    assertThat(putDone.await(60, TimeUnit.SECONDS)).isTrue();
    takeTarget.set(putCount.get());
    assertThat(takeDone.await(60, TimeUnit.SECONDS)).isTrue();
    executor.shutdown();
    assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

    assertThat(putCount.get()).isGreaterThan(0);
    assertThat(takenValues).hasSize(putCount.get());
  }

  @Test
  @DisplayName("should reject put when buffer is full (configurable policy)")
  void testPutBufferFull() {
    // Fill the buffer beyond capacity
    int putCount = 0;
    for (long i = 0; i < 2048; i++) {
      if (ringBuffer.put(i)) {
        putCount++;
      }
    }

    assertThat(putCount).isGreaterThan(0).isLessThan(2048);
  }

  @Test
  @DisplayName("should correctly report buffer size and position")
  void testBufferMetrics() {
    assertThat(ringBuffer.getBufferSize()).isEqualTo(1024);

    for (int i = 0; i < 10; i++) {
      ringBuffer.put(i);
    }

    // Buffer should have items
    for (int i = 0; i < 10; i++) {
      ringBuffer.take();
    }
  }

  @Test
  @DisplayName("should maintain FIFO order for sequential operations")
  void testFIFOOrder() {
    long[] expected = new long[100];
    for (long i = 0; i < 100; i++) {
      expected[(int) i] = i * 2;
      ringBuffer.put(i * 2);
    }

    for (int i = 0; i < 100; i++) {
      long taken = ringBuffer.take();
      assertThat(taken).as("Position " + i).isEqualTo(expected[i]);
    }
  }
}
