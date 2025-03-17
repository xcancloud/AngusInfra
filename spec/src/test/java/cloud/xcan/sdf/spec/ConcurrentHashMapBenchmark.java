package cloud.xcan.sdf.spec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapBenchmark {

  private static final int NUM_THREADS = 100;
  private static final int NUM_ITERATIONS = 1000000;

  /**
   * Test Result:
   *
   * <pre>
   * ConcurrentHashMap write time：2569ms
   * HashMap write time：1911ms
   * ConcurrentHashMap read time：1222ms
   * HashMap read time：1440ms
   * </pre>
   */
  public static void main(String[] args) throws InterruptedException {
    Map<String, Integer> concurrentHashMap = new ConcurrentHashMap<>();
    Map<String, Integer> hashMap = new HashMap<>();

    // Concurrent write testing
    long concurrentHashMapWriteTime = runWriteTest(concurrentHashMap);
    long hashMapWriteTime = runWriteTest(hashMap);

    // Concurrent read testing
    long concurrentHashMapReadTime = runReadTest(concurrentHashMap);
    long hashMapReadTime = runReadTest(hashMap);

    // Print results
    System.out.println("ConcurrentHashMap write time：" + concurrentHashMapWriteTime + "ms");
    System.out.println("HashMap write time：" + hashMapWriteTime + "ms");
    System.out.println("ConcurrentHashMap read time：" + concurrentHashMapReadTime + "ms");
    System.out.println("HashMap read time：" + hashMapReadTime + "ms");
  }

  private static long runWriteTest(Map<String, Integer> map) throws InterruptedException {
    long startTime = System.currentTimeMillis();

    Thread[] threads = new Thread[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < NUM_ITERATIONS; j++) {
          map.put("key" + j, j);
        }
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }

  private static long runReadTest(Map<String, Integer> map) throws InterruptedException {
    // First, concurrent writes
    runWriteTest(map);

    long startTime = System.currentTimeMillis();

    Thread[] threads = new Thread[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < NUM_ITERATIONS; j++) {
          map.get("key" + j);
        }
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }
}
