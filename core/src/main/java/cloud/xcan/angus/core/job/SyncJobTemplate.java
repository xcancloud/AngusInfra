package cloud.xcan.angus.core.job;

import cloud.xcan.angus.core.utils.AppEnvUtils;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.experimental.DistributedLock;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@DoInFuture("When the number of failures exceeds the limit, the circuit breaker is triggered to stop the execution of the Job.")
@Slf4j
public class SyncJobTemplate implements JobTemplate {

  private final DistributedLock distributedLock;

  public SyncJobTemplate(DistributedLock distributedLock) {
    this.distributedLock = distributedLock;
  }

  @Override
  public void execute(String lockKey, long timeout, TimeUnit unit, SyncTaskCallback callback) {
    if (!AppEnvUtils.APP_INIT_READY) {
      log.warn("Application not ready, skip sync job task {}.",
          callback.getClass().getSimpleName());
      return;
    }

    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = distributedLock.tryLock(lockKey, reqId, timeout, unit);
      if (!tryLock) {
        log.warn("[{}] Acquire lock failed", lockKey);
        return;
      }

      // Execute business callback
      callback.doInSync();

      log.debug("[{}] Sync task executed successfully", lockKey);
    } catch (Exception e) {
      log.error("[{}] Sync task failed: {}", lockKey, e.getMessage(), e);
    } finally {
      distributedLock.releaseLock(lockKey, reqId);
    }
  }

}
