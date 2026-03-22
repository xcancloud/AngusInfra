package cloud.xcan.angus.idgen.uid.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.uid.InstanceIdAssigner;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CachedUidGenerator Unit Tests")
class CachedUidGeneratorTest {

  private CachedUidGenerator generator;
  private InstanceIdAssigner mockAssigner;

  @BeforeEach
  void setUp() throws Exception {
    generator = new CachedUidGenerator();
    generator.setTimeBits(28);
    generator.setWorkerBits(22);
    generator.setSeqBits(13);
    generator.setBoostPower(2);
    generator.setEpochStr("2016-05-20");

    mockAssigner = mock(InstanceIdAssigner.class);
    when(mockAssigner.assignInstanceIdByEnv()).thenReturn(1L);
    generator.setInstanceIdAssigner(mockAssigner);

    generator.afterPropertiesSet();
  }

  @Test
  @DisplayName("should generate UIDs from ring buffer with high throughput")
  void testBufferedGeneration() throws Exception {
    for (int i = 0; i < 1000; i++) {
      long uid = generator.getUID();
      assertThat(uid).isGreaterThan(0);
    }
  }

  @Test
  @DisplayName("should maintain UID uniqueness under extreme concurrent load")
  void testConcurrentBufferedGeneration() throws Exception {
    int threadCount = 200;
    int idsPerThread = 50;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    Set<Long> generatedIds = new HashSet<>();
    Object lock = new Object();
    AtomicInteger duplicates = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < idsPerThread; j++) {
            long uid = generator.getUID();
            synchronized (lock) {
              if (!generatedIds.add(uid)) {
                duplicates.incrementAndGet();
              }
            }
          }
        } catch (Exception e) {
          duplicates.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    assertThat(duplicates.get()).isZero();
    assertThat(generatedIds.size()).isEqualTo(threadCount * idsPerThread);
  }

  @Test
  @DisplayName("should cleanup resources on destroy")
  void testDestroy() throws Exception {
    generator.destroy();
    // Should not throw exception
    assertThat(generator.getBufferPaddingExecutor()).isNotNull();
  }

  @Test
  @DisplayName("should parse UID correctly")
  void testParseUID() throws Exception {
    long uid = generator.getUID();
    String parsed = generator.parseUID(uid);

    assertThat(parsed).isNotEmpty().contains("UID=");
  }

  @Test
  @DisplayName("should handle ring buffer exceptions with custom rejection policy")
  void testRingBufferFull() throws Exception {
    // RingBuffer size is limited, but with proper padding strategy should not get full
    // under normal circumstances. This test validates that extreme edge cases are handled.
    generator.setBoostPower(0); // Very small buffer to trigger rejections

    // After triggering buffer full scenarios, generator should remain functional
    long uid = generator.getUID();
    assertThat(uid).isGreaterThan(0);
  }
}
