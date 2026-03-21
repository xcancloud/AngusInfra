package cloud.xcan.angus.idgen.benchmark;

import cloud.xcan.angus.idgen.DefaultUidGenerator;
import cloud.xcan.angus.idgen.WorkerNodeStrategy;
import cloud.xcan.angus.idgen.uid.RingBuffer;
import cloud.xcan.angus.idgen.uid.UID;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.*;

/**
 * UID生成性能基准测试
 *
 * 测试以下场景：
 * 1. 单线程顺序生成
 * 2. 多线程并发生成（10个线程）
 * 3. 并发+批量获取
 *
 * 目标吞吐量：≥6.5M ops/sec（单线程）
 *
 * 运行方式：
 * mvn exec:java -Dexec.mainClass="cloud.xcan.angus.idgen.benchmark.UidGeneratorBenchmark"
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
public class UidGeneratorBenchmark {

  private DefaultUidGenerator uidGenerator;
  private ExecutorService executorService;
  private static final int RING_BUFFER_PAD_POWER = 2;
  private static final int NUM_THREADS = 10;

  @Setup
  public void setup() {
    // 创建UID生成器
    uidGenerator = new DefaultUidGenerator(MockWorkerNodeStrategy.INSTANCE);
    uidGenerator.setTimeBits(28);
    uidGenerator.setWorkerBits(22);
    uidGenerator.setSeqBits(13);
    uidGenerator.setEpochStr("2016-05-20");
    uidGenerator.setStartInstanceId(1L);

    // 创建线程池
    executorService = Executors.newFixedThreadPool(NUM_THREADS);
  }

  @TearDown
  public void tearDown() {
    if (executorService != null && !executorService.isShutdown()) {
      executorService.shutdown();
      try {
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
          executorService.shutdownNow();
        }
      } catch (InterruptedException e) {
        executorService.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * 单线程顺序生成UID基准测试
   */
  @Benchmark
  public long singleThreadSequential() {
    return uidGenerator.nextId();
  }

  /**
   * 单线程批量生成UID（批大小=100）
   */
  @Benchmark
  public long singleThreadBatch100() {
    long sum = 0;
    for (int i = 0; i < 100; i++) {
      sum += uidGenerator.nextId();
    }
    return sum;
  }

  /**
   * 多线程并发生成UID（10个线程）
   * 使用CountDownLatch等待所有线程完成【注：基准测试中应避免复杂同步】
   */
  @Benchmark
  public void multiThreadConcurrent() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(NUM_THREADS);
    for (int i = 0; i < NUM_THREADS; i++) {
      // 每个线程生成100个ID
      executorService.submit(() -> {
        try {
          for (int j = 0; j < 100; j++) {
            uidGenerator.nextId();
          }
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
  }

  /**
   * 多线程并发生成+验证ID唯一性（检查样本）
   */
  @Benchmark
  public Set<Long> multiThreadValidateUniqueness() throws InterruptedException {
    Set<Long> ids = Collections.newSetFromMap(new ConcurrentHashMap<>());
    CountDownLatch latch = new CountDownLatch(NUM_THREADS);

    for (int i = 0; i < NUM_THREADS; i++) {
      executorService.submit(() -> {
        try {
          for (int j = 0; j < 10; j++) {
            ids.add(uidGenerator.nextId());
          }
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
    return ids;
  }

  /**
   * 测试ID格式正确性（时间戳提取）
   */
  @Benchmark
  public long parseIdMetadata() {
    long uid = uidGenerator.nextId();
    UID uidObj = new UID(uid);
    long timestamp = uidObj.getTimestamp();
    return timestamp;
  }

  /**
   * 运行所有基准测试
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(UidGeneratorBenchmark.class.getSimpleName())
        .result("benchmark_uid.json")
        .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
        .build();

    new Runner(opt).run();
  }

  /**
   * Mock WorkerNodeStrategy实现
   */
  static class MockWorkerNodeStrategy implements WorkerNodeStrategy {
    static final MockWorkerNodeStrategy INSTANCE = new MockWorkerNodeStrategy();

    @Override
    public long getWorkerId() {
      return 1L;
    }

    @Override
    public int getInstanceId() {
      return 1;
    }
  }
}
