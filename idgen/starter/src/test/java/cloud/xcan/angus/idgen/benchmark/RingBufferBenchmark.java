package cloud.xcan.angus.idgen.benchmark;

import cloud.xcan.angus.idgen.uid.RingBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * RingBuffer性能基准测试
 * <p>
 * RingBuffer是lock-free的环形缓冲区，用于缓存预生成的UID
 * <p>
 * 测试以下场景： 1. 单线程put/take操作 2. 多线程并发put（生产者） 3. 多线程并发take（消费者） 4. 生产者-消费者（不同比率） 5. 填充率（Padding）
 * <p>
 * 目标：单线程吞吐 > 50M ops/sec
 * <p>
 * 运行方式： mvn exec:java -Dexec.mainClass="cloud.xcan.angus.idgen.benchmark.RingBufferBenchmark"
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
public class RingBufferBenchmark {

  private RingBuffer ringBuffer;
  private ExecutorService producerExecutor;
  private ExecutorService consumerExecutor;

  // RingBuffer初始化参数
  private static final int RING_BUFFER_SIZE = 8192;
  private static final int PADDING_FACTOR = 50;

  @Setup
  public void setup() {
    // 创建RingBuffer（16bit表示buffer大小，padding factor用于避免False Sharing）
    ringBuffer = new RingBuffer(RING_BUFFER_SIZE, PADDING_FACTOR);

    // 为缓冲区填充初始值
    for (long i = 0; i < RING_BUFFER_SIZE; i++) {
      ringBuffer.put(i);
    }

    producerExecutor = Executors.newFixedThreadPool(4);
    consumerExecutor = Executors.newFixedThreadPool(4);
  }

  @TearDown
  public void tearDown() {
    if (producerExecutor != null && !producerExecutor.isShutdown()) {
      producerExecutor.shutdown();
      try {
        if (!producerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
          producerExecutor.shutdownNow();
        }
      } catch (InterruptedException e) {
        producerExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
    if (consumerExecutor != null && !consumerExecutor.isShutdown()) {
      consumerExecutor.shutdown();
      try {
        if (!consumerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
          consumerExecutor.shutdownNow();
        }
      } catch (InterruptedException e) {
        consumerExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * 单线程顺序put操作（生产）
   */
  @Benchmark
  public boolean singleThreadPut() {
    return ringBuffer.put(System.nanoTime());
  }

  /**
   * 单线程顺序take操作（消费）
   */
  @Benchmark
  public long singleThreadTake() {
    long value = ringBuffer.take();
    if (value > 0) {
      ringBuffer.put(value + 1); // 循环利用，产生新ID
    }
    return value;
  }

  /**
   * 多线程并发put操作（4个生产者线程）
   */
  @Benchmark
  public void multiProducerPut() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(4);
    AtomicLong putCount = new AtomicLong(0);

    for (int i = 0; i < 4; i++) {
      final int threadId = i;
      producerExecutor.submit(() -> {
        try {
          for (int j = 0; j < 100; j++) {
            long value = System.nanoTime() + threadId * 10000000000L + j;
            if (ringBuffer.put(value)) {
              putCount.incrementAndGet();
            }
          }
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
  }

  /**
   * 多线程并发take操作（4个消费者线程）
   */
  @Benchmark
  public void multiConsumerTake() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(4);
    AtomicLong takeCount = new AtomicLong(0);

    for (int i = 0; i < 4; i++) {
      consumerExecutor.submit(() -> {
        try {
          for (int j = 0; j < 100; j++) {
            long value = ringBuffer.take();
            if (value > 0) {
              takeCount.incrementAndGet();
              // 循环利用
              ringBuffer.put(value + 1);
            }
          }
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
  }

  /**
   * 生产者-消费者场景：2生产，2消费
   */
  @Benchmark
  public void producerConsumerBalance() throws InterruptedException {
    CountDownLatch producerLatch = new CountDownLatch(2);
    CountDownLatch consumerLatch = new CountDownLatch(2);
    AtomicLong produced = new AtomicLong(0);
    AtomicLong consumed = new AtomicLong(0);

    // 启动2个生产者线程
    for (int i = 0; i < 2; i++) {
      final int threadId = i;
      producerExecutor.submit(() -> {
        try {
          for (int j = 0; j < 200; j++) {
            long value = System.nanoTime() + threadId * 1000000000L + j;
            if (ringBuffer.put(value)) {
              produced.incrementAndGet();
            }
            Thread.yield(); // 让出CPU给消费者线程
          }
        } finally {
          producerLatch.countDown();
        }
      });
    }

    // 启动2个消费者线程
    for (int i = 0; i < 2; i++) {
      consumerExecutor.submit(() -> {
        try {
          while (consumed.get() < 400) {
            long value = ringBuffer.take();
            if (value > 0) {
              consumed.incrementAndGet();
              ringBuffer.put(value + 1);
            } else {
              Thread.yield();
            }
          }
        } finally {
          consumerLatch.countDown();
        }
      });
    }

    producerLatch.await();
    consumerLatch.await();
  }

  /**
   * 测试Padding对性能的影响 通过避免False Sharing，单线程和多线程性能差异应该较小
   */
  @Benchmark
  public void paddingEffectMeasure() throws InterruptedException {
    // 4个线程，每个线程执行1000次操作
    CountDownLatch latch = new CountDownLatch(4);
    AtomicLong[] counters = new AtomicLong[4];

    for (int i = 0; i < 4; i++) {
      counters[i] = new AtomicLong(0);
      final int threadId = i;
      producerExecutor.submit(() -> {
        try {
          for (int j = 0; j < 1000; j++) {
            // 模拟对单个计数器的持续访问
            counters[threadId].incrementAndGet();
            // 同时进行RingBuffer操作
            ringBuffer.put(System.nanoTime());
          }
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
  }

  /**
   * 运行所有基准测试
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(RingBufferBenchmark.class.getSimpleName())
        .result("benchmark_ringbuffer.json")
        .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
        .build();

    new Runner(opt).run();
  }
}
