package cloud.xcan.sdf.spec.thread.delay;

import static java.lang.Thread.NORM_PRIORITY;

import cloud.xcan.sdf.spec.thread.DefaultThreadFactory;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author XiaoLong Liu
 */
@Slf4j
public class DelayOrderQueueManager {

  private final ExecutorService executor;
  private final DelayQueue<DelayOrderTask<?>> delayQueue;

  public final static String XCAN_DELAY_ORDER_QUEUE_SCHEDULE = "xcanDelayOrderQueueSchedule";
  public final static String XCAN_DELAY_ORDER_QUEUE_EXEC = "xcanDelayOrderQueueExec";

  public DelayOrderQueueManager() {
    this(Runtime.getRuntime().availableProcessors() * 2, XCAN_DELAY_ORDER_QUEUE_EXEC,
        XCAN_DELAY_ORDER_QUEUE_SCHEDULE);
  }

  public DelayOrderQueueManager(int execThreads) {
    this(execThreads, XCAN_DELAY_ORDER_QUEUE_EXEC, XCAN_DELAY_ORDER_QUEUE_SCHEDULE);
  }

  public DelayOrderQueueManager(int execThreads, String execThreadName, String scheduleThreadName) {
    this.executor = Executors.newFixedThreadPool(execThreads,
        new DefaultThreadFactory(execThreadName, NORM_PRIORITY + 1));
    this.delayQueue = new DelayQueue<>();
    startScheduleThread(scheduleThreadName);
  }

  private void startScheduleThread(String scheduleThreadName) {
    Thread scheduleThread = new Thread(this::execute);
    scheduleThread.setName(scheduleThreadName);
    scheduleThread.start();
  }

  private void execute() {
    DelayOrderTask<?> delayOrderTask;
    Runnable task;
    for (; ; ) {
      try {
        delayOrderTask = delayQueue.take();
        task = delayOrderTask.getTask();
        if (Objects.nonNull(task)) {
          executor.execute(task);
        }
      } catch (InterruptedException e) {
        log.error("Handling delay task exception", e);
      }
    }
  }

  /**
   * Add task
   *
   * @param time Delay time
   * @param unit time unit
   */
  public void put(Runnable task, long time, TimeUnit unit) {
    long timeout = TimeUnit.NANOSECONDS.convert(time, unit);
    DelayOrderTask<?> delayOrder = new DelayOrderTask<>(timeout, task);
    delayQueue.put(delayOrder);
  }

  /**
   * Delete task
   */
  public boolean removeTask(DelayOrderTask<?> task) {
    return delayQueue.remove(task);
  }

  public void stop() {
    executor.shutdown();
  }
}  
