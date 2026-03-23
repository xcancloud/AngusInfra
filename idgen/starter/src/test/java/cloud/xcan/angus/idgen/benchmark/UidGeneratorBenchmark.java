package cloud.xcan.angus.idgen.benchmark;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.uid.InstanceIdAssigner;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
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
 * UID 生成：使用与生产一致的 {@link CachedUidGenerator}（RingBuffer + {@link InstanceIdAssigner}）。
 * <p>
 * 运行示例：
 * <pre>
 *   mvn -pl idgen/starter test-compile exec:java \
 *     -Dexec.classpathScope=test \
 *     -Dexec.mainClass=org.openjdk.jmh.Main \
 *     -Dexec.args="cloud.xcan.angus.idgen.benchmark.UidGeneratorBenchmark"
 * </pre>
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
public class UidGeneratorBenchmark {

  private CachedUidGenerator generator;

  @Setup
  public void setup() throws Exception {
    generator = new CachedUidGenerator();
    generator.setTimeBits(28);
    generator.setWorkerBits(22);
    generator.setSeqBits(13);
    generator.setEpochStr("2016-05-20");
    generator.setBoostPower(2);

    InstanceIdAssigner assigner = mock(InstanceIdAssigner.class);
    when(assigner.assignInstanceIdByEnv()).thenReturn(1L);
    generator.setInstanceIdAssigner(assigner);

    generator.afterPropertiesSet();
  }

  @TearDown
  public void tearDown() throws Exception {
    if (generator != null) {
      generator.destroy();
    }
  }

  @Benchmark
  public long singleThreadGetUid() {
    return generator.getUID();
  }

  @Benchmark
  public long singleThreadBatch100() {
    long last = 0;
    for (int i = 0; i < 100; i++) {
      last = generator.getUID();
    }
    return last;
  }

  @Benchmark
  @Threads(4)
  public long fourThreadsGetUid() {
    return generator.getUID();
  }

  @Benchmark
  public String parseSampleUid() {
    long uid = generator.getUID();
    return generator.parseUID(uid);
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(UidGeneratorBenchmark.class.getSimpleName())
        .result("benchmark_uid.json")
        .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
        .build();
    new Runner(opt).run();
  }
}
