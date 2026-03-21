package cloud.xcan.angus.idgen.benchmark;

import cloud.xcan.angus.idgen.DefaultBidGenerator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * BID（业务ID）生成性能基准测试
 *
 * 测试以下场景：
 * 1. 单个业务key的ID生成
 * 2. 多个业务key（10个租户）的并发生成
 * 3. 批量ID生成（步长配置）
 *
 * 目标吞吐量：≥200K ops/sec（单租户）
 *
 * 运行方式：
 * mvn exec:java -Dexec.mainClass="cloud.xcan.angus.idgen.benchmark.BidGeneratorBenchmark"
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
public class BidGeneratorBenchmark {

  private DefaultBidGenerator bidGenerator;
  private ExecutorService executorService;
  private static final int NUM_THREADS = 10;
  private static final int NUM_TENANTS = 10;
  private static final String[] TENANT_IDS = IntStream.range(0, NUM_TENANTS)
      .mapToObj(i -> String.format("tenant_%d", i))
      .toArray(String[]::new);
  private static final String[] BIZ_KEYS = IntStream.range(0, 5)
      .mapToObj(i -> String.format("ORDER_SEQ_%d", i))
      .toArray(String[]::new);

  @Setup
  public void setup() {
    bidGenerator = new DefaultBidGenerator();
    bidGenerator.setMaxStep(1000000L);
    bidGenerator.setMaxBatchNum(10000L);
    bidGenerator.setMaxSeqLength(40L);
    bidGenerator.setInitialMapCapacity(512);

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
   * 单租户单业务线程ID生成基准测试
   */
  @Benchmark
  public String singleTenantSingleBizKey() {
    return bidGenerator.nextId("tenant_0", "ORDER_SEQ_0");
  }

  /**
   * 单租户多业务线程并发生成（5个不同的业务线程）
   */
  @Benchmark
  public String singleTenantMultiBizKeys() {
    String tenantId = "tenant_0";
    // Round-robin选择业务线程
    int bizIndex = (int) ((System.nanoTime() / 1000) % BIZ_KEYS.length);
    return bidGenerator.nextId(tenantId, BIZ_KEYS[bizIndex]);
  }

  /**
   * 多租户并发生成（轮流访问10个租户）
   */
  @Benchmark
  public String multiTenantConcurrent() {
    int tenantIndex = (int) ((System.nanoTime() / 1000) % NUM_TENANTS);
    int bizIndex = (int) ((System.nanoTime() / 1000 / NUM_TENANTS) % BIZ_KEYS.length);
    return bidGenerator.nextId(TENANT_IDS[tenantIndex], BIZ_KEYS[bizIndex]);
  }

  /**
   * 多线程并发生成（NumThreads个线程，每个线程处理多个租户）
   */
  @Benchmark
  public void multiThreadMultiTenant() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(NUM_THREADS);
    for (int threadId = 0; threadId < NUM_THREADS; threadId++) {
      final int tid = threadId;
      executorService.submit(() -> {
        try {
          for (int i = 0; i < 20; i++) {
            int tenantIdx = (tid + i) % NUM_TENANTS;
            int bizIdx = i % BIZ_KEYS.length;
            bidGenerator.nextId(TENANT_IDS[tenantIdx], BIZ_KEYS[bizIdx]);
          }
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
  }

  /**
   * 批量ID获取（一次获取10个ID）
   * 获取格式示例：seq_1, seq_2, ..., seq_10
   */
  @Benchmark
  public String batchIdGeneration() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      String bid = bidGenerator.nextId("tenant_0", "ORDER_SEQ_0");
      if (i > 0) sb.append(",");
      sb.append(bid);
    }
    return sb.toString();
  }

  /**
   * 验证ID唯一性（采样检查：生成100个ID）
   */
  @Benchmark
  public Set<String> validateUniqueness() {
    return IntStream.range(0, 100)
        .mapToObj(i -> bidGenerator.nextId("tenant_0", "ORDER_SEQ_0"))
        .collect(Collectors.toSet());
  }

  /**
   * 运行所有基准测试
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(BidGeneratorBenchmark.class.getSimpleName())
        .result("benchmark_bid.json")
        .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
        .build();

    new Runner(opt).run();
  }
}
