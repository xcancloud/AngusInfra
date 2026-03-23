package cloud.xcan.angus.idgen.benchmark;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.idgen.bid.ConfigIdAssigner;
import cloud.xcan.angus.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.angus.idgen.bid.Format;
import cloud.xcan.angus.idgen.bid.Mode;
import cloud.xcan.angus.idgen.bid.impl.DefaultBidGenerator;
import cloud.xcan.angus.idgen.entity.IdConfig;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * 业务 ID（BID）生成：基于当前 {@link DefaultBidGenerator} API（{@code getId(bizKey, tenantId)} +
 * {@link ConfigIdAssigner} / {@link DistributedIncrAssigner}）。
 * <p>
 * 运行示例：
 * <pre>
 *   mvn -pl idgen/starter test-compile exec:java \
 *     -Dexec.classpathScope=test \
 *     -Dexec.mainClass=org.openjdk.jmh.Main \
 *     -Dexec.args="cloud.xcan.angus.idgen.benchmark.BidGeneratorBenchmark"
 * </pre>
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
@OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
public class BidGeneratorBenchmark {

  private static final int NUM_TENANTS = 10;
  private static final Long[] TENANT_IDS =
      IntStream.range(0, NUM_TENANTS).mapToObj(Long::valueOf).toArray(Long[]::new);
  private static final String[] BIZ_KEYS =
      IntStream.range(0, 5).mapToObj(i -> "ORDER_SEQ_" + i).toArray(String[]::new);

  private DefaultBidGenerator bidGenerator;
  private final AtomicLong nextAssignMaxId = new AtomicLong(0L);

  @Setup
  public void setup() {
    ConfigIdAssigner config = mock(ConfigIdAssigner.class);
    DistributedIncrAssigner incr = mock(DistributedIncrAssigner.class);

    when(config.retrieveFromIdConfig(anyString(), anyLong())).thenAnswer(inv -> {
      String bizKey = inv.getArgument(0);
      Long tenantId = inv.getArgument(1);
      return idConfig(bizKey, tenantId, Format.SEQ);
    });
    when(config.assignSegmentByParam(anyLong(), anyString(), anyLong()))
        .thenAnswer(inv -> nextAssignMaxId.addAndGet(50_000L));

    nextAssignMaxId.set(0L);
    bidGenerator = new DefaultBidGenerator(config, incr, 512);
  }

  private static IdConfig idConfig(String bizKey, Long tenantId, Format format) {
    IdConfig config = new IdConfig();
    config.setBizKey(bizKey);
    config.setTenantId(tenantId);
    config.setFormat(format);
    config.setSeqLength(6);
    config.setStep(1000L);
    config.setMode(Mode.DB);
    return config;
  }

  @TearDown
  public void tearDown() {
    bidGenerator = null;
  }

  @Benchmark
  public String singleTenantSingleBizKey() {
    return bidGenerator.getId("ORDER_SEQ_0", BidGenerator.GLOBAL_TENANT_ID);
  }

  @Benchmark
  public String singleTenantRoundRobinBizKeys() {
    int idx = (int) ((System.nanoTime() >>> 10) % BIZ_KEYS.length);
    return bidGenerator.getId(BIZ_KEYS[idx], BidGenerator.GLOBAL_TENANT_ID);
  }

  @Benchmark
  public String multiTenantRoundRobin() {
    int t = (int) ((System.nanoTime() >>> 10) % NUM_TENANTS);
    int b = (int) ((System.nanoTime() >>> 14) % BIZ_KEYS.length);
    return bidGenerator.getId(BIZ_KEYS[b], TENANT_IDS[t]);
  }

  @Benchmark
  public List<String> batchTenIds() {
    return bidGenerator.getIds("ORDER_SEQ_0", 10, BidGenerator.GLOBAL_TENANT_ID);
  }

  @Benchmark
  @Threads(4)
  public String fourThreadsGlobalTenant() {
    return bidGenerator.getId("ORDER_SEQ_0", BidGenerator.GLOBAL_TENANT_ID);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(BidGeneratorBenchmark.class.getSimpleName())
        .result("benchmark_bid.json")
        .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
        .build();
    new Runner(opt).run();
  }
}
