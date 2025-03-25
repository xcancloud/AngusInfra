package cloud.xcan.angus.spec.thread;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultiTaskThreadPool {

  private static final String DEFAULT_THREAD_PREFIX = "MultiTaskThreadPool";
  private static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
  private static final int DEFAULT_MAXIMUM_POOL_SIZE = DEFAULT_CORE_POOL_SIZE * 2;
  private static final long DEFAULT_KEEP_ALIVE_TIME = 60L;
  private static final int DEFAULT_QUEUE_CAPACITY = 1000;

  private final ExecutorService executorService;

  public MultiTaskThreadPool() {
    this(DEFAULT_THREAD_PREFIX, DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE,
        DEFAULT_KEEP_ALIVE_TIME, DEFAULT_QUEUE_CAPACITY);
  }

  public MultiTaskThreadPool(String threadPrefix, int corePoolSize, int maxPoolSize,
      long keepAliveTime, int queueCapacity) {
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);
    ThreadFactory threadFactory = new DefaultThreadFactory(threadPrefix);
    RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
    executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime,
        TimeUnit.SECONDS, workQueue, threadFactory, handler);
  }

  public void execute(Runnable task) {
    executorService.execute(task);
  }

  public <T> Future<T> submit(Callable<T> task) {
    return executorService.submit(task);
  }

  public <T> List<Future<T>> invokeAll(List<Callable<T>> tasks) throws InterruptedException {
    return executorService.invokeAll(tasks);
  }

  public <T> T invokeAny(List<Callable<T>> tasks) throws InterruptedException, ExecutionException {
    return executorService.invokeAny(tasks);
  }

  public void shutdown() {
    executorService.shutdown();
  }

  public List<Runnable> shutdownNow() {
    return executorService.shutdownNow();
  }

  public boolean isShutdown() {
    return executorService.isShutdown();
  }

  public boolean isTerminated() {
    return executorService.isTerminated();
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executorService.awaitTermination(timeout, unit);
  }

}
