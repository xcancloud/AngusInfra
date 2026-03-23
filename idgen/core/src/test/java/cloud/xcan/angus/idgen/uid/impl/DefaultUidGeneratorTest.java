package cloud.xcan.angus.idgen.uid.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.api.pojo.instance.InstanceInfo;
import cloud.xcan.angus.api.pojo.instance.InstanceType;
import cloud.xcan.angus.idgen.exception.IdGenerateException;
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

@DisplayName("DefaultUidGenerator Unit Tests")
class DefaultUidGeneratorTest {

  private DefaultUidGenerator generator;
  private InstanceIdAssigner mockAssigner;

  @BeforeEach
  void setUp() throws Exception {
    generator = new DefaultUidGenerator();
    generator.setTimeBits(28);
    generator.setWorkerBits(22);
    generator.setSeqBits(13);
    // 28-bit delta seconds is ~8.5 years; 2016-05-20 is exhausted by 2026 — use default-era epoch.
    generator.setEpochStr("2021-01-01");

    mockAssigner = mock(InstanceIdAssigner.class);
    when(mockAssigner.assignInstanceIdByEnv()).thenReturn(1L);
    generator.setInstanceIdAssigner(mockAssigner);

    generator.afterPropertiesSet();
  }

  @Test
  @DisplayName("should generate unique IDs sequentially")
  void testSequentialGeneration() throws Exception {
    long uid1 = generator.getUID();
    long uid2 = generator.getUID();
    long uid3 = generator.getUID();

    assertThat(uid1).isGreaterThan(0);
    assertThat(uid2).isGreaterThan(uid1);
    assertThat(uid3).isGreaterThan(uid2);
  }

  @Test
  @DisplayName("should generate unique IDs under concurrent load")
  void testConcurrentGeneration() throws Exception {
    int threadCount = 100;
    int idsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    Set<Long> generatedIds = new HashSet<>();
    Object lockIds = new Object();

    AtomicInteger errorCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < idsPerThread; j++) {
            long uid = generator.getUID();
            synchronized (lockIds) {
              if (!generatedIds.add(uid)) {
                errorCount.incrementAndGet();
              }
            }
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    assertThat(errorCount.get()).isZero();
    assertThat(generatedIds).hasSize(threadCount * idsPerThread);
  }

  @Test
  @DisplayName("should parse UID correctly")
  void testParseUID() throws Exception {
    long uid = generator.getUID();
    String parsed = generator.parseUID(uid);

    assertThat(parsed).isNotEmpty();
    assertThat(parsed)
        .contains("\"UID\"")
        .contains("\"datetime\"")
        .contains("\"instanceId\"")
        .contains("\"sequence\"");
  }

  @Test
  @DisplayName("should throw exception when instance id assignment fails")
  void testFailureOnInstanceIdAssignment() {
    DefaultUidGenerator failingGenerator = new DefaultUidGenerator();
    failingGenerator.setRetriesNum(0);

    InstanceIdAssigner failingAssigner = mock(InstanceIdAssigner.class);
    when(failingAssigner.assignInstanceIdByEnv()).thenReturn(null);
    failingGenerator.setInstanceIdAssigner(failingAssigner);

    assertThatThrownBy(failingGenerator::afterPropertiesSet).isInstanceOf(
        RuntimeException.class).hasMessageContaining("Failed to obtain instance id");
  }

  @Test
  @DisplayName("should throw IdGenerateException when timestamp delta exceeds configured bits")
  void testGenerationException() throws Exception {
    DefaultUidGenerator exhausted = new DefaultUidGenerator();
    exhausted.setTimeBits(28);
    exhausted.setWorkerBits(22);
    exhausted.setSeqBits(13);
    exhausted.setEpochStr("1970-01-01");
    exhausted.setInstanceIdAssigner(mockAssigner);
    exhausted.afterPropertiesSet();

    assertThatThrownBy(() -> exhausted.getUID()).isInstanceOf(IdGenerateException.class)
        .hasMessageContaining("Timestamp bits is exhausted");
  }

  @Test
  @DisplayName("should handle instance info correctly")
  void testInstanceInfoSetup() throws Exception {
    InstanceInfo mockInfo = mock(InstanceInfo.class);
    when(mockInfo.getHost()).thenReturn("127.0.0.1");
    when(mockInfo.getPort()).thenReturn("8080");
    when(mockInfo.getInstanceType()).thenReturn(InstanceType.HOST);

    DefaultUidGenerator gen = new DefaultUidGenerator();
    gen.setInstanceInfo(mockInfo);
    gen.setInstanceIdAssigner(mockAssigner);

    assertThatCode(gen::afterPropertiesSet).doesNotThrowAnyException();
  }
}
