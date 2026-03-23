package cloud.xcan.angus.idgen.benchmark;

import cloud.xcan.angus.idgen.uid.buffer.BufferPaddingExecutor;
import cloud.xcan.angus.idgen.uid.buffer.RingBuffer;
import java.util.ArrayList;
import java.util.List;
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
 * {@link RingBuffer}：{@code take()} 会触发 {@link BufferPaddingExecutor#asyncPadding()}，必须注入
 * padding 执行器，否则会 NPE。
 * <p>
 * 运行示例：
 * <pre>
 *   mvn -pl idgen/starter test-compile exec:java \
 *     -Dexec.classpathScope=test \
 *     -Dexec.mainClass=org.openjdk.jmh.Main \
 *     -Dexec.args="cloud.xcan.angus.idgen.benchmark.RingBufferBenchmark"
 * </pre>
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
public class RingBufferBenchmark {

  private static final int RING_BUFFER_SIZE = 8192;
  private static final int PADDING_FACTOR = 50;

  private RingBuffer ringBuffer;
  private BufferPaddingExecutor bufferPaddingExecutor;

  private List<Long> paddingBatch(long second) {
    List<Long> list = new ArrayList<>(512);
    long base = second * 1_000_000L;
    for (int i = 0; i < 512; i++) {
      list.add(base + i);
    }
    return list;
  }

  @Setup
  public void setup() {
    ringBuffer = new RingBuffer(RING_BUFFER_SIZE, PADDING_FACTOR);
    bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::paddingBatch, false);
    ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
    for (long i = 0; i < RING_BUFFER_SIZE / 2; i++) {
      ringBuffer.put(i);
    }
  }

  @TearDown
  public void tearDown() {
    if (bufferPaddingExecutor != null) {
      bufferPaddingExecutor.shutdown();
    }
  }

  /**
   * 消费一个槽位并放回新值，维持缓冲区大致平衡。
   */
  @Benchmark
  public long takeThenPut() {
    long v = ringBuffer.take();
    ringBuffer.put(v + 1);
    return v;
  }

  @Benchmark
  @Threads(4)
  public long fourThreadsTakeThenPut() {
    long v = ringBuffer.take();
    ringBuffer.put(v + 1);
    return v;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(RingBufferBenchmark.class.getSimpleName())
        .result("benchmark_ringbuffer.json")
        .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
        .build();
    new Runner(opt).run();
  }
}
