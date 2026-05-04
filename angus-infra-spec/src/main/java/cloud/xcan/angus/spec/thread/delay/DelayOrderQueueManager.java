package cloud.xcan.angus.spec.thread.delay;

import static java.lang.Thread.NORM_PRIORITY;

import cloud.xcan.angus.spec.thread.DefaultThreadFactory;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Schedules {@link Runnable} tasks on a {@link DelayQueue} and executes them on a fixed pool when
 * due.
 */
@Slf4j
public class DelayOrderQueueManager {

  public static final String XCAN_DELAY_ORDER_QUEUE_SCHEDULE = "xcanDelayOrderQueueSchedule";
  public static final String XCAN_DELAY_ORDER_QUEUE_EXEC = "xcanDelayOrderQueueExec";

  private final ExecutorService executor;
  private final DelayQueue<DelayOrderTask<?>> delayQueue;
  private final Thread scheduleThread;
  private volatile boolean running = true;

  public DelayOrderQueueManager() {
    this(Runtime.getRuntime().availableProcessors() * 2, XCAN_DELAY_ORDER_QUEUE_EXEC,
        XCAN_DELAY_ORDER_QUEUE_SCHEDULE);
  }

  public DelayOrderQueueManager(int execThreads) {
    this(execThreads, XCAN_DELAY_ORDER_QUEUE_EXEC, XCAN_DELAY_ORDER_QUEUE_SCHEDULE);
  }

  public DelayOrderQueueManager(int execThreads, String execThreadName, String scheduleThreadName) {
    Objects.requireNonNull(execThreadName, "execThreadName");
    Objects.requireNonNull(scheduleThreadName, "scheduleThreadName");
    this.executor = Executors.newFixedThreadPool(execThreads,
        new DefaultThreadFactory(execThreadName, false,
            Math.min(NORM_PRIORITY + 1, Thread.MAX_PRIORITY)));
    this.delayQueue = new DelayQueue<>();
    this.scheduleThread = Thread.ofPlatform().name(scheduleThreadName).start(this::scheduleLoop);
  }

  private void scheduleLoop() {
    while (running) {
      try {
        DelayOrderTask<?> delayOrderTask = delayQueue.take();
        Runnable task = delayOrderTask.getTask();
        if (task != null) {
          executor.execute(task);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.debug("Delay schedule thread interrupted, stopping: {}", scheduleThread.getName());
        break;
      } catch (Throwable t) {
        log.error("Unexpected error in delay schedule loop", t);
      }
    }
  }

  /**
   * Enqueues a delayed task; returns the wrapper for {@link #removeTask(DelayOrderTask)}.
   *
   * @param time delay amount
   * @param unit time unit of {@code time}
   */
  public DelayOrderTask<?> put(Runnable task, long time, TimeUnit unit) {
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(unit, "unit");
    long delayNanos = unit.toNanos(time);
    DelayOrderTask<?> delayOrder = new DelayOrderTask<>(delayNanos, task);
    delayQueue.put(delayOrder);
    return delayOrder;
  }

  public boolean removeTask(DelayOrderTask<?> task) {
    return delayQueue.remove(task);
  }

  /**
   * Stops accepting new delayed work, interrupts the scheduler, and shuts down the executor pool.
   */
  public void stop() {
    running = false;
    scheduleThread.interrupt();
    executor.shutdown();
    try {
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
