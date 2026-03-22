package cloud.xcan.angus.core.job;

import java.util.concurrent.TimeUnit;

public interface JobTemplate {

  /**
   * Execute distributed synchronization task
   *
   * @param lockKey  Distributed lock Key
   * @param timeout  Lock hold timeout time
   * @param unit     Time unit
   * @param callback Business callback interface
   */
  void execute(String lockKey, long timeout, TimeUnit unit, SyncTaskCallback callback);
}
